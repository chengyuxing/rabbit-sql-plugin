package com.github.chengyuxing.plugin.rabbit.sql;

import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;

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
        for (Module module : modules) {
            var moduleVfs = ProjectUtil.guessModuleDir(module);
            if (Objects.nonNull(moduleVfs) && moduleVfs.exists()) {
                var allConfigVfs = FilenameIndex.getAllFilesByExt(project, "yml", module.getModuleProductionSourceScope());
                for (VirtualFile configVfs : allConfigVfs) {
                    var config = new XQLConfigManager.Config(project, moduleVfs, configVfs);
                    var configName = config.getConfigName();
                    // maybe for support springboot.
                    if (!ProjectFileUtil.isXqlFileManagerConfig(configName)) {
                        continue;
                    }
                    if (!config.isValid()) {
                        continue;
                    }
                    if (config.isActive()) {
                        config.fire();
                    }
                    xqlConfigManager.add(project, moduleVfs.toNioPath(), config);
                }
            }
        }
    }
}
