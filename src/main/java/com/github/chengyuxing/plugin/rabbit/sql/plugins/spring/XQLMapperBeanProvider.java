package com.github.chengyuxing.plugin.rabbit.sql.plugins.spring;

import com.github.chengyuxing.plugin.rabbit.sql.util.PsiUtil;
import com.intellij.openapi.diagnostic.ControlFlowException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.psi.*;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.spring.model.CommonSpringBean;
import com.intellij.spring.model.SpringImplicitBean;
import com.intellij.spring.model.SpringImplicitBeansProviderBase;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.chengyuxing.plugin.rabbit.sql.util.PsiUtil.*;

public class XQLMapperBeanProvider extends SpringImplicitBeansProviderBase {
    private static final Logger log = Logger.getInstance(XQLMapperBeanProvider.class);

    @Override
    protected Collection<CommonSpringBean> getImplicitBeans(@NotNull Module module) {
        try {
            PsiSearchHelper helper = PsiSearchHelper.getInstance(module.getProject());

            var mapperScanClass = getMapperScanPsiClass(helper, module.getModuleProductionSourceScope());
            if (mapperScanClass == null) {
                return List.of();
            }

            String[] basePackages = PsiUtil.getMapperScanBasePackages(mapperScanClass);

            if (PsiUtil.isSpringBootClass(mapperScanClass)) {
                Set<PsiClass> mapperPsiClasses = getMapperPsiClasses(helper, module.getModuleProductionSourceScope());
                return mapperPsiClasses.stream()
                        .filter(psiClass -> psiClass.getName() != null && psiClass.getQualifiedName() != null)
                        .filter(psiClass -> isBeanInBasePackage(basePackages, psiClass))
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
}
