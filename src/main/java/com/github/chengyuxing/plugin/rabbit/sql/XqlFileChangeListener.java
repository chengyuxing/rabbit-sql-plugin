package com.github.chengyuxing.plugin.rabbit.sql;

import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.plugin.rabbit.sql.common.ResourceCache;
import com.github.chengyuxing.plugin.rabbit.sql.util.PsiUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.github.chengyuxing.plugin.rabbit.sql.util.PsiUtil.projectContains;

public class XqlFileChangeListener implements BulkFileListener {
    private final ResourceCache resourceCache = ResourceCache.getInstance();
    private final Project project;

    public XqlFileChangeListener(Project project) {
        this.project = project;
    }

    @Override
    public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
        for (var event : events) {
            var vf = event.getFile();
            if (vf != null && vf.getExtension() != null) {
                var ext = vf.getExtension();
                // xql-file-manager.yml
                if (vf.getName().equals(Constants.CONFIG_NAME)) {
                    // 内容修改、文件创建
                    if (projectContains(project, vf)) {
                        resourceCache.createResource(project, vf.toNioPath());
                        // 文件被删除
                    } else if (!vf.isValid()) {
                        var res = vf.getParent();
                        if (res != null && res.isValid()) {
                            resourceCache.removeResource(project, res);
                        }
                    }
                    // 如果是有其他yml文件变动，这里需要确定，是不是 xql-file-manager.yml 改名了或者路径变了
                } else if (ext.equals("yml")) {
                    if (projectContains(project, vf)) {
                        var module = PsiUtil.getModuleDir(project, vf);
                        var xqlFileManager = module.resolve(Constants.CONFIG_PATH);
                        var xqlFileManagerVf = VirtualFileManager.getInstance().findFileByNioPath(xqlFileManager);
                        // 这里说明猜想正确，项目中没有名为 xql-file-manager.yml 的文件
                        if (xqlFileManagerVf == null || !xqlFileManagerVf.exists()) {
                            // 项目中已经不存在此配置了，删除可能存在的缓存
                            resourceCache.removeResource(project, vf);
                        }
                    }
                } else if (ext.equals("xql")) {
                    if (projectContains(project, vf)) {
                        ResourceCache.Resource resource;
                        if (vf.isValid()) {
                            resource = resourceCache.getResource(project, vf);
                        } else {
                            resource = resourceCache.getResource(project, vf.getParent());
                        }
                        if (resource != null)
                            resource.fire("xql file removed or update: " + vf);
                    } else if (!vf.isValid()) {
                        ResourceCache.Resource resource = resourceCache.getResource(project, vf.getParent());
                        if (resource != null)
                            resource.fire("xql file removed: " + vf);
                    }
                }
            }
        }
    }
}
