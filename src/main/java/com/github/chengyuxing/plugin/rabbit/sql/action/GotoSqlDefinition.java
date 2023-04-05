package com.github.chengyuxing.plugin.rabbit.sql.action;

import com.github.chengyuxing.plugin.rabbit.sql.XqlFileListenOnStartup;
import com.github.chengyuxing.plugin.rabbit.sql.common.Store;
import com.github.chengyuxing.plugin.rabbit.sql.lang.XqlIcons;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.impl.source.tree.java.PsiJavaTokenImpl;
import com.intellij.psi.search.PsiShortNamesCache;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Collection;

import static com.github.chengyuxing.plugin.rabbit.sql.common.Constants.SQL_NAME_PATTERN;

public class GotoSqlDefinition extends RelatedItemLineMarkerProvider {
    private static final Logger log = Logger.getInstance(XqlFileListenOnStartup.class);

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        if (!(element instanceof PsiJavaTokenImpl) || !(element.getParent() instanceof PsiLiteralExpression)) {
            return;
        }
        PsiLiteralExpression literalExpression = (PsiLiteralExpression) element.getParent();
        String sqlRef = literalExpression.getValue() instanceof String ? (String) literalExpression.getValue() : null;
        if (sqlRef == null) {
            return;
        }
        if (sqlRef.matches(SQL_NAME_PATTERN)) {
            sqlRef = sqlRef.substring(1);
            if (Store.INSTANCE.xqlFileManager.contains(sqlRef)) {
                var sqlPart = sqlRef.split("\\.");
                var alias = sqlPart[0];
                var sqlName = sqlPart[1];
                try {
                    if (Store.INSTANCE.xqlFileManager.getFiles().containsKey(alias)) {
                        var xqlFilePath = Store.INSTANCE.xqlFileManager.getFiles().get(alias);
                        var xqlFileName = Path.of(xqlFilePath).getFileName().toString();
                        Project project = element.getProject();
                        PsiShortNamesCache shortNamesCache = PsiShortNamesCache.getInstance(project);
                        var files = shortNamesCache.getFilesByName(xqlFileName);
                        if (files.length > 0) {
                            PsiFile xqlFile = files[0];

                            // 考虑下跳转到指定的行号
                            // Command + 鼠标左键 支持跳转
                            var markInfo = NavigationGutterIconBuilder.create(XqlIcons.FILE)
                                    .setTarget(xqlFile)
                                    .setTooltipText(xqlFileName + " -> " + sqlName)
                                    .createLineMarkerInfo(element);
                            result.add(markInfo);
                        }
                    }
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }
    }
}
