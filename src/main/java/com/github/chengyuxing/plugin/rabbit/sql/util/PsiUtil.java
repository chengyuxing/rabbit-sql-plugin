package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiJavaTokenImpl;
import org.jetbrains.annotations.NotNull;
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

    public static String getJavaLiteral(PsiElement element) {
        if (!(element instanceof PsiJavaTokenImpl) || !(element.getParent() instanceof PsiLiteralExpression literalExpression)) {
            return null;
        }
        return literalExpression.getValue() instanceof String ? (String) literalExpression.getValue() : null;
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
}
