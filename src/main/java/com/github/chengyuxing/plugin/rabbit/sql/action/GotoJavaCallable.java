package com.github.chengyuxing.plugin.rabbit.sql.action;

import com.github.chengyuxing.plugin.rabbit.sql.common.Store;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiJavaTokenImpl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GotoJavaCallable extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement xqlPsiElement, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        if (!(xqlPsiElement instanceof PsiComment)) {
            return;
        }
        String sqlNameTag = xqlPsiElement.getText();
        if (sqlNameTag == null) {
            return;
        }
        var pattern = Pattern.compile("/\\*\\s*\\[\\s*(?<name>\\S+)\\s*]\\s*\\*/");
        var m = pattern.matcher(sqlNameTag);
        if (m.find()) {
            var sqlName = m.group("name");
            var xqlFile = xqlPsiElement.getContainingFile();
            if (xqlFile != null) {
                var alias = xqlFile.getVirtualFile().getNameWithoutExtension();
                String sqlPath = alias + "." + sqlName;
                if (Store.INSTANCE.xqlFileManager.contains(sqlPath)) {
                    final var sqlRef = "&" + sqlPath;
                    var project = xqlPsiElement.getProject();
                    List<PsiElement> founded = Store.INSTANCE.projectJavas.stream()
                            .map(p -> VirtualFileManager.getInstance().findFileByNioPath(p))
                            .filter(Objects::nonNull)
                            .map(vf -> PsiManager.getInstance(project).findFile(vf))
                            .filter(Objects::nonNull)
                            .filter(psi -> psi.getText().contains(sqlRef))
                            .map(psi -> {
                                final List<PsiElement> psiElements = new ArrayList<>();
                                psi.accept(new JavaRecursiveElementWalkingVisitor() {
                                    @Override
                                    public void visitElement(@NotNull PsiElement javaPsiElement) {
                                        if (javaPsiElement instanceof PsiJavaTokenImpl && javaPsiElement.getParent() instanceof PsiLiteralExpression) {
                                            var literalExpression = (PsiLiteralExpression) javaPsiElement.getParent();
                                            String v = literalExpression.getValue() instanceof String ? (String) literalExpression.getValue() : null;
                                            if (v != null && v.equals(sqlRef)) {
                                                psiElements.add(javaPsiElement);
                                            }
                                        }
                                        super.visitElement(javaPsiElement);
                                    }
                                });
                                return psiElements;
                            }).flatMap(Collection::stream)
                            .collect(Collectors.toList());

                    if (!founded.isEmpty()) {
                        var markInfo = NavigationGutterIconBuilder.create(AllIcons.Actions.Diff)
                                .setTargets(founded)
                                .setTooltipText("Where I am (" + founded.size() + " locations)!")
                                .createLineMarkerInfo(xqlPsiElement);
                        result.add(markInfo);
                    }
                }
            }
        }
    }
}
