package com.github.chengyuxing.plugin.rabbit.sql.common;

import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.common.script.exception.ScriptSyntaxException;
import com.github.chengyuxing.common.script.pipe.IPipe;
import com.github.chengyuxing.common.util.ReflectUtils;
import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.plugin.rabbit.sql.ui.XqlFileManagerToolWindow;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.XqlFileManagerPanel;
import com.github.chengyuxing.plugin.rabbit.sql.util.ArrayListValueSet;
import com.github.chengyuxing.plugin.rabbit.sql.util.ClassFileLoader;
import com.github.chengyuxing.plugin.rabbit.sql.util.NotificationUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.XQLFileManagerConfig;
import com.github.chengyuxing.sql.exceptions.XQLParseException;
import com.github.chengyuxing.sql.util.SqlGenerator;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service(Service.Level.PROJECT)
public final class XQLConfigManager implements Disposable {
    private static final Logger log = Logger.getInstance(XQLConfigManager.class);

    private final Project project;
    private final Map<Path, Set<Config>> configMap = new ConcurrentHashMap<>();
    private final NotificationExecutor notificationExecutor;

    public static XQLConfigManager getInstance(Project project) {
        return project.getService(XQLConfigManager.class);
    }

    XQLConfigManager(Project project) {
        this.project = project;
        this.notificationExecutor = new NotificationExecutor(messages ->
                messages.forEach(m ->
                        NotificationUtil.showMessage(project, m.getText(), m.getType()))
                , 1500);
    }

    public void add(Path module, Config config) {
        var configs = configMap.get(module);
        if (configs == null) {
            configs = new ArrayListValueSet<>();
            configMap.put(module, configs);
        }
        configs.add(config);
    }

    public Map<Path, Set<Config>> getConfigMap() {
        return configMap;
    }

    public Set<Config> getConfigs(@NotNull Path module) {
        var configs = getConfigMap().get(module);
        return Objects.nonNull(configs) ? configs : Set.of();
    }

    public void toggleActive(Config _config) {
        var configs = getConfigs(_config.getModulePath());
        for (var config : configs) {
            config.setActive(config == _config);
        }
    }

    public Config getActiveConfig(@NotNull Path module) {
        var configs = getConfigs(module);
        for (var config : configs) {
            if (config.isActive()) {
                return config;
            }
        }
        return null;
    }

    public Config getActiveConfig(PsiElement element) {
        if (element == null) {
            return null;
        }
        var module = ProjectFileUtil.getModulePath(element);
        if (module == null) {
            return null;
        }
        return getActiveConfig(module);
    }

    public XQLFileManager getActiveXqlFileManager(PsiElement element) {
        if (element == null) {
            return null;
        }
        var c = getActiveConfig(element);
        if (Objects.nonNull(c)) {
            return c.getXqlFileManager();
        }
        return null;
    }

    public void cleanup() {
        var configs = configMap;
        configs.entrySet().removeIf(entry -> {
            var moduleVf = VirtualFileManager.getInstance().findFileByNioPath(entry.getKey());
            if (Objects.nonNull(moduleVf)) {
                return !ProjectFileUtil.isResourceProjectModule(moduleVf);
            }
            return true;
        });
        configs.forEach((k, v) -> v.removeIf(config -> !config.isValid()));
    }

    public Config newConfig(VirtualFile moduleVf) {
        return new Config(moduleVf);
    }

    @Override
    public void dispose() {
        notificationExecutor.close();
        configMap.clear();
    }

    public final class Config implements AutoCloseable {
        private final Path modulePath;
        // src/main/resources
        private final Path resourcesRoot;

        private VirtualFile configVfs;
        private Path configPath;

        private final XQLFileManagerConfig xqlFileManagerConfig;
        private final XQLFileManager xqlFileManager;
        private final Set<String> originalXqlFiles;
        private SqlGenerator sqlGenerator = new SqlGenerator(':');
        private boolean active = false;

        public Config(VirtualFile moduleVfs) {
            this.modulePath = moduleVfs.toNioPath();

            this.resourcesRoot = this.modulePath.resolve(Constants.RESOURCE_ROOT);

            this.originalXqlFiles = ConcurrentHashMap.newKeySet();

            this.xqlFileManagerConfig = new XQLFileManagerConfig();
            this.xqlFileManager = new XQLFileManager() {
                private final Path classesPath = modulePath.resolve(Path.of("target", "classes"));

                @Override
                protected Map<String, IPipe<?>> buildPipeInstances() {
                    Thread currentThread = Thread.currentThread();
                    ClassLoader originalClassLoader = currentThread.getContextClassLoader();
                    ClassLoader pluginClassLoader = this.getClass().getClassLoader();
                    try {
                        currentThread.setContextClassLoader(pluginClassLoader);
                        ClassFileLoader loader = ClassFileLoader.of(pluginClassLoader, classesPath);
                        Map<String, IPipe<?>> newPipeInstances = new HashMap<>();
                        Map<String, IPipe<?>> oldPipeInstances = this.getPipeInstances();
                        for (Map.Entry<String, String> e : getPipes().entrySet()) {
                            var pipeName = e.getKey();
                            var pipeClassName = e.getValue();

                            IPipe<?> old = oldPipeInstances.get(pipeName);
                            if (old != null) {
                                newPipeInstances.put(pipeName, old);
                            } else {
                                var pipeClassPath = classesPath.resolve(pipeClassName.replace(".", "/") + ".class");
                                if (!Files.exists(pipeClassPath)) {
                                    notificationExecutor.show(Message.warning(MessageBundle.message("xql.config.manager.loadPipe.notExists", messagePrefix(), pipeClassPath)));
                                    continue;
                                }
                                try {
                                    var pipeClass = loader.findClass(pipeClassName);
                                    if (pipeClass == null) {
                                        continue;
                                    }
                                    newPipeInstances.put(pipeName, (IPipe<?>) ReflectUtils.getInstance(pipeClass));
                                } catch (Throwable ex) {
                                    notificationExecutor.show(Message.warning(MessageBundle.message("xql.config.manager.loadPipe.error", messagePrefix(), pipeClassName, ex.getMessage())));
                                }
                            }
                        }
                        return newPipeInstances;
                    } finally {
                        currentThread.setContextClassLoader(originalClassLoader);
                    }
                }
            };
        }

        public void setConfigVfs(VirtualFile configVfs) {
            this.configVfs = configVfs;
            if (Objects.nonNull(this.configVfs)) {
                this.configPath = this.configVfs.toNioPath();
                this.active = isPrimary();
            }
        }

        Set<Message> initXqlFileManager() {
            if (!isValid()) {
                return Set.of();
            }
            Set<Message> successes = new HashSet<>();
            Set<Message> warnings = new HashSet<>();
            try {
                xqlFileManagerConfig.loadYaml(new FileResource(configPath.toUri().toString()));
                if (xqlFileManagerConfig.getFiles().isEmpty()) {
                    return Set.of();
                }
                xqlFileManagerConfig.copyStateTo(xqlFileManager);
                var newFiles = new LinkedHashMap<String, String>();
                for (Map.Entry<String, String> e : xqlFileManager.getFiles().entrySet()) {
                    var alias = e.getKey();
                    // e.g. xqls/home.xql
                    var filename = e.getValue().trim();
                    if (filename.isEmpty()) {
                        originalXqlFiles.add("");
                        warnings.add(Message.warning(MessageBundle.message("xql.config.manager.loadXql.empty", messagePrefix(), alias)));
                        continue;
                    }
                    String uri = getUri(filename);
                    // whatever valid or not, save original xql-file-manager.yml files.
                    originalXqlFiles.add(uri);
                    if (ProjectFileUtil.isLocalFileUri(uri) && !Files.exists(Path.of(URI.create(uri)))) {
                        warnings.add(Message.warning(MessageBundle.message("xql.config.manager.loadXql.notExists", messagePrefix(), filename)));
                        continue;
                    }
                    newFiles.put(alias, uri);
                }
                xqlFileManager.setFiles(newFiles);
                xqlFileManager.init();
                successes.add(Message.info(MessageBundle.message("xql.config.manager.loadXql.success", messagePrefix())));
            } catch (XQLParseException e) {
                if (e.getCause() instanceof ScriptSyntaxException cause) {
                    warnings.add(Message.warning(messagePrefix() + e.getMessage()));
                    warnings.add(Message.warning(messagePrefix() + cause.getMessage()));
                } else {
                    warnings.add(Message.error(messagePrefix() + e.getMessage()));
                    log.warn(e);
                }
            } catch (ConcurrentModificationException e) {
                log.warn(e);
            } catch (Exception e) {
                warnings.add(Message.error(MessageBundle.message("xql.config.manager.loadXql.error", messagePrefix(), e.getMessage())));
                log.warn(e);
            }
            if (warnings.isEmpty()) {
                return successes;
            }
            return warnings;
        }

        private @NotNull String getUri(String filename) {
            String uri;
            if (ProjectFileUtil.isURI(filename)) {
                uri = filename;
            } else {
                // e.g. src/main/resources/xqls/home.xql
                uri = resourcesRoot.resolve(filename).toUri().toString();
            }
            return uri;
        }

        private String messagePrefix() {
            return "[" + getModuleName() + ":" + getConfigName() + "]  ";
        }

        private void fire(boolean silent) {
            ProgressManager.getInstance().run(new Task.Backgroundable(project, MessageBundle.message("xql.config.manager.loadXql.progress"), true) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    ProgressManager.checkCanceled();
                    indicator.setIndeterminate(true);
                    var messages = initXqlFileManager();
                    if (silent) {
                        return;
                    }
                    notificationExecutor.show(messages);
                }

                @Override
                public void onSuccess() {
                    ApplicationManager.getApplication().invokeLater(() -> XqlFileManagerToolWindow.getXqlFileManagerPanel(project, XqlFileManagerPanel::updateStates));
                }

                @Override
                public void onCancel() {
                    ApplicationManager.getApplication().invokeLater(() -> NotificationUtil.showMessage(project, "Loading XQL files canceled.", NotificationType.WARNING));
                }
            });
        }

        public void fire() {
            fire(false);
        }

        public void silentFire() {
            fire(true);
        }

        public SqlGenerator getSqlGenerator() {
            if (sqlGenerator.getNamedParamPrefix() != xqlFileManager.getNamedParamPrefix()) {
                sqlGenerator = new SqlGenerator(xqlFileManager.getNamedParamPrefix());
            }
            return sqlGenerator;
        }

        public XQLFileManagerConfig getXqlFileManagerConfig() {
            return xqlFileManagerConfig;
        }

        public XQLFileManager getXqlFileManager() {
            return xqlFileManager;
        }

        public Path getConfigPath() {
            return configPath;
        }

        public VirtualFile getConfigVfs() {
            return configVfs;
        }

        public Project getProject() {
            return project;
        }

        public Set<String> getOriginalXqlFiles() {
            return originalXqlFiles;
        }

        public String getConfigName() {
            return configVfs.getName();
        }

        public Path getModulePath() {
            return modulePath;
        }

        public String getModuleName() {
            return modulePath.getFileName().toString();
        }

        public Path getResourcesRoot() {
            return resourcesRoot;
        }

        /**
         * Is current config active.
         *
         * @return true or false
         */
        public boolean isActive() {
            return active;
        }

        void setActive(boolean active) {
            this.active = active;
        }

        /**
         * src/main/resources/xql-file-manager.yml
         *
         * @return true or false
         */
        public boolean isPrimary() {
            if (!isPhysicExists()) {
                return false;
            }
            return configPath.endsWith(Constants.CONFIG_PATH);
        }

        public boolean isValid() {
            if (Objects.isNull(project)) return false;
            return isPhysicExists();
        }

        public boolean isPhysicExists() {
            if (Objects.isNull(configPath)) {
                return false;
            }
            return Files.exists(configPath);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Config config)) return false;

            return Objects.equals(getProject(), config.getProject()) && getModulePath().equals(config.getModulePath()) && Objects.equals(getConfigPath(), config.getConfigPath());
        }

        @Override
        public int hashCode() {
            int result = Objects.hashCode(getProject());
            result = 31 * result + getModulePath().hashCode();
            result = 31 * result + Objects.hashCode(getConfigPath());
            return result;
        }

        @Override
        public void close() {
            xqlFileManager.close();
            originalXqlFiles.clear();
        }
    }
}
