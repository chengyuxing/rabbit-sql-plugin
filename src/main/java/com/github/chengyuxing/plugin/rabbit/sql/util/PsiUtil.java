package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.sql.annotation.*;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.impl.source.tree.java.PsiJavaTokenImpl;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry;
import org.jetbrains.kotlin.psi.KtStringTemplateExpression;
import org.jetbrains.yaml.psi.YAMLAnchor;
import org.jetbrains.yaml.psi.YamlRecursivePsiElementVisitor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public class PsiUtil {

    public static Map<String, String> getYmlAnchors(Project project, VirtualFile configYml) {
        var anchors = new LinkedHashMap<String, String>();
        if (Objects.isNull(configYml) || !configYml.exists()) {
            return Map.of();
        }
        var ymlPsi = PsiManager.getInstance(project).findFile(configYml);
        if (Objects.isNull(ymlPsi)) {
            return Map.of();
        }
        ProgressManager.checkCanceled();
        ymlPsi.acceptChildren(new YamlRecursivePsiElementVisitor() {
            @Override
            public void visitAnchor(@NotNull YAMLAnchor anchor) {
                var name = anchor.getName();
                var markedValue = anchor.getMarkedValue();
                if (Objects.nonNull(markedValue)) {
                    var value = markedValue.getText().substring(name.length() + 1).trim();
                    anchors.put(name, value);
                }
            }
        });
        return anchors;
    }

    public static void navigate2xqlFile(String alias, String name, XQLConfigManager.Config config) {
        var xqlVf = ProjectFileUtil.findXqlByAlias(alias, config);
        if (Objects.nonNull(xqlVf) && xqlVf.exists()) {
            var psi = PsiManager.getInstance(config.getProject()).findFile(xqlVf);
            navigate2xqlFile(psi, name);
        }
    }

    public static void navigate2xqlFile(PsiElement psi, String sqlFragmentName) {
        if (Objects.nonNull(psi)) {
            ProgressManager.checkCanceled();
            psi.acceptChildren(new PsiRecursiveElementVisitor() {
                @Override
                public void visitElement(@NotNull PsiElement element) {
                    if (element instanceof PsiComment comment) {
                        if (comment.getText().matches("/\\*\\s*\\[\\s*" + sqlFragmentName + "\\s*]\\s*\\*/")) {
                            var nav = comment.getNavigationElement();
                            NavigationUtil.activateFileWithPsiElement(nav);
                            return;
                        }
                    }
                    if (element instanceof GeneratedParserUtilBase.DummyBlock) {
                        super.visitElement(element);
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
        // handle kotlin
        if (element instanceof KtLiteralStringTemplateEntry stringTemplateEntry) {
            return stringTemplateEntry.getText();
        }
        if (element instanceof LeafPsiElement && element.getParent() instanceof KtLiteralStringTemplateEntry stringTemplateEntry) {
            return stringTemplateEntry.getText();
        }
        if (element instanceof KtStringTemplateExpression expression) {
            var text = expression.getText();
            return text.substring(1, text.length() - 1);
        }
        // handle java
        if (element instanceof PsiLiteralExpression literalExpression) {
            return literalExpression.getValue() instanceof String ? (String) literalExpression.getValue() : null;
        }
        if (element instanceof PsiJavaTokenImpl && element.getParent() instanceof PsiLiteralExpression literalExpression) {
            return literalExpression.getValue() instanceof String ? (String) literalExpression.getValue() : null;
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
        var psiClass = PsiTreeUtil.getParentOfType(chileElement, PsiClass.class);
        if (Objects.isNull(psiClass)) {
            return false;
        }
        if (!psiClass.isInterface()) {
            return false;
        }
        return psiClass.hasAnnotation(XQLMapper.class.getName());
    }

    public static String getXQLMapperAlias(PsiClass psiClass) {
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
        var psiClass = PsiTreeUtil.getParentOfType(childElement, PsiClass.class);
        if (Objects.isNull(psiClass)) {
            return null;
        }
        return getXQLMapperAlias(psiClass);
    }

    public static String getAnnoTextValue(PsiAnnotationMemberValue psiAnnoAttr) {
        if (psiAnnoAttr instanceof PsiLiteralExpression literalExpression) {
            if (literalExpression.getValue() instanceof String s) {
                return s;
            }
        }
        var psiAlias = psiAnnoAttr.getText();
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
        return !psiMethod.hasAnnotation(Insert.class.getName()) &&
                !psiMethod.hasAnnotation(Update.class.getName()) &&
                !psiMethod.hasAnnotation(Delete.class.getName()) &&
                !psiMethod.hasAnnotation(Procedure.class.getName());
    }

    public static boolean isXQLMapperMethodIdentifier(PsiElement element) {
        if (element instanceof PsiIdentifier && element.getParent() instanceof PsiMethod psiMethod) {
            return isXQLMapperMethod(psiMethod);
        }
        return false;
    }
}
