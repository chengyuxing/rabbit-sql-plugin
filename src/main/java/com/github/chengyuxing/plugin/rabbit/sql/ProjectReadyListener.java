package com.github.chengyuxing.plugin.rabbit.sql;

import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.XqlFileManagerToolWindow;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.XqlFileManagerPanel;
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
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public class ProjectReadyListener implements DumbService.DumbModeListener {
    private final XQLConfigManager xqlConfigManager = XQLConfigManager.getInstance();
    private final Project project;

    public ProjectReadyListener(Project project) {
        this.project = project;
    }

    @Override
    public void exitDumbMode() {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Searching xql configs...", true) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                progressIndicator.setIndeterminate(true);
                ApplicationManager.getApplication().runReadAction(() -> {
                    var modules = ModuleManager.getInstance(project).getModules();
                    if (modules.length > 0) {
                        for (Module module : modules) {
                            ProgressManager.checkCanceled();
                            var moduleVfs = ProjectUtil.guessModuleDir(module);
                            var allConfigVfs = FilenameIndex.getAllFilesByExt(project, "yml", module.getModuleContentScope());
                            addConfigs(moduleVfs, allConfigVfs);
                        }
                    } else {
                        var projectVf = ProjectUtil.guessProjectDir(project);
                        var allConfigVfs = FilenameIndex.getAllFilesByExt(project, "yml", GlobalSearchScope.projectScope(project));
                        addConfigs(projectVf, allConfigVfs);
                    }
                });
            }
        });
    }

    private void addConfigs(VirtualFile projectVf, Collection<VirtualFile> allConfigVfs) {
        if (Objects.isNull(projectVf) || !projectVf.exists()) {
            return;
        }
        if (!ProjectFileUtil.isResourceProjectModule(projectVf)) {
            return;
        }
        var projectNioPath = projectVf.toNioPath();
        var found = false;
        for (VirtualFile configVfs : allConfigVfs) {
            if (!ProjectFileUtil.isResourceXqlFileManagerConfig(projectVf, configVfs)) {
                continue;
            }
            found = true;
            var config = xqlConfigManager.newConfig(project, projectVf);
            config.setConfigVfs(configVfs);
            if (!config.isValid()) {
                continue;
            }
            config.silentFire();
            xqlConfigManager.add(project, projectNioPath, config);
        }
        if (!found) {
            var config = xqlConfigManager.newConfig(project, projectVf);
            xqlConfigManager.add(project, projectNioPath, config);
            ApplicationManager.getApplication().invokeLater(() -> XqlFileManagerToolWindow.getXqlFileManagerPanel(project, XqlFileManagerPanel::updateStates));
        }
    }
}
