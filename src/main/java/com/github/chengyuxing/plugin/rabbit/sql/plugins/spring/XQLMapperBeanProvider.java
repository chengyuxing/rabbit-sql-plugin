package com.github.chengyuxing.plugin.rabbit.sql.plugins.spring;

import com.github.chengyuxing.common.util.StringUtils;
import com.github.chengyuxing.sql.annotation.XQLMapper;
import com.intellij.openapi.diagnostic.ControlFlowException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.psi.*;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.spring.model.CommonSpringBean;
import com.intellij.spring.model.SpringImplicitBean;
import com.intellij.spring.model.SpringImplicitBeansProviderBase;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class XQLMapperBeanProvider extends SpringImplicitBeansProviderBase {
    private static final Logger log = Logger.getInstance(XQLMapperBeanProvider.class);
    private static final String MAPPER_SCAN_FQN = "com.github.chengyuxing.sql.spring.autoconfigure.mapping.XQLMapperScan";
    private static final String MAPPER_SCAN_ANNO_DISPLAY = "@XQLMapperScan";
    private static final String MAPPER_ANNO_DISPLAY = "@" + XQLMapper.class.getSimpleName();

    @Override
    protected Collection<CommonSpringBean> getImplicitBeans(@NotNull Module module) {
        try {
            PsiSearchHelper helper = PsiSearchHelper.getInstance(module.getProject());

            var mapperScanClass = getMapperScanPsiClass(helper,module);
            if (mapperScanClass == null) {
                return List.of();
            }

            String[] basePackages = new String[0];
            var anno = mapperScanClass.getAnnotation(MAPPER_SCAN_FQN);
            if (Objects.nonNull(anno)) {
                var packagesPsi = anno.findAttributeValue("basePackages");
                if (packagesPsi instanceof PsiLiteralExpression psiLiteralExpression) {
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

                Set<PsiClass> mapperPsiClasses = getMapperPsiClasses(helper, module);
                return mapperPsiClasses.stream()
                        .filter(psiClass -> psiClass.getName() != null && psiClass.getQualifiedName() != null)
                        .filter(psiClass -> isBeanInBasePackage(myBasePackages, psiClass))
                        .map(psiClass -> {
                            var beanName = psiClass.getName();
                            beanName = beanName.substring(0, 1).toLowerCase() + beanName.substring(1);
                            return new SpringImplicitBean(getProviderName(), psiClass, beanName);
                        }).collect(Collectors.toList());
            }
            return List.of();
        } catch (Exception e) {
            if (e instanceof ControlFlowException) {
                throw e;
            }
            log.warn(e);
            return List.of();
        }
    }

    @Override
    public @NotNull String getProviderName() {
        return "Rabbit SQL Springboot";
    }

    private static PsiClass getMapperScanPsiClass(PsiSearchHelper helper, Module module) {
        AtomicReference<PsiClass> mapperScanPsiClass = new AtomicReference<>(null);
        helper.processElementsWithWord((elem, offset) -> {
            if (elem.getContainingFile() instanceof PsiJavaFile pjf) {
                var psiClass = PsiTreeUtil.getChildOfType(pjf.getOriginalElement(), PsiClass.class);
                if (Objects.nonNull(psiClass) && psiClass.hasAnnotation(MAPPER_SCAN_FQN)) {
                    mapperScanPsiClass.set(psiClass);
                }
            }
            return true;
        }, module.getModuleProductionSourceScope(), MAPPER_SCAN_ANNO_DISPLAY, UsageSearchContext.IN_CODE, true);
        return mapperScanPsiClass.get();
    }

    private static Set<PsiClass> getMapperPsiClasses(PsiSearchHelper helper, Module module) {
        Set<PsiClass> mapperPsiClasses = new HashSet<>();
        helper.processElementsWithWord((elem, offset) -> {
            if (elem.getContainingFile() instanceof PsiJavaFile pjf) {
                var psiClass = PsiTreeUtil.getChildOfType(pjf.getOriginalElement(), PsiClass.class);
                if (Objects.nonNull(psiClass) && psiClass.hasAnnotation(XQLMapper.class.getName())) {
                    mapperPsiClasses.add(psiClass);
                }
            }
            return true;
        }, module.getModuleProductionSourceScope(), MAPPER_ANNO_DISPLAY, UsageSearchContext.IN_CODE, true);
        return mapperPsiClasses;
    }

    private static boolean isBeanInBasePackage(String[] basePackages, PsiClass psiClass) {
        if (basePackages.length == 0) {
            return true;
        }
        return StringUtils.startsWiths(psiClass.getQualifiedName(), basePackages);
    }
}
