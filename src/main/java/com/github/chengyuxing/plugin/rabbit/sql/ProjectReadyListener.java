package com.github.chengyuxing.plugin.rabbit.sql;

import com.github.chengyuxing.plugin.rabbit.sql.ui.XqlFileManagerToolWindow;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.XqlFileManagerPanel;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class ProjectReadyListener implements DumbService.DumbModeListener {
    private final Project project;
    private final Path projectPath;

    public ProjectReadyListener(Project project) {
        this.project = project;
        this.projectPath = ProjectFileUtil.getProjectPath(project);
    }

    @Override
    public void exitDumbMode() {
        if (projectPath == null) return;
        ProgressManager.getInstance().run(new Task.Backgroundable(project, MessageBundle.message("project.ready.progress"), true) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                progressIndicator.setIndeterminate(true);
                ProjectFileUtil.loadProjectConfigs(project, true, ProgressManager::checkCanceled);
            }

            @Override
            public void onSuccess() {
                ApplicationManager.getApplication().invokeLater(() -> XqlFileManagerToolWindow.getXqlFileManagerPanel(project, XqlFileManagerPanel::updateStates));
            }
        });
    }
}
