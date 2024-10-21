package com.github.chengyuxing.plugin.rabbit.sql.extensions;

import com.github.chengyuxing.common.tuple.Pair;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.file.XqlIcons;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.PsiUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.annotation.CountQuery;
import com.github.chengyuxing.sql.annotation.XQL;
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
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;

import static com.github.chengyuxing.plugin.rabbit.sql.common.Constants.SQL_NAME_PATTERN;

public class GotoXqlDefinition extends RelatedItemLineMarkerProvider {
    private static final Logger log = Logger.getInstance(GotoXqlDefinition.class);

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement sourceElement, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        var sqlRef = handlerSqlRef(sourceElement);
        if (Objects.nonNull(sqlRef)) {
            addLineMarker(sqlRef.getItem1(), sqlRef.getItem2(), result);
        }
        var sqlMapperRef = handlerMapperMethodSqlRef(sourceElement);
        if (Objects.nonNull(sqlMapperRef)) {
            addLineMarker(sqlMapperRef.getItem1(), sqlMapperRef.getItem2(), result);
        }

        var sqlCqRef = handlerMapperCountQuerySqlRef(sourceElement);
        if (Objects.nonNull(sqlCqRef)) {
            addLineMarker(sqlCqRef.getItem1(), sqlCqRef.getItem2(), result);
        }
    }

    private void addLineMarker(String sqlRef, PsiElement sourceElement, Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        if (sqlRef.matches(SQL_NAME_PATTERN)) {
            sqlRef = sqlRef.substring(1);
            var xqlFileManager = XQLConfigManager.getInstance().getActiveXqlFileManager(sourceElement);
            if (Objects.nonNull(xqlFileManager) && xqlFileManager.contains(sqlRef)) {
                var sqlRefParts = StringUtil.extraSqlReference(sqlRef);
                var alias = sqlRefParts.getItem1();
                var sqlName = sqlRefParts.getItem2();
                try {
                    var allXqlFiles = xqlFileManager.getFiles();
                    if (allXqlFiles.containsKey(alias)) {
                        var xqlFilePath = allXqlFiles.get(alias);
                        if (!ProjectFileUtil.isLocalFileUri(xqlFilePath)) {
                            return;
                        }
                        var xqlPath = Path.of(URI.create(xqlFilePath));
                        var vf = VirtualFileManager.getInstance().findFileByNioPath(xqlPath);
                        if (vf == null || !vf.isValid()) return;
                        Project project = sourceElement.getProject();
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
                                                .createLineMarkerInfo(sourceElement);
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

    protected Pair<String, PsiElement> handlerMapperCountQuerySqlRef(@NotNull PsiElement sourceElement) {
        var psiAlias = PsiUtil.getXQLMapperAlias(sourceElement);
        if (Objects.isNull(psiAlias)) {
            return null;
        }
        if (!PsiUtil.isXQLMapperMethodIdentifier(sourceElement)) {
            return null;
        }
        var annoAttr = PsiUtil.getMethodAnnoValue((PsiIdentifier) sourceElement, CountQuery.class.getName(), "value");
        if (Objects.nonNull(annoAttr)) {
            var cQAttrValue = PsiUtil.getAnnoTextValue(annoAttr);
            if (!Objects.equals("", cQAttrValue)) {
                return Pair.of("&" + XQLFileManager.encodeSqlReference(psiAlias, cQAttrValue), PsiTreeUtil.findChildOfType(annoAttr, PsiJavaTokenImpl.class));
            }
        }
        return null;
    }

    protected Pair<String, PsiElement> handlerMapperMethodSqlRef(@NotNull PsiElement sourceElement) {
        var psiAlias = PsiUtil.getXQLMapperAlias(sourceElement);
        if (Objects.isNull(psiAlias)) {
            return null;
        }
        if (!PsiUtil.isXQLMapperMethodIdentifier(sourceElement)) {
            return null;
        }
        var psiMethodAnnoAttr = PsiUtil.getMethodAnnoValue((PsiIdentifier) sourceElement, XQL.class.getName(), "value");
        if (Objects.nonNull(psiMethodAnnoAttr)) {
            var attrValue = PsiUtil.getAnnoTextValue(psiMethodAnnoAttr);
            // @XQL(type = Type.insert)
            // int addGuest(DataRow dataRow);
            if (Objects.equals("", attrValue)) {
                return Pair.of("&" + XQLFileManager.encodeSqlReference(psiAlias, sourceElement.getText()), sourceElement);

                // @XQL("queryGuests")
                // Stream<Guest> queryGuests(Map<String, Object> args);
            } else {
                return Pair.of("&" + XQLFileManager.encodeSqlReference(psiAlias, attrValue), PsiTreeUtil.findChildOfType(psiMethodAnnoAttr, PsiJavaTokenImpl.class));
            }

            // List<DataRow> queryGuests(Map<String, Object> args);
        } else {
            return Pair.of("&" + XQLFileManager.encodeSqlReference(psiAlias, sourceElement.getText()), sourceElement);
        }
    }

    protected Pair<String, PsiElement> handlerSqlRef(PsiElement sourceElement) {
        if (!(sourceElement instanceof PsiJavaTokenImpl) || !(sourceElement.getParent() instanceof PsiLiteralExpression literalExpression)) {
            return null;
        }
        return literalExpression.getValue() instanceof String ? Pair.of((String) literalExpression.getValue(), sourceElement) : null;
    }
}
