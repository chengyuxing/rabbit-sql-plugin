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
    private final Map<Path, Set<Config>> configMap = new HashMap<>();
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

    public synchronized void add(Path module, Config config) {
        if (module == null) {
            return;
        }
        var configs = configMap.computeIfAbsent(module, key -> new ArrayListValueSet<>());
        var old = configs.stream().filter(config::equals).findFirst().orElse(null);
        if (old != null) {
            config.setActive(old.isActive());
        } else if (configs.stream().anyMatch(Config::isActive)) {
            config.setActive(false);
        }
        configs.add(config);
    }

    public synchronized Map<Path, Set<Config>> getConfigMap() {
        Map<Path, Set<Config>> snapshot = new LinkedHashMap<>();
        configMap.forEach((module, configs) -> snapshot.put(module, getConfigs(module)));
        return Collections.unmodifiableMap(snapshot);
    }

    public synchronized Set<Config> getConfigs(Path module) {
        if (module == null) {
            return Set.of();
        }
        var configs = configMap.get(module);
        return configs == null ? Set.of() : Collections.unmodifiableSet(new LinkedHashSet<>(configs));
    }

    public synchronized void toggleActive(Config _config) {
        var configs = getConfigs(_config.getModulePath());
        if (!configs.contains(_config)) {
            return;
        }
        for (var config : configs) {
            config.setActive(config.equals(_config));
        }
    }

    public synchronized Config getActiveConfig(Path module) {
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

    public synchronized void cleanup() {
        var configs = configMap;
        var projectPath = ProjectFileUtil.getProjectPath(project);
        configs.entrySet().removeIf(entry -> {
            // remove other modules which not belongs the current project, god know why.
            if (projectPath == null || !entry.getKey().startsWith(projectPath)) {
                return true;
            }
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
    public synchronized void dispose() {
        notificationExecutor.close();
        configMap.clear();
    }

    public abstract class PluginXQLFileManager extends XQLFileManager {
        private final Path[] classesPaths;

        private volatile Map<String, String> errorAlias = new LinkedHashMap<>();

        public PluginXQLFileManager(Path modulePath) {
            this.classesPaths = new Path[]{
                    modulePath.resolve("target/classes"),
                    modulePath.resolve("build/classes/java/main"),
                    modulePath.resolve("build/classes/kotlin/main")
            };
        }

        protected abstract String messagePrefix();

        public Map<String, String> getErrorAlias() {
            return errorAlias;
        }

        @Override
        protected Map<String, Resource> buildResources() {
            Set<Message> messages = new LinkedHashSet<>();
            Map<String, Resource> newResources = new LinkedHashMap<>();
            Map<String, Resource> oldResources = this.getResources();
            Map<String, String> errors = new LinkedHashMap<>();
            for (Map.Entry<String, String> e : getFiles().entrySet()) {
                String alias = e.getKey();
                String filename = e.getValue();

                FileResource fr = createFileResource(filename);
                if (!fr.exists()) {
                    messages.add(Message.warning(messagePrefix() + MessageBundle.message("xql.config.manager.loadResource.notExists", filename, alias)));
                    continue;
                }
                String ext = fr.getFilenameExtension();
                if (ProjectFileUtil.isXqlFileExtension(ext)) {
                    try {
                        Resource old = oldResources.get(alias);
                        if (old != null
                                && old.getFilename().equals(filename)
                                && old.getLastModified() == fr.getLastModified()) {
                            newResources.put(alias, old);
                        } else {
                            newResources.put(alias, parseXql(alias, filename, fr));
                        }
                    } catch (XQLParseException ex) {
                        StringJoiner sb = new StringJoiner("\n");
                        if (ex.getCause() instanceof ScriptSyntaxException cause) {
                            messages.add(Message.warning(messagePrefix() + ex.getMessage()));
                            messages.add(Message.warning(messagePrefix() + cause.getMessage()));
                            sb.add(ex.getMessage());
                            sb.add(cause.getMessage());
                        } else {
                            messages.add(Message.error(messagePrefix() + ex.getMessage()));
                            sb.add(ex.getMessage());
                            log.warn(ex);
                        }
                        newResources.put(alias, new Resource(filename));
                        errors.put(alias, sb.toString());
                    } catch (Exception ex) {
                        messages.add(Message.error(messagePrefix() + ex.getMessage()));
                        newResources.put(alias, new Resource(filename));
                        errors.put(alias, ex.getMessage());
                        log.warn(ex);
                    }
                }
            }
            notificationExecutor.show(messages);
            this.errorAlias = errors;
            return newResources;
        }

        @Override
        protected Map<String, IPipe<?>> buildPipeInstances() {
            Thread currentThread = Thread.currentThread();
            ClassLoader originalClassLoader = currentThread.getContextClassLoader();
            ClassLoader pluginClassLoader = this.getClass().getClassLoader();
            try {
                currentThread.setContextClassLoader(pluginClassLoader);
                ClassFileLoader loader = ClassFileLoader.of(pluginClassLoader, classesPaths);
                Map<String, IPipe<?>> newPipeInstances = new HashMap<>();
                for (Map.Entry<String, String> e : getPipes().entrySet()) {
                    var pipeName = e.getKey();
                    var pipeClassName = e.getValue();
                    try {
                        var pipeClass = loader.loadClass(pipeClassName);
                        newPipeInstances.put(pipeName, (IPipe<?>) ReflectUtils.getInstance(pipeClass));
                    } catch (ClassNotFoundException ex) {
                        notificationExecutor.show(Message.warning(MessageBundle.message("xql.config.manager.loadPipe.notExists", messagePrefix(), pipeClassName)));
                    } catch (Throwable ex) {
                        notificationExecutor.show(Message.warning(MessageBundle.message("xql.config.manager.loadPipe.error", messagePrefix(), pipeClassName, ex.getMessage())));
                    }
                }
                return newPipeInstances;
            } finally {
                currentThread.setContextClassLoader(originalClassLoader);
            }
        }

        @Override
        public void close() {
            super.close();
            errorAlias = new LinkedHashMap<>();
        }
    }

    public final class Config implements AutoCloseable {
        private final Path modulePath;
        // src/main/resources
        private final Path resourcesRoot;

        private volatile VirtualFile configVfs;
        private volatile Path configPath;

        private final XQLFileManagerConfig xqlFileManagerConfig;
        private final PluginXQLFileManager xqlFileManager;
        private final Set<String> originalXqlFiles;
        private volatile boolean active = false;

        public Config(VirtualFile moduleVfs) {
            this.modulePath = moduleVfs.toNioPath();

            this.resourcesRoot = this.modulePath.resolve(Constants.RESOURCES_ROOT);

            this.originalXqlFiles = ConcurrentHashMap.newKeySet();

            this.xqlFileManagerConfig = new XQLFileManagerConfig();
            this.xqlFileManager = new PluginXQLFileManager(modulePath) {
                @Override
                protected String messagePrefix() {
                    return Config.this.messagePrefix();
                }
            };
        }

        public synchronized void setConfigVfs(VirtualFile configVfs) {
            this.configVfs = configVfs;
            if (Objects.nonNull(this.configVfs)) {
                this.configPath = this.configVfs.toNioPath();
                this.active = isPrimary();
            }
        }

        synchronized Set<Message> initXqlFileManager() {
            if (!isValid()) {
                return Set.of();
            }
            Set<Message> successes = new LinkedHashSet<>();
            Set<Message> warnings = new LinkedHashSet<>();
            try {
                // source user project xql files
                var loadedConfig = new XQLFileManagerConfig();
                loadedConfig.loadYaml(new FileResource(configPath.toUri().toString()));
                loadedConfig.copyStateTo(xqlFileManagerConfig);
                loadedConfig.copyStateTo(xqlFileManager);
                originalXqlFiles.clear();
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
            var configName = getConfigName();
            if (!configName.isEmpty()) {
                configName = ":" + configName;
            }
            return "[" + getModuleName() + configName + "]  ";
        }

        public void fire(boolean silent) {
            ProgressManager.getInstance().run(new Task.Backgroundable(project, MessageBundle.message("xql.config.manager.loadXql.progress"), true) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    indicator.setIndeterminate(true);
                    ProgressManager.checkCanceled();
                    var messages = initXqlFileManager();
                    if (silent) {
                        return;
                    }
                    notificationExecutor.show(messages);
                }

                @Override
                public void onSuccess() {
                    XqlFileManagerToolWindow.getXqlFileManagerPanel(project, XqlFileManagerPanel::updateStates);
                }

                @Override
                public void onCancel() {
                    NotificationUtil.showMessage(project, MessageBundle.message("xql.config.manager.loadXql.cancel"), NotificationType.WARNING);
                }
            });
        }

        public void fire() {
            fire(false);
        }

        public SqlGenerator getSqlGenerator() {
            return xqlFileManager.getSqlGenerator();
        }

        public synchronized XQLFileManagerConfig getXqlFileManagerConfig() {
            var snapshot = new XQLFileManagerConfig();
            xqlFileManagerConfig.copyStateTo(snapshot);
            return snapshot;
        }

        public PluginXQLFileManager getXqlFileManager() {
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
            if (configVfs == null) {
                return "";
            }
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
        public synchronized void close() {
            xqlFileManager.close();
            originalXqlFiles.clear();
        }
    }
}
