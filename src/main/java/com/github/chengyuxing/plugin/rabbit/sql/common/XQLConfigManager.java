package com.github.chengyuxing.plugin.rabbit.sql.common;

import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.common.script.IPipe;
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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class XQLConfigManager {
    private static volatile XQLConfigManager instance;
    private final Map<Project, Map<Path, Set<Config>>> configMap = new ConcurrentHashMap<>();

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
        if (!configMap.containsKey(project)) {
            var map = new HashMap<Path, Set<Config>>();
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
            configs.forEach((k, v) -> v.removeIf(config -> !config.isValid()));
        }
    }

    public static final class Config implements AutoCloseable {
        private final Project project;
        private final VirtualFile configVfs;
        private final Path configPath;
        private final Path modulePath;
        // src/main/resources
        private final Path resourcesRoot;

        private final NotificationExecutor notificationExecutor;
        private final XQLFileManagerConfig xqlFileManagerConfig;
        private final XQLFileManager xqlFileManager;
        private final Set<String> originalXqlFiles;
        private SqlGenerator sqlGenerator = new SqlGenerator(':');
        private boolean active = false;

        public Config(Project project, VirtualFile moduleVfs, VirtualFile configVfs) {
            this.project = project;

            this.configVfs = configVfs;
            this.configPath = this.configVfs.toNioPath();

            this.modulePath = moduleVfs.toNioPath();

            this.resourcesRoot = this.modulePath.resolve(Constants.RESOURCE_ROOT);

            this.originalXqlFiles = new HashSet<>();

            this.active = isPrimary();

            this.notificationExecutor = new NotificationExecutor(messages ->
                    messages.forEach(m ->
                            NotificationUtil.showMessage(project, m.getText(), m.getType())), 1500);
            this.xqlFileManagerConfig = new XQLFileManagerConfig();
            this.xqlFileManager = new XQLFileManager() {
                private final Path classesPath = modulePath.resolve(Path.of("target", "classes"));

                @Override
                protected void loadPipes() {
                    Thread currentThread = Thread.currentThread();
                    ClassLoader originalClassLoader = currentThread.getContextClassLoader();
                    ClassLoader pluginClassLoader = this.getClass().getClassLoader();
                    try {
                        currentThread.setContextClassLoader(pluginClassLoader);
                        if (!pipes.isEmpty()) {
                            ClassFileLoader loader = ClassFileLoader.of(pluginClassLoader, classesPath);
                            for (Map.Entry<String, String> e : pipes.entrySet()) {
                                var pipeName = e.getKey();
                                var pipeClassName = e.getValue();
                                var pipeClassPath = classesPath.resolve(ResourceUtil.package2path(pipeClassName) + ".class");
                                if (!Files.exists(pipeClassPath)) {
                                    notificationExecutor.show(Message.warning(messagePrefix() + "pipe '" + pipeClassName + "' not found, maybe should re-compile project."));
                                    continue;
                                }
                                try {
                                    var pipeClass = loader.findClass(pipeClassName);
                                    if (pipeClass == null) {
                                        continue;
                                    }
                                    pipeInstances.put(pipeName, (IPipe<?>) ReflectUtil.getInstance(pipeClass));
                                } catch (Throwable ex) {
                                    notificationExecutor.show(Message.warning(messagePrefix() + "load pipe '" + pipeClassName + "' error: " + ex.getMessage()));
                                }
                            }
                        }
                    } finally {
                        currentThread.setContextClassLoader(originalClassLoader);
                    }
                }
            };
        }

        Set<Message> initXqlFileManager() {
            Set<Message> successes = new HashSet<>();
            Set<Message> warnings = new HashSet<>();
            try {
                xqlFileManagerConfig.loadYaml(new FileResource(configPath.toUri().toString()));
                xqlFileManagerConfig.copyStateTo(xqlFileManager);
                var newFiles = new HashMap<String, String>();
                for (Map.Entry<String, String> e : xqlFileManager.getFiles().entrySet()) {
                    var alias = e.getKey();
                    // e.g. xqls/home.xql
                    var filename = e.getValue().trim();
                    if (filename.isEmpty()) {
                        originalXqlFiles.add("");
                        warnings.add(Message.warning(messagePrefix() + "'" + alias + "' associated invalid location."));
                        continue;
                    }
                    Path abPath;
                    if (filename.startsWith("file:")) {
                        // e.g. file:/Users/.../home.xql
                        abPath = Path.of(URI.create(filename));
                    } else {
                        // e.g. src/main/resources/xqls/home.xql
                        abPath = resourcesRoot.resolve(filename);
                    }
                    var uri = abPath.toUri().toString();
                    // whatever valid or not, save original xql-file-manager.yml files.
                    originalXqlFiles.add(uri);
                    if (!Files.exists(abPath)) {
                        warnings.add(Message.warning(messagePrefix() + filename + " not exists."));
                        continue;
                    }
                    newFiles.put(alias, uri);
                }
                xqlFileManager.setFiles(newFiles);
                xqlFileManager.init();
                successes.add(Message.info(messagePrefix() + "updated!"));
            } catch (YamlDeserializeException e) {
                warnings.add(Message.error(messagePrefix() + "config content invalid: " + e.getMessage()));
            } catch (Exception e) {
                warnings.add(Message.error(messagePrefix() + "error: " + e.getMessage()));
            }
            if (warnings.isEmpty()) {
                return successes;
            }
            return warnings;
        }

        private String messagePrefix() {
            return "[" + getModuleName() + ":" + getConfigName() + "] " + " ";
        }

        public void fire(boolean showNotification) {
            var messages = initXqlFileManager();
            if (showNotification) {
                notificationExecutor.show(messages);
            }
        }

        public void fire() {
            fire(false);
        }

        public SqlGenerator getSqlGenerator() {
            if (sqlGenerator.getNamedParamPrefix().charAt(0) != xqlFileManager.getNamedParamPrefix()) {
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
            return configPath.endsWith(Constants.CONFIG_PATH);
        }

        public boolean isValid() {
            if (Objects.isNull(project)) return false;
            return isPhysicExists();
        }

        public boolean isExists() {
            return configVfs.exists();
        }

        public boolean isPhysicExists() {
            return Files.exists(configPath);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Config config)) return false;

            if (getProject() != null ? !getProject().equals(config.getProject()) : config.getProject() != null)
                return false;
            if (!getConfigPath().equals(config.getConfigPath())) return false;
            return getModulePath().equals(config.getModulePath());
        }

        @Override
        public int hashCode() {
            int result = getProject() != null ? getProject().hashCode() : 0;
            result = 31 * result + getConfigPath().hashCode();
            result = 31 * result + getModulePath().hashCode();
            return result;
        }

        @Override
        public void close() {
            xqlFileManager.close();
            originalXqlFiles.clear();
            notificationExecutor.close();
        }

        public void clear() {
            xqlFileManager.close();
            originalXqlFiles.clear();
        }
    }
}
