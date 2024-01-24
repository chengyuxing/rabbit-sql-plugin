package com.github.chengyuxing.plugin.rabbit.sql.extensions;

import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.file.XqlIcons;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.openapi.diagnostic.ControlFlowException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiJavaTokenImpl;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;

import static com.github.chengyuxing.plugin.rabbit.sql.common.Constants.SQL_NAME_PATTERN;

public class GotoXqlDefinition extends RelatedItemLineMarkerProvider {
    private static final Logger log = Logger.getInstance(GotoXqlDefinition.class);

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement javaElement, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        if (!(javaElement instanceof PsiJavaTokenImpl) || !(javaElement.getParent() instanceof PsiLiteralExpression literalExpression)) {
            return;
        }
        String sqlRef = literalExpression.getValue() instanceof String ? (String) literalExpression.getValue() : null;
        if (sqlRef == null) {
            return;
        }
        if (sqlRef.matches(SQL_NAME_PATTERN)) {
            sqlRef = sqlRef.substring(1);
            var xqlFileManager = XQLConfigManager.getInstance().getActiveXqlFileManager(javaElement);
            if (Objects.nonNull(xqlFileManager) && xqlFileManager.contains(sqlRef)) {
                var dotIdx = sqlRef.indexOf(".");
                var alias = sqlRef.substring(0, dotIdx).trim();
                var sqlName = sqlRef.substring(dotIdx + 1).trim();
                try {
                    var allXqlFiles = xqlFileManager.getFiles();
                    if (allXqlFiles.containsKey(alias)) {
                        var xqlFilePath = allXqlFiles.get(alias);
                        var xqlPath = Path.of(URI.create(xqlFilePath));

                        var vf = VirtualFileManager.getInstance().findFileByNioPath(xqlPath);
                        if (vf == null || !vf.isValid()) return;
                        Project project = javaElement.getProject();
                        var xqlFile = PsiManager.getInstance(project).findFile(vf);
                        if (xqlFile == null) return;
                        ProgressManager.checkCanceled();
                        xqlFile.acceptChildren(new PsiRecursiveElementVisitor() {
                            @Override
                            public void visitElement(@NotNull PsiElement element) {
                                if (element instanceof PsiComment comment) {
                                    if (comment.getText().matches("/\\*\\s*\\[\\s*" + sqlName + "\\s*]\\s*\\*/")) {
                                        var markInfo = NavigationGutterIconBuilder.create(XqlIcons.XQL_FILE)
                                                .setTarget(comment)
                                                .setTooltipText(xqlPath.getFileName() + " -> " + sqlName)
                                                .createLineMarkerInfo(javaElement);
                                        result.add(markInfo);
                                        return;
                                    }
                                }
                                if (element instanceof GeneratedParserUtilBase.DummyBlock) {
                                    super.visitElement(element);
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    if (e instanceof ControlFlowException) {
                        throw e;
                    }
                    log.warn(e);
                }
            }
        }
    }
}
