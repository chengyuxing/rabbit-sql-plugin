package com.github.chengyuxing.plugin.rabbit.sql;

import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.plugin.rabbit.sql.common.ResourceCache;
import com.github.chengyuxing.plugin.rabbit.sql.util.XqlUtil;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;

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
        for (var vf : ProjectRootManager.getInstance(project).getContentSourceRoots()) {
            var xqlFileManager = vf.toNioPath().resolve(Constants.CONFIG_NAME);
            if (XqlUtil.xqlFileManagerExists(xqlFileManager)) {
                var resource = resourceCache.createResource(project, xqlFileManager);
                if (resource != null) {
                    resource.fire();
                }
            }
        }
    }
}
