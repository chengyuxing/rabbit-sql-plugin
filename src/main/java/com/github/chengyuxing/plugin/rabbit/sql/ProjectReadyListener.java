package com.github.chengyuxing.plugin.rabbit.sql;

import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.plugin.rabbit.sql.common.ResourceCache;
import com.github.chengyuxing.plugin.rabbit.sql.util.XqlUtil;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFileManager;

import java.util.stream.Stream;

public class ProjectReadyListener implements DumbService.DumbModeListener {
    private final ResourceCache resourceCache = ResourceCache.getInstance();
    private final Project project;

    public ProjectReadyListener(Project project) {
        this.project = project;
    }

    @Override
    public void exitDumbMode() {
        // file:///Users/chengyuxing/IdeaProjects/my-project/sbp-test1/src/main/resources
        // file:///Users/chengyuxing/IdeaProjects/my-project/sbp-test2/src/main/resources
        Stream.of(ProjectRootManager.getInstance(project).getContentSourceRoots()).forEach(vf -> {
            var xqlFileManager = vf.toNioPath().resolve(Constants.CONFIG_NAME);
            if (XqlUtil.xqlFileManagerExists(xqlFileManager)) {
                resourceCache.createResource(project, xqlFileManager);
            }
        });
        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new XqlFileChangeListener(project));
        project.getMessageBus().connect().subscribe(ProjectManager.TOPIC, new XqlConfigLifecycleListener());
    }
}
