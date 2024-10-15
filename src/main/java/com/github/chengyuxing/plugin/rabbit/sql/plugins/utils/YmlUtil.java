package com.github.chengyuxing.plugin.rabbit.sql.plugins.utils;

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLAnchor;
import org.jetbrains.yaml.psi.YamlRecursivePsiElementVisitor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class YmlUtil {
    public static Map<String, String> getYmlAnchors(Project project, VirtualFile configYml) {
        var anchors = new LinkedHashMap<String, String>();
        if (Objects.isNull(configYml) || !configYml.exists()) {
            return Map.of();
        }
        var ymlPsi = PsiManager.getInstance(project).findFile(configYml);
        if (Objects.isNull(ymlPsi)) {
            return Map.of();
        }
        ProgressManager.checkCanceled();
        ymlPsi.acceptChildren(new YamlRecursivePsiElementVisitor() {
            @Override
            public void visitAnchor(@NotNull YAMLAnchor anchor) {
                var name = anchor.getName();
                var markedValue = anchor.getMarkedValue();
                if (Objects.nonNull(markedValue)) {
                    var value = markedValue.getText().substring(name.length() + 1).trim();
                    anchors.put(name, value);
                }
            }
        });
        return anchors;
    }
}
