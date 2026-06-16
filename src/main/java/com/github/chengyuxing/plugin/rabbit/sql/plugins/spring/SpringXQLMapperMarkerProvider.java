package com.github.chengyuxing.plugin.rabbit.sql.plugins.spring;

import com.github.chengyuxing.plugin.rabbit.sql.util.PsiUtil;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.spring.SpringApiIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;

import static com.github.chengyuxing.plugin.rabbit.sql.util.PsiUtil.MAPPER_SCAN_FQN;
import static com.github.chengyuxing.plugin.rabbit.sql.util.PsiUtil.isBeanInBasePackage;

public class SpringXQLMapperMarkerProvider extends RelatedItemLineMarkerProvider {
    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        if (!(element instanceof PsiIdentifier id)) {
            return;
        }
        if (!(id.getParent() instanceof PsiClass mapperScanClass)) {
            return;
        }
        var mapperScanAnno = mapperScanClass.getAnnotation(MAPPER_SCAN_FQN);
        if (mapperScanAnno == null) {
            return;
        }
        var project = element.getProject();
        var module = ModuleUtil.findModuleForFile(element.getContainingFile().getVirtualFile(), project);
        if (module == null) {
            return;
        }
        PsiSearchHelper helper = PsiSearchHelper.getInstance(project);

        String[] basePackages = PsiUtil.getMapperScanBasePackages(mapperScanClass);
        var mappers = PsiUtil.getMapperPsiClasses(helper, GlobalSearchScope.moduleScope(module))
                .stream()
                .filter(psiClass -> psiClass.getName() != null && psiClass.getQualifiedName() != null)
                .filter(psiClass -> isBeanInBasePackage(basePackages, psiClass))
                .toList();
        if (mappers.isEmpty()) {
            return;
        }
        var markInfo = NavigationGutterIconBuilder
                .create(SpringApiIcons.Gutter.SpringScan)
                .setTargets(mappers)
                .setCellRenderer(() -> new DefaultPsiElementCellRenderer() {
                    @Override
                    protected Icon getIcon(PsiElement element) {
                        return AllIcons.Nodes.Interface;
                    }

                    @Override
                    public String getContainerText(PsiElement element, String name) {
                        if (element instanceof PsiClass clazz) {
                            return clazz.getQualifiedName();
                        }
                        return super.getContainerText(element, name);
                    }
                })
                .setTooltipText("Navigate to the Spring XQL mapper bean declaration(s)")
                .createLineMarkerInfo(mapperScanAnno);
        result.add(markInfo);
    }
}
