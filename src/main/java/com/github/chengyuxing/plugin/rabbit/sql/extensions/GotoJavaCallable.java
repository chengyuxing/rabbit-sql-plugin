package com.github.chengyuxing.plugin.rabbit.sql.extensions;

import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.util.PsiUtil;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GotoJavaCallable extends RelatedItemLineMarkerProvider {
    private final static Logger log = Logger.getInstance(GotoJavaCallable.class);

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement xqlPsiElement, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        if (!(xqlPsiElement instanceof PsiComment)) {
            return;
        }
        String sqlNameTag = xqlPsiElement.getText();
        if (sqlNameTag == null) {
            return;
        }
        var pattern = Pattern.compile(Constants.SQL_NAME_ANNOTATION_PATTERN);
        var m = pattern.matcher(sqlNameTag);
        if (m.find()) {
            var sqlName = m.group("name");
            var xqlFile = xqlPsiElement.getContainingFile();
            if (xqlFile != null) {
                if (!xqlFile.isPhysical()) {
                    xqlFile = xqlFile.getOriginalFile();
                }
                var xqlVf = xqlFile.getVirtualFile();
                if (xqlVf == null) {
                    return;
                }
                if (!Objects.equals(xqlVf.getExtension(), "xql")) {
                    return;
                }
                var project = xqlPsiElement.getProject();
                var module = ModuleUtil.findModuleForPsiElement(xqlPsiElement);
                if (module == null) return;

                var xqlFileManager = XQLConfigManager.getInstance().getActiveXqlFileManager(project, xqlPsiElement);
                if (Objects.isNull(xqlFileManager)) return;

                for (Map.Entry<String, String> file : xqlFileManager.getFiles().entrySet()) {
                    if (file.getValue().equals(xqlVf.toNioPath().toUri().toString())) {
                        var sqlPath = file.getKey() + "." + sqlName;
                        if (xqlFileManager.contains(sqlPath)) {
                            final var sqlRef = "&" + sqlPath;
                            try {
                                List<PsiElement> founded = FilenameIndex.getAllFilesByExt(project, "java", GlobalSearchScope.moduleScope(module))
                                        .stream()
                                        .filter(vf -> vf != null && vf.isValid())
                                        .map(vf -> PsiManager.getInstance(project).findFile(vf))
                                        .filter(Objects::nonNull)
                                        .map(psi -> {
                                            final List<PsiElement> psiElements = new ArrayList<>();
                                            psi.accept(new JavaRecursiveElementWalkingVisitor() {
                                                @Override
                                                public void visitLiteralExpression(@NotNull PsiLiteralExpression expression) {
                                                    String v = expression.getValue() instanceof String ? (String) expression.getValue() : null;
                                                    if (v != null && v.equals(sqlRef)) {
                                                        psiElements.add(expression);
                                                    }
                                                    // unnecessary to do that anymore.
                                                    // super.visitElement(expression);
                                                }
                                            });
                                            return psiElements;
                                        }).flatMap(Collection::stream)
                                        .collect(Collectors.toList());

                                if (!founded.isEmpty()) {
                                    var markInfo = NavigationGutterIconBuilder.create(AllIcons.Actions.DiagramDiff)
                                            .setTargets(founded)
                                            .setCellRenderer(() -> new DefaultPsiElementCellRenderer() {
                                                @Override
                                                protected Icon getIcon(PsiElement element) {
                                                    return AllIcons.Nodes.Class;
                                                }

                                                @Override
                                                public String getContainerText(PsiElement element, String name) {
                                                    var className = PsiUtil.getClassName(element);
                                                    if (className != null) {
                                                        var method = PsiUtil.findMethod(element);
                                                        if (!method.isEmpty()) {
                                                            method = "#" + method;
                                                        }
                                                        return className + method;
                                                    }
                                                    return super.getContainerText(element, name);
                                                }
                                            })
                                            .setPopupTitle("Choose reference of sql name \"" + sqlName + "\" (" + founded.size() + " founded)")
                                            .setTooltipText("Where I am (" + founded.size() + " locations)!")
                                            .createLineMarkerInfo(xqlPsiElement);
                                    result.add(markInfo);
                                }
                            } catch (Throwable e) {
                                log.warn(e);
                            }
                        }
                    }
                }
            }
        }
    }
}
