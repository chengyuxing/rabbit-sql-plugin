package com.github.chengyuxing.plugin.rabbit.sql;

import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ProjectReadyListener implements DumbService.DumbModeListener {
    private final XQLConfigManager xqlConfigManager = XQLConfigManager.getInstance();
    private final Project project;

    public ProjectReadyListener(Project project) {
        this.project = project;
    }

    @Override
    public void exitDumbMode() {
        var modules = ModuleManager.getInstance(project).getModules();
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Searching xql configs...", true) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                progressIndicator.setIndeterminate(true);
                ApplicationManager.getApplication().runReadAction(() -> {
                    for (Module module : modules) {
                        ProgressManager.checkCanceled();
                        var moduleVfs = ProjectUtil.guessModuleDir(module);
                        if (Objects.nonNull(moduleVfs) && moduleVfs.exists()) {
                            if (!ProjectFileUtil.isResourceProjectModule(moduleVfs)) {
                                continue;
                            }
                            var moduleNioPath = moduleVfs.toNioPath();
                            var allConfigVfs = FilenameIndex.getAllFilesByExt(project, "yml", module.getModuleProductionSourceScope());
                            if (allConfigVfs.isEmpty()) {
                                var config = xqlConfigManager.newConfig(project, moduleVfs);
                                xqlConfigManager.add(project, moduleNioPath, config);
                                continue;
                            }
                            var found = false;
                            for (VirtualFile configVfs : allConfigVfs) {
                                if (!ProjectFileUtil.isResourceXqlFileManagerConfig(moduleVfs, configVfs)) {
                                    continue;
                                }
                                found = true;
                                var config = xqlConfigManager.newConfig(project, moduleVfs);
                                config.setConfigVfs(configVfs);
                                if (!config.isValid()) {
                                    continue;
                                }
                                config.silentFire();
                                xqlConfigManager.add(project, moduleNioPath, config);
                            }
                            if (!found) {
                                var config = xqlConfigManager.newConfig(project, moduleVfs);
                                xqlConfigManager.add(project, moduleNioPath, config);
                            }
                        }
                    }
                });
            }
        });
    }
}
