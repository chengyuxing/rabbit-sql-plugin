package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.github.chengyuxing.common.util.StringUtils;
import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.plugins.FeatureChecker;
import com.github.chengyuxing.plugin.rabbit.sql.plugins.java.JavaUtil;
import com.github.chengyuxing.plugin.rabbit.sql.plugins.kotlin.KotlinUtil;
import com.github.chengyuxing.sql.annotation.*;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;

public class PsiUtil {
    public static final String MAPPER_SCAN_FQN = "com.github.chengyuxing.sql.spring.autoconfigure.mapping.XQLMapperScan";
    public static final String MAPPER_SCAN_ANNO_DISPLAY = "@XQLMapperScan";
    public static final String MAPPER_ANNO_DISPLAY = "@" + XQLMapper.class.getSimpleName();

    public static void navigate2xqlFile(String alias, String name, XQLConfigManager.Config config) {
        var xqlVf = ProjectFileUtil.findXqlByAlias(alias, config);
        if (Objects.nonNull(xqlVf) && xqlVf.exists()) {
            ApplicationManager.getApplication().runReadAction(() -> {
                var psi = PsiManager.getInstance(config.getProject()).findFile(xqlVf);
                navigate2xqlFile(psi, name);
            });
        }
    }

    public static void navigate2xqlFile(PsiElement psi, String sqlFragmentName) {
        if (Objects.nonNull(psi)) {
            ApplicationManager.getApplication().runReadAction(() -> {
                var comments = PsiTreeUtil.findChildrenOfType(psi, PsiComment.class);
                for (PsiComment comment : comments) {
                    if (StringUtil.isCommentSqlName(sqlFragmentName, comment.getText())) {
                        var target = comment.getNavigationElement();
                        NavigationUtil.activateFileWithPsiElement(target);
                        break;
                    }
                }
            });
        }
    }

    public static void saveUnsavedXqlAndConfig(@NotNull Project project) {
        var fileDocumentManager = FileDocumentManager.getInstance();
        var unsaved = fileDocumentManager.getUnsavedDocuments();
        for (Document doc : unsaved) {
            var psi = PsiDocumentManager.getInstance(project).getPsiFile(doc);
            if (Objects.nonNull(psi)) {
                var vf = psi.getVirtualFile();
                if (Objects.nonNull(vf) && vf.exists()) {
                    var filename = vf.getName();
                    var ext = vf.getExtension();
                    if (Objects.equals(ext, "xql")) {
                        fileDocumentManager.saveDocument(doc);
                        continue;
                    }
                    if (Constants.CONFIG_PATTERN.matcher(filename).matches()) {
                        fileDocumentManager.saveDocument(doc);
                    }
                }
            }
        }
    }

    public static void reHighlightActiveEditor(Project project) {
        var editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor != null) {
            var psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
            if (psiFile != null && StringUtils.endsWiths(psiFile.getName(), ".java", ".kt")) {
                DaemonCodeAnalyzer.getInstance(project).restart(psiFile);
            }
        }
    }

    public static String getClassName(PsiElement childElement) {
        var clazz = com.intellij.psi.util.PsiUtil.getTopLevelClass(childElement);
        if (clazz != null) {
            return clazz.getQualifiedName();
        }
        return null;
    }

    public static String findMethod(PsiElement inCodeBody) {
        if (inCodeBody == null) {
            return "";
        }
        var codeBlock = com.intellij.psi.util.PsiUtil.getTopLevelEnclosingCodeBlock(inCodeBody, null);
        var clazz = com.intellij.psi.util.PsiUtil.getTopLevelClass(inCodeBody);
        if (clazz != null) {
            var methods = clazz.getAllMethods();
            for (var m : methods) {
                var id = m.getNameIdentifier();
                if (id == null) {
                    return "";
                }
                if (Objects.equals(m.getBody(), codeBlock)) {
                    var params = m.getParameterList();
                    var joiner = new StringJoiner(", ", "(", ")");
                    for (var i = 0; i < params.getParametersCount(); i++) {
                        var p = params.getParameter(i);
                        if (p != null) {
                            var t = p.getType().getPresentableText();
                            joiner.add(t);
                        }
                    }
                    return id.getText() + joiner;
                }
            }
        }
        return "";
    }

    public static String getJvmLangLiteral(PsiElement element) {
        if (FeatureChecker.isPluginEnabled(FeatureChecker.KOTLIN_PLUGIN_ID)) {
            var s = KotlinUtil.getStringLiteral(element);
            if (s != null) {
                return s;
            }
        }
        if (FeatureChecker.isPluginEnabled(FeatureChecker.JAVA_PLUGIN_ID)) {
            return JavaUtil.getStringLiteral(element);
        }
        return null;
    }

    public static VirtualFile getActiveFile(Project project) {
        var editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor != null) {
            var pf = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
            if (Objects.nonNull(pf)) {
                return pf.getVirtualFile();
            }
        }
        return null;
    }

    public static PsiElement getElementAtCaret(Project project) {
        var editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor != null) {
            var pf = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
            if (Objects.nonNull(pf)) {
                int caretOffset = editor.getCaretModel().getOffset();
                return pf.findElementAt(caretOffset);
            }
        }
        return null;
    }

    public static boolean isParentAXQLMapperInterface(PsiElement chileElement) {
        var psiClass = com.intellij.psi.util.PsiUtil.getTopLevelClass(chileElement);
        if (Objects.isNull(psiClass)) {
            return false;
        }
        if (!psiClass.isInterface()) {
            return false;
        }
        return psiClass.hasAnnotation(XQLMapper.class.getName());
    }

    public static @Nullable String getXQLMapperAlias(PsiClass psiClass) {
        var mapper = psiClass.getAnnotation(XQLMapper.class.getName());
        if (Objects.isNull(mapper)) {
            return null;
        }
        if (!psiClass.isInterface()) {
            return null;
        }
        var psiAnnoAttr = mapper.findAttributeValue("value");
        if (Objects.isNull(psiAnnoAttr)) {
            return null;
        }
        return getAnnoTextValue(psiAnnoAttr);
    }

    public static String getXQLMapperAlias(PsiElement childElement) {
        var psiClass = com.intellij.psi.util.PsiUtil.getTopLevelClass(childElement);
        if (Objects.isNull(psiClass)) {
            return null;
        }
        return getXQLMapperAlias(psiClass);
    }

    public static @Nullable String getAnnoTextValue(PsiAnnotationMemberValue psiAnnoAttr) {
        if (psiAnnoAttr instanceof PsiLiteralExpression literalExpression) {
            if (literalExpression.getValue() instanceof String s) {
                return s;
            }
        }
        var psiAlias = psiAnnoAttr.getText();
        if (psiAlias.length() <= 1) {
            return null;
        }
        psiAlias = psiAlias.substring(1, psiAlias.length() - 1);
        return psiAlias;
    }

    public static PsiAnnotationMemberValue getMethodAnnoValue(PsiIdentifier element, String annoClassName, String attrName) {
        if (element.getParent() instanceof PsiMethod psiMethod) {
            return getMethodAnnoValue(psiMethod, annoClassName, attrName);
        }
        return null;
    }

    public static PsiAnnotationMemberValue getMethodAnnoValue(PsiMethod psiMethod, String annoClassName, String attrName) {
        var methodAnno = psiMethod.getAnnotation(annoClassName);
        if (Objects.nonNull(methodAnno)) {
            var psiMethodAnnoAttr = methodAnno.findAttributeValue(attrName);
            if (Objects.nonNull(psiMethodAnnoAttr)) {
                return psiMethodAnnoAttr;
            }
        }
        return null;
    }

    public static PsiAnnotationMemberValue getIfElementIsAnnotationAttr(PsiElement element, String annotationName, String attrName) {
        var psiAttrValuePair = PsiTreeUtil.getParentOfType(element, PsiNameValuePair.class);
        if (Objects.isNull(psiAttrValuePair)) {
            return null;
        }
        var psiAnnotation = PsiTreeUtil.getParentOfType(psiAttrValuePair, PsiAnnotation.class);
        if (Objects.isNull(psiAnnotation)) {
            return null;
        }
        if (!psiAnnotation.hasQualifiedName(annotationName)) {
            return null;
        }
        if (psiAttrValuePair.getAttributeName().equals(attrName)) {
            return psiAttrValuePair.getValue();
        }
        return null;
    }

    public static boolean isXQLMapperMethod(PsiMethod psiMethod) {
        return !psiMethod.hasAnnotation(Function.class.getName()) &&
                !psiMethod.hasAnnotation(Procedure.class.getName());
    }

    public static boolean isXQLMapperMethodIdentifier(PsiElement element) {
        if (element instanceof PsiIdentifier && element.getParent() instanceof PsiMethod psiMethod) {
            return isXQLMapperMethod(psiMethod);
        }
        return false;
    }

    public static PsiClass getMapperScanPsiClass(PsiSearchHelper helper, GlobalSearchScope scope) {
        AtomicReference<PsiClass> mapperScanPsiClass = new AtomicReference<>(null);
        helper.processElementsWithWord((elem, offset) -> {
            if (elem.getContainingFile() instanceof PsiJavaFile pjf) {
                var psiClass = PsiTreeUtil.getChildOfType(pjf.getOriginalElement(), PsiClass.class);
                if (Objects.nonNull(psiClass) && psiClass.hasAnnotation(MAPPER_SCAN_FQN)) {
                    mapperScanPsiClass.set(psiClass);
                }
            }
            return true;
        }, scope, MAPPER_SCAN_ANNO_DISPLAY, UsageSearchContext.IN_CODE, true);
        return mapperScanPsiClass.get();
    }

    public static Set<PsiClass> getMapperPsiClasses(PsiSearchHelper helper, GlobalSearchScope scope) {
        Set<PsiClass> mapperPsiClasses = new HashSet<>();
        helper.processElementsWithWord((elem, offset) -> {
            if (elem.getContainingFile() instanceof PsiJavaFile pjf) {
                var psiClass = PsiTreeUtil.getChildOfType(pjf.getOriginalElement(), PsiClass.class);
                if (Objects.nonNull(psiClass) && psiClass.hasAnnotation(XQLMapper.class.getName())) {
                    mapperPsiClasses.add(psiClass);
                }
            }
            return true;
        }, scope, MAPPER_ANNO_DISPLAY, UsageSearchContext.IN_CODE, true);
        return mapperPsiClasses;
    }

    public static String[] getMapperScanBasePackages(PsiClass mapperScanClass) {
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
        return basePackages;
    }

    public static boolean isBeanInBasePackage(String[] basePackages, PsiClass psiClass) {
        if (basePackages.length == 0) {
            return true;
        }
        return StringUtils.startsWiths(psiClass.getQualifiedName(), basePackages);
    }

    public static boolean isSpringBootClass(PsiElement element) {
        var psiClass = com.intellij.psi.util.PsiUtil.getTopLevelClass(element);
        if (Objects.isNull(psiClass)) {
            return false;
        }
        return psiClass.hasAnnotation("org.springframework.boot.autoconfigure.SpringBootApplication") ||
                psiClass.hasAnnotation("org.springframework.context.annotation.Configuration") ||
                psiClass.hasAnnotation("org.springframework.boot.autoconfigure.EnableAutoConfiguration") ||
                psiClass.hasAnnotation("org.springframework.stereotype.Component");
    }
}
