package com.github.chengyuxing.plugin.rabbit.sql;

import com.github.chengyuxing.plugin.rabbit.sql.common.ResourceCache;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.github.chengyuxing.plugin.rabbit.sql.common.Globals.openedProjects;

public class InitXqlConfigOnStartup implements ProjectActivity {
    private static final Logger log = Logger.getInstance(InitXqlConfigOnStartup.class);

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        log.debug("current project: " + project);
        if (!openedProjects.contains(project)) {
            openedProjects.add(project);
            ResourceCache.getInstance().initXqlFileManager(project);
        }
        return null;
    }
}
