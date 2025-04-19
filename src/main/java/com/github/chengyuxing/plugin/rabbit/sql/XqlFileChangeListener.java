package com.github.chengyuxing.plugin.rabbit.sql;

import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.XqlFileManagerToolWindow;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.XqlFileManagerPanel;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
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
                    VirtualFile projectVf;
                    var module = ModuleUtil.findModuleForFile(vf, project);
                    if (Objects.nonNull(module)) {
                        projectVf = ProjectUtil.guessModuleDir(module);
                    } else {
                        projectVf = ProjectUtil.guessProjectDir(project);
                    }
                    if (Objects.nonNull(projectVf) && projectVf.exists()) {
                        if (ProjectFileUtil.isResourceXqlFileManagerConfig(projectVf, vf)) {
                            var config = xqlConfigManager.newConfig(project, projectVf);
                            config.setConfigVfs(vf);
                            if (config.isValid()) {
                                config.fire();
                                xqlConfigManager.add(project, projectVf.toNioPath(), config);
                            }
                        }
                    }
                    xqlConfigManager.cleanup(project);
                    ApplicationManager.getApplication().invokeLater(() -> XqlFileManagerToolWindow.getXqlFileManagerPanel(project, XqlFileManagerPanel::updateStates));
                } else if (Objects.equals(vf.getExtension(), "xql")) {
                    var xqlPath = vf.toNioPath().toUri().toString();
                    var validXqlVf = vf;
                    // file is deleted.
                    if (!vf.isValid()) {
                        validXqlVf = ProjectFileUtil.getValidVirtualFile(vf);
                    }
                    if (Objects.isNull(validXqlVf)) continue;
                    var projectVf = ProjectFileUtil.findModule(project, validXqlVf);
                    if (Objects.isNull(projectVf)) {
                        projectVf = ProjectUtil.guessProjectDir(project);
                    }
                    if (Objects.nonNull(projectVf) && projectVf.exists()) {
                        var configs = xqlConfigManager.getConfigs(project, projectVf.toNioPath());
                        if (Objects.nonNull(configs)) {
                            log.debug("find project: " + projectVf + " configs.");
                            new HashSet<>(configs).forEach(config -> {
                                if (config.isValid()) {
                                    var configured = config.getOriginalXqlFiles().contains(xqlPath);
                                    // configured files:
                                    // content modified
                                    // file deleted
                                    // file created
                                    // other file name change matched configured files
                                    if (configured) {
                                        config.fire();
                                    } else {
                                        // filename changed which not included in config files.
                                        new HashSet<>(config.getOriginalXqlFiles()).forEach(cfgPath -> {
                                            var p = Path.of(URI.create(cfgPath));
                                            if (cfgPath.isEmpty() || !Files.exists(p)) {
                                                config.fire();
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                } else if (vf.isDirectory()) {
                    VirtualFile projectVf = null;
                    var module = ModuleUtil.findModuleForFile(vf, project);
                    if (Objects.nonNull(module)) {
                        projectVf = ProjectUtil.guessModuleDir(module);
                    } else {
                        var vfOfProject = ProjectUtil.guessProjectForFile(vf);
                        if (Objects.nonNull(vfOfProject)) {
                            projectVf = ProjectUtil.guessProjectDir(vfOfProject);
                        }
                    }
                    if (Objects.nonNull(projectVf) && projectVf.exists()) {
                        if (ProjectFileUtil.isResourceProjectModule(projectVf)) {
                            var config = xqlConfigManager.newConfig(project, projectVf);
                            xqlConfigManager.add(project, projectVf.toNioPath(), config);
                        }
                    }
                    xqlConfigManager.cleanup(project);
                    ApplicationManager.getApplication().invokeLater(() -> XqlFileManagerToolWindow.getXqlFileManagerPanel(project, XqlFileManagerPanel::updateStates));
                }
            }
        }
    }
}
