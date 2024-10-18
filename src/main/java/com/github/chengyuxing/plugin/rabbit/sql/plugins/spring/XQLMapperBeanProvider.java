package com.github.chengyuxing.plugin.rabbit.sql.plugins.spring;

import com.github.chengyuxing.common.utils.StringUtil;
import com.github.chengyuxing.sql.annotation.XQLMapper;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.spring.model.CommonSpringBean;
import com.intellij.spring.model.SpringImplicitBean;
import com.intellij.spring.model.SpringImplicitBeansProviderBase;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class XQLMapperBeanProvider extends SpringImplicitBeansProviderBase {
    private static final String MAPPER_SCAN_FQN = "com.github.chengyuxing.sql.spring.autoconfigure.mapping.XQLMapperScan";

    @Override
    protected Collection<CommonSpringBean> getImplicitBeans(@NotNull Module module) {
        var moduleJavas = FilenameIndex.getAllFilesByExt(module.getProject(), "java", module.getModuleRuntimeScope(false));
        var mapperScanClassOptional = moduleJavas.stream()
                .map(vf -> PsiManager.getInstance(module.getProject()).findFile(vf))
                .filter(Objects::nonNull)
                .map(psiFile -> {
                    var originalFile = psiFile.getOriginalElement();
                    var psiClass = PsiTreeUtil.getChildOfType(originalFile, PsiClass.class);
                    if (Objects.nonNull(psiClass) && psiClass.hasAnnotation(MAPPER_SCAN_FQN)) {
                        return psiClass;
                    }
                    return null;
                }).filter(Objects::nonNull)
                .findFirst();

        if (mapperScanClassOptional.isEmpty()) {
            return List.of();
        }

        var mapperScanClass = mapperScanClassOptional.get();

        String[] basePackages = new String[0];
        var anno = mapperScanClass.getAnnotation(MAPPER_SCAN_FQN);
        if (Objects.nonNull(anno)) {
            var packagesPsi = anno.findAttributeValue("basePackages");
            if (packagesPsi instanceof PsiLiteralExpression) {
                var psiLiteralExpression = (PsiLiteralExpression) packagesPsi;
                var singlePackage = psiLiteralExpression.getValue();
                if (Objects.nonNull(singlePackage)) {
                    basePackages = new String[]{singlePackage + "."};
                }
            } else {
                basePackages = PsiTreeUtil.findChildrenOfType(packagesPsi, PsiLiteralExpression.class)
                        .stream()
                        .map(PsiLiteralExpression::getValue)
                        .filter(Objects::nonNull)
                        .map(p -> p.toString().trim() + ".")
                        .toArray(String[]::new);
            }
        }

        if (mapperScanClass.hasAnnotation("org.springframework.boot.autoconfigure.SpringBootApplication") ||
                mapperScanClass.hasAnnotation("org.springframework.context.annotation.Configuration") ||
                mapperScanClass.hasAnnotation("org.springframework.boot.autoconfigure.EnableAutoConfiguration") ||
                mapperScanClass.hasAnnotation("org.springframework.stereotype.Component")) {
            final var myBasePackages = basePackages;
            return moduleJavas.stream()
                    .map(vf -> PsiManager.getInstance(module.getProject()).findFile(vf))
                    .filter(Objects::nonNull)
                    .map(psiFile -> {
                        var originalFile = psiFile.getOriginalElement();
                        var psiClass = PsiTreeUtil.getChildOfType(originalFile, PsiClass.class);
                        if (Objects.nonNull(psiClass) && psiClass.hasAnnotation(XQLMapper.class.getName())) {
                            return psiClass;
                        }
                        return null;
                    }).filter(Objects::nonNull)
                    .filter(psiClass -> psiClass.getName() != null && psiClass.getQualifiedName() != null)
                    .filter(psiClass -> isBeanInBasePackage(myBasePackages, psiClass))
                    .map(psiClass -> {
                        var beanName = psiClass.getName();
                        beanName = beanName.substring(0, 1).toLowerCase() + beanName.substring(1);
                        return new SpringImplicitBean(getProviderName(), psiClass, beanName);
                    }).collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public @NotNull String getProviderName() {
        return "Rabbit SQL Springboot";
    }

    private static boolean isBeanInBasePackage(String[] basePackages, PsiClass psiClass) {
        if (basePackages.length == 0) {
            return true;
        }
        return StringUtil.startsWiths(psiClass.getQualifiedName(), basePackages);
    }
}
