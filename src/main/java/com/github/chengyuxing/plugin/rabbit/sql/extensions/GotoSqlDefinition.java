package com.github.chengyuxing.plugin.rabbit.sql.extensions;

import com.github.chengyuxing.plugin.rabbit.sql.common.ResourceCache;
import com.github.chengyuxing.plugin.rabbit.sql.file.XqlIcons;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiJavaTokenImpl;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;

import static com.github.chengyuxing.plugin.rabbit.sql.common.Constants.SQL_NAME_PATTERN;

public class GotoSqlDefinition extends RelatedItemLineMarkerProvider {
    private static final Logger log = Logger.getInstance(GotoSqlDefinition.class);

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement javaElement, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        if (!(javaElement instanceof PsiJavaTokenImpl) || !(javaElement.getParent() instanceof PsiLiteralExpression)) {
            return;
        }
        PsiLiteralExpression literalExpression = (PsiLiteralExpression) javaElement.getParent();
        String sqlRef = literalExpression.getValue() instanceof String ? (String) literalExpression.getValue() : null;
        if (sqlRef == null) {
            return;
        }
        if (sqlRef.matches(SQL_NAME_PATTERN)) {
            sqlRef = sqlRef.substring(1);
            var resource = ResourceCache.getInstance().getResource(javaElement);
            if (resource != null && resource.getXqlFileManager().contains(sqlRef)) {
                var dotIdx = sqlRef.indexOf(".");
                var alias = sqlRef.substring(0, dotIdx).trim();
                var sqlName = sqlRef.substring(dotIdx + 1).trim();
                try {
                    var allXqlFiles = resource.getXqlFileManager().getFiles();
                    if (allXqlFiles.containsKey(alias)) {
                        var xqlFilePath = allXqlFiles.get(alias);
                        var xqlPath = Path.of(URI.create(xqlFilePath));

                        var vf = VirtualFileManager.getInstance().findFileByNioPath(xqlPath);
                        if (vf == null || !vf.isValid()) return;
                        Project project = javaElement.getProject();
                        var xqlFile = PsiManager.getInstance(project).findFile(vf);
                        if (xqlFile == null) return;
                        xqlFile.acceptChildren(new PsiElementVisitor() {
                            @Override
                            public void visitComment(@NotNull PsiComment comment) {
                                if (comment.getText().matches("/\\*\\s*\\[\\s*" + sqlName + "\\s*]\\s*\\*/")) {
                                    var markInfo = NavigationGutterIconBuilder.create(XqlIcons.XQL_FILE)
                                            .setTarget(comment)
                                            .setTooltipText(xqlPath.getFileName() + " -> " + sqlName)
                                            .createLineMarkerInfo(javaElement);
                                    result.add(markInfo);
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    log.warn(e);
                }
            }
        }
    }
}
