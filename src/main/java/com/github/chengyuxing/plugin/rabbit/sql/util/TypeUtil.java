package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.github.chengyuxing.common.tuple.Pair;
import com.github.chengyuxing.common.util.StringUtils;
import com.github.chengyuxing.common.util.ValueUtils;
import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLMapperConfig;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.MapperGenerateForm;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.ClassInfo;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XQLJavaType;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XQLMapperTemplateData;
import com.github.chengyuxing.sql.XQLFileManager;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.github.chengyuxing.plugin.rabbit.sql.common.Constants.FULLY_CLASS_PATTERN;

public class TypeUtil {
    /**
     * Get entity class simple name
     *
     * @param paramTypeOrGenericType fully class name
     * @return simple name if is entity
     */
    public static @Nullable String getUserEntity(String paramTypeOrGenericType) {
        if (MapperGenerateForm.GENERIC_TYPES.contains(paramTypeOrGenericType)) {
            return null;
        }
        if (MapperGenerateForm.PARAM_TYPES.contains(paramTypeOrGenericType)) {
            return null;
        }
        if (FULLY_CLASS_PATTERN.matcher(paramTypeOrGenericType).matches()) {
            int lastDotIdx = paramTypeOrGenericType.lastIndexOf(".");
            return paramTypeOrGenericType.substring(lastDotIdx + 1);
        }
        return null;
    }

    public static String replaceGenericT(String returnType, String genericType) {
        if (returnType.equals(XQLJavaType.GenericT.getValue())) {
            return genericType;
        }
        return returnType.replace(XQLJavaType.GenericT.getValue(), "<" + genericType + ">");
    }

    public static String returnTypeName(String returnType, String genericType) {
        if (returnType.equals(XQLJavaType.GenericT.getValue())) {
            return genericType;
        }
        if (returnType.contains("<")) {
            return returnType.substring(0, returnType.indexOf("<"));
        }
        return returnType;
    }

    public static boolean isPageReturnType(XQLMapperTemplateData.Method method) {
        return StringUtils.startsWiths(method.getReturnType(), XQLJavaType.PagedResource.getValue() + "<",
                XQLJavaType.PagedResource.getValue() + " ",
                XQLJavaType.IPageable.getValue() + " ");
    }

    public static String detectCountQuery(String sqlName, XQLMapperTemplateData.Method method, XQLFileManager.Resource resource) {
        if (TypeUtil.isPageReturnType(method)) {
            var entry = resource.getEntry();
            String[] ends = new String[]{
                    "Count", "count", "-count", "_count"
            };
            for (String end : ends) {
                var cqName = sqlName + end;
                if (entry.containsKey(cqName)) {
                    return cqName;
                }
            }
        }
        return null;
    }

    public static String getPageConfig(XQLMapperConfig.PageableConfigProps pageableConfig, XQLMapperTemplateData.Method method) {
        if (isPageReturnType(method) && !pageableConfig.isEmpty()) {
            return pageableConfig.formatToTemplate();
        }
        return null;
    }

    public static Pair<Set<XQLMapperTemplateData.Parameter>, Set<String>> collectParams(Set<String> paramNames, Map<String, XQLMapperConfig.XQLParam> paramObjects, XQLMapperTemplateData.Method method, XQLMapperConfig config) {
        var newParams = new LinkedHashSet<XQLMapperTemplateData.Parameter>();
        var imports = new LinkedHashSet<String>();
        if (paramObjects.isEmpty()) {
            for (var param : paramNames) {
                newParams.add(new XQLMapperTemplateData.Parameter(param, Object.class.getSimpleName(), ""));
            }
        } else {
            for (var paramName : paramNames) {
                var paramObj = paramObjects.get(paramName);
                if (Objects.isNull(paramObj)) {
                    newParams.add(new XQLMapperTemplateData.Parameter(paramName, Object.class.getSimpleName(), ""));
                    continue;
                }
                if (Objects.equals(paramObj.getRequired(), false)) {
                    continue;
                }

                String shortType = Object.class.getSimpleName();

                var type = paramObj.getType();
                if (Objects.nonNull(type)) {
                    shortType = type;
                    if (type.contains(".")) {
                        var typeNameAndPackage = StringUtil.getTypeAndPackagePath(type);
                        shortType = typeNameAndPackage.getItem1();
                        imports.add(typeNameAndPackage.getItem2());
                    }
                }

                newParams.add(new XQLMapperTemplateData.Parameter(
                        paramName,
                        shortType,
                        ValueUtils.coalesce(paramObj.getComment(), ""))
                );
            }
        }

        if (method.getReturnType().equals(XQLJavaType.PagedResource.getValue()) || method.getReturnType().startsWith(XQLJavaType.PagedResource.getValue() + "<")) {
            newParams.add(new XQLMapperTemplateData.Parameter(config.getPageKey(), int.class.getSimpleName(), MessageBundle.message("page.number")));
            newParams.add(new XQLMapperTemplateData.Parameter(config.getSizeKey(), int.class.getSimpleName(), MessageBundle.message("page.size")));
        }
        return Pair.of(newParams, imports);
    }

    public static ClassInfo extractFullClassInfo(String fullyClassName) {
        String packageName = null;
        var className = fullyClassName;
        int dotIdx = className.lastIndexOf('.');
        if (dotIdx != -1) {
            packageName = className.substring(0, dotIdx);
            className = className.substring(dotIdx + 1);
        }
        return new ClassInfo(packageName, className);
    }
}
