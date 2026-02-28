package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.plugins.FeatureChecker;
import com.github.chengyuxing.plugin.rabbit.sql.plugins.java.JavaUtil;
import com.github.chengyuxing.plugin.rabbit.sql.plugins.kotlin.KotlinUtil;
import com.github.chengyuxing.sql.annotation.*;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.StringJoiner;

public class PsiUtil {
    public static void navigate2xqlFile(String alias, String name, XQLConfigManager.Config config) {
        var xqlVf = ProjectFileUtil.findXqlByAlias(alias, config);
        if (Objects.nonNull(xqlVf) && xqlVf.exists()) {
            var psi = PsiManager.getInstance(config.getProject()).findFile(xqlVf);
            navigate2xqlFile(psi, name);
        }
    }

    public static void navigate2xqlFile(PsiElement psi, String sqlFragmentName) {
        if (Objects.nonNull(psi)) {
            var comments = PsiTreeUtil.findChildrenOfType(psi, PsiComment.class);
            for (PsiComment comment : comments) {
                if (StringUtil.isCommentSqlName(sqlFragmentName, comment.getText())) {
                    var nav = comment.getNavigationElement();
                    NavigationUtil.activateFileWithPsiElement(nav);
                    return;
                }
            }
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
                    if (filename.matches(Constants.CONFIG_PATTERN)) {
                        fileDocumentManager.saveDocument(doc);
                    }
                }
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
}
