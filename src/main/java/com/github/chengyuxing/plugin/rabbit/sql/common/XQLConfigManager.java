package com.github.chengyuxing.plugin.rabbit.sql.common;

import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.common.script.exception.ScriptSyntaxException;
import com.github.chengyuxing.common.script.expression.IPipe;
import com.github.chengyuxing.common.utils.ReflectUtil;
import com.github.chengyuxing.common.utils.ResourceUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.ArrayListValueSet;
import com.github.chengyuxing.plugin.rabbit.sql.util.ClassFileLoader;
import com.github.chengyuxing.plugin.rabbit.sql.util.NotificationUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.XQLFileManagerConfig;
import com.github.chengyuxing.sql.exceptions.YamlDeserializeException;
import com.github.chengyuxing.sql.utils.SqlGenerator;
import com.intellij.openapi.diagnostic.Logger;
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
import java.util.function.Supplier;

public class XQLConfigManager {
    private static final Logger log = Logger.getInstance(XQLConfigManager.class);

    private static volatile XQLConfigManager instance;
    private final Map<Project, Map<Path, Set<Config>>> configMap = new ConcurrentHashMap<>();
    private final Map<Project, NotificationExecutor> notificationMap = new ConcurrentHashMap<>();

    private XQLConfigManager() {
    }

    public static XQLConfigManager getInstance() {
        if (instance == null) {
            synchronized (XQLConfigManager.class) {
                if (instance == null) {
                    instance = new XQLConfigManager();
                }
            }
        }
        return instance;
    }

    public void add(Project project, Path module, Config config) {
        if (!notificationMap.containsKey(project)) {
            var notificationExecutor = new NotificationExecutor(messages ->
                    messages.forEach(m ->
                            NotificationUtil.showMessage(project, m.getText(), m.getType())), 1500);
            notificationMap.put(project, notificationExecutor);
        }
        if (!configMap.containsKey(project)) {
            var map = new LinkedHashMap<Path, Set<Config>>();
            configMap.put(project, map);
        }
        var map = configMap.get(project);
        if (!map.containsKey(module)) {
            var set = new ArrayListValueSet<Config>();
            map.put(module, set);
        }
        var configs = map.get(module);
        configs.add(config);
    }

    public Map<Path, Set<Config>> getConfigMap(Project project) {
        var moduleConfigs = configMap.get(project);
        return Objects.nonNull(moduleConfigs) ? moduleConfigs : Map.of();
    }

    public Set<Config> getConfigs(Project project, Path module) {
        var configs = getConfigMap(project).get(module);
        return Objects.nonNull(configs) ? configs : Set.of();
    }

    public void toggleActive(Project project, Config _config) {
        var configs = getConfigs(project, _config.getModulePath());
        for (var config : configs) {
            config.clear();
            config.setActive(config == _config);
        }
    }

    public Config getActiveConfig(Project project, Path module) {
        var configs = getConfigs(project, module);
        for (var config : configs) {
            if (config.isActive()) {
                return config;
            }
        }
        return null;
    }

    public Config getActiveConfig(Project project, PsiElement element) {
        var module = ProjectFileUtil.getModulePath(project, element);
        return getActiveConfig(project, module);
    }

    public Config getActiveConfig(PsiElement element) {
        if (Objects.nonNull(element)) {
            return getActiveConfig(element.getProject(), element);
        }
        return null;
    }

    public XQLFileManager getActiveXqlFileManager(Project project, PsiElement element) {
        var c = getActiveConfig(project, element);
        if (Objects.nonNull(c)) {
            return c.getXqlFileManager();
        }
        return null;
    }

    public XQLFileManager getActiveXqlFileManager(PsiElement element) {
        if (Objects.nonNull(element)) {
            return getActiveXqlFileManager(element.getProject(), element);
        }
        return null;
    }

    public int size(Project project) {
        var configs = configMap.get(project);
        return Objects.nonNull(configs) ? configs.size() : 0;
    }

    public void clear(Project project) {
        var configs = configMap.remove(project);
        if (Objects.nonNull(configs))
            configs.forEach((k, v) -> v.clear());
    }

    public void cleanup(Project project) {
        var configs = configMap.get(project);
        if (Objects.nonNull(configs)) {
            configs.entrySet().removeIf(entry -> {
                var moduleVf = VirtualFileManager.getInstance().findFileByNioPath(entry.getKey());
                if (Objects.nonNull(moduleVf)) {
                    return !ProjectFileUtil.isResourceProjectModule(moduleVf);
                }
                return true;
            });
            configs.forEach((k, v) -> v.removeIf(config -> !config.isValid()));
        }
    }

    public Config newConfig(Project project, VirtualFile moduleVf) {
        return new Config(project, moduleVf);
    }

    public final class Config implements AutoCloseable {
        private final Project project;
        private final Path modulePath;
        // src/main/resources
        private final Path resourcesRoot;

        private VirtualFile configVfs;
        private Path configPath;

        private final Supplier<Optional<NotificationExecutor>> notificationExecutor;
        private final XQLFileManagerConfig xqlFileManagerConfig;
        private final XQLFileManager xqlFileManager;
        private final Set<String> originalXqlFiles;
        private SqlGenerator sqlGenerator = new SqlGenerator(':');
        private boolean active = false;

        public Config(Project project, VirtualFile moduleVfs) {
            this.project = project;

            this.modulePath = moduleVfs.toNioPath();

            this.resourcesRoot = this.modulePath.resolve(Constants.RESOURCE_ROOT);

            this.originalXqlFiles = new HashSet<>();

            this.notificationExecutor = () -> Optional.ofNullable(notificationMap.get(project));

            this.xqlFileManagerConfig = new XQLFileManagerConfig();
            this.xqlFileManager = new XQLFileManager() {
                private final Path classesPath = modulePath.resolve(Path.of("target", "classes"));

                @Override
                protected void loadPipes() {
                    if (pipes.isEmpty()) {
                        return;
                    }
                    Thread currentThread = Thread.currentThread();
                    ClassLoader originalClassLoader = currentThread.getContextClassLoader();
                    ClassLoader pluginClassLoader = this.getClass().getClassLoader();
                    try {
                        currentThread.setContextClassLoader(pluginClassLoader);
                        ClassFileLoader loader = ClassFileLoader.of(pluginClassLoader, classesPath);
                        for (Map.Entry<String, String> e : pipes.entrySet()) {
                            var pipeName = e.getKey();
                            var pipeClassName = e.getValue();
                            var pipeClassPath = classesPath.resolve(ResourceUtil.package2path(pipeClassName) + ".class");
                            if (!Files.exists(pipeClassPath)) {
                                notificationExecutor.get().ifPresent(n -> n.show(Message.warning(messagePrefix() + "pipe '" + pipeClassName + "' not found, maybe should re-compile project.")));
                                continue;
                            }
                            try {
                                var pipeClass = loader.findClass(pipeClassName);
                                if (pipeClass == null) {
                                    continue;
                                }
                                pipeInstances.put(pipeName, (IPipe<?>) ReflectUtil.getInstance(pipeClass));
                            } catch (Throwable ex) {
                                notificationExecutor.get().ifPresent(n -> n.show(Message.warning(messagePrefix() + "load pipe '" + pipeClassName + "' error: " + ex.getMessage())));
                            }
                        }
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
                        warnings.add(Message.warning(messagePrefix() + "'" + alias + "' associated invalid location."));
                        continue;
                    }
                    String uri = getUri(filename);
                    // whatever valid or not, save original xql-file-manager.yml files.
                    originalXqlFiles.add(uri);
                    if (ProjectFileUtil.isLocalFileUri(uri) && !Files.exists(Path.of(URI.create(uri)))) {
                        warnings.add(Message.warning(messagePrefix() + filename + " not exists."));
                        continue;
                    }
                    newFiles.put(alias, uri);
                }
                xqlFileManager.setFiles(newFiles);
                xqlFileManager.init();
                successes.add(Message.info(messagePrefix() + "updated!"));
            } catch (ScriptSyntaxException e) {
                warnings.add(Message.warning(messagePrefix() + e.getMessage()));
                var cause = e.getCause();
                if (Objects.nonNull(cause)) {
                    warnings.add(Message.warning(messagePrefix() + cause.getMessage()));
                }
            } catch (YamlDeserializeException e) {
                warnings.add(Message.error(messagePrefix() + "config content invalid: " + e.getMessage()));
                log.warn(e);
            } catch (Exception e) {
                warnings.add(Message.error(messagePrefix() + "error: " + e.getMessage()));
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
            return "[" + getModuleName() + ":" + getConfigName() + "] " + " ";
        }

        public void fire() {
            var messages = initXqlFileManager();
            notificationExecutor.get().ifPresent(n -> n.show(messages));
        }

        public void silentFire() {
            initXqlFileManager();
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
            notificationExecutor.get().ifPresent(NotificationExecutor::close);
        }

        public void clear() {
            xqlFileManager.close();
            originalXqlFiles.clear();
        }
    }
}
