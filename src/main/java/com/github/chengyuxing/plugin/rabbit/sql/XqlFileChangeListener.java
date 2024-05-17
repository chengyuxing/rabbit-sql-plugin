package com.github.chengyuxing.plugin.rabbit.sql;

import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.XqlFileManagerToolWindow;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.XqlFileManagerPanel;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class XqlFileChangeListener implements BulkFileListener {
    private static final Logger log = Logger.getInstance(XqlFileChangeListener.class);
    private final XQLConfigManager xqlConfigManager = XQLConfigManager.getInstance();
    private final Project project;

    public XqlFileChangeListener(Project project) {
        this.project = project;
    }

    @Override
    public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
        for (var event : events) {
            var vf = event.getFile();
            if (vf != null) {
                if (ProjectFileUtil.isXqlFileManagerConfig(vf.getName())) {
                    var module = ModuleUtil.findModuleForFile(vf, project);
                    if (Objects.nonNull(module)) {
                        var moduleVf = ProjectUtil.guessModuleDir(module);
                        if (Objects.nonNull(moduleVf) && moduleVf.exists()) {
                            if (ProjectFileUtil.isResourceXqlFileManagerConfig(moduleVf, vf)) {
                                var config = new XQLConfigManager.Config(project, moduleVf);
                                config.setConfigVfs(vf);
                                if (config.isValid()) {
                                    if (config.isActive()) {
                                        config.fire(true);
                                    }
                                    xqlConfigManager.add(project, moduleVf.toNioPath(), config);
                                }
                            }
                        }
                    }
                    xqlConfigManager.cleanup(project);
                    XqlFileManagerToolWindow.getXqlFileManagerPanel(project, XqlFileManagerPanel::updateStates);
                } else if (Objects.equals(vf.getExtension(), "xql")) {
                    var xqlPath = vf.toNioPath().toUri().toString();
                    var validXqlVf = vf;
                    // file is deleted.
                    if (!vf.isValid()) {
                        validXqlVf = ProjectFileUtil.getValidVirtualFile(vf);
                    }
                    if (Objects.isNull(validXqlVf)) continue;
                    var moduleVf = ProjectFileUtil.findModule(project, validXqlVf);
                    if (Objects.nonNull(moduleVf) && moduleVf.exists()) {
                        var configs = xqlConfigManager.getConfigs(project, moduleVf.toNioPath());
                        if (Objects.nonNull(configs)) {
                            log.debug("find module: " + moduleVf + " configs.");
                            configs.forEach(config -> {
                                if (config.isValid() && config.isActive()) {
                                    var configured = config.getOriginalXqlFiles().contains(xqlPath);
                                    // configured files:
                                    // content modified
                                    // file deleted
                                    // file created
                                    // other file name change matched configured files
                                    if (configured) {
                                        config.fire(true);
                                    } else {
                                        // filename changed which not included in config files.
                                        config.getOriginalXqlFiles().forEach(cfgPath -> {
                                            var p = Path.of(URI.create(cfgPath));
                                            if (cfgPath.isEmpty() || !Files.exists(p)) {
                                                config.fire(true);
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                } else if (vf.isDirectory()) {
                    var module = ModuleUtil.findModuleForFile(vf, project);
                    if (Objects.nonNull(module)) {
                        var moduleVf = ProjectUtil.guessModuleDir(module);
                        if (Objects.nonNull(moduleVf) && moduleVf.exists()) {
                            if (ProjectFileUtil.isResourceProjectModule(moduleVf)) {
                                var config = new XQLConfigManager.Config(project, moduleVf);
                                xqlConfigManager.add(project, moduleVf.toNioPath(), config);
                            }
                        }
                    }
                    xqlConfigManager.cleanup(project);
                    XqlFileManagerToolWindow.getXqlFileManagerPanel(project, XqlFileManagerPanel::updateStates);
                }
            }
        }
    }
}
