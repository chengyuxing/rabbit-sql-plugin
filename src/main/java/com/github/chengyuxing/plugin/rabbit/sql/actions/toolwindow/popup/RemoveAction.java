package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.popup;

import com.github.chengyuxing.common.tuple.Quadruple;
import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNodeData;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.SwingUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiDocumentManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

public class RemoveAction extends AnAction {
    private final JTree tree;

    public RemoveAction(JTree tree) {
        super(MessageBundle.message("remove.text"), MessageBundle.message("action.removeXql.description"), AllIcons.Actions.RemoveMulticaret);
        this.tree = tree;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (Objects.isNull(project)) {
            return;
        }
        var nodeSource = SwingUtil.getTreeSelectionNodeUserData(tree);
        if (Objects.isNull(nodeSource)) {
            return;
        }
        if (nodeSource.type() == XqlTreeNodeData.Type.XQL_FILE) {
            removeXQLFile(project, nodeSource);
            return;
        }
        if (nodeSource.type() == XqlTreeNodeData.Type.XQL_FRAGMENT) {
            removeSQLObject(project, nodeSource);
        }
    }

    private void removeXQLFile(Project project, XqlTreeNodeData nodeSource) {
        @SuppressWarnings("unchecked") var data = (Quadruple<String, String, String, XQLConfigManager.Config>) nodeSource.source();
        var alias = data.getItem1();
        var config = data.getItem4();
        int result = Messages.showOkCancelDialog(project,
                "Remove the '" + alias + "' from XQL Configuration?",
                "Remove XQL",
                "Ok",
                "Cancel",
                Messages.getQuestionIcon());
        if (result == Messages.OK) {
            WriteCommandAction.runWriteCommandAction(project, MessageBundle.message("command.modify", config.getConfigName()), null, () -> {
                var doc = ApplicationManager.getApplication().runReadAction((Computable<Document>) () ->
                        FileDocumentManager.getInstance().getDocument(config.getConfigVfs()));
                if (doc != null) {
                    int targetLn = -1;
                    int filesNodeLn = -1;
                    int pipesNodeLn = -1;
                    for (int i = 0; i < doc.getLineCount(); i++) {
                        var line = doc.getText(new TextRange(doc.getLineStartOffset(i), doc.getLineEndOffset(i)));
                        if (line.matches("^files:\\s*")) {
                            filesNodeLn = i;
                            continue;
                        }
                        if (line.startsWith("  " + alias + ":")) {
                            targetLn = i;
                            continue;
                        }
                        if (line.matches("^#?pipes:\\s*")) {
                            pipesNodeLn = i;
                            break;
                        }
                    }
                    if (targetLn != -1 && targetLn > filesNodeLn && targetLn < pipesNodeLn) {
                        var idx = doc.getLineStartOffset(targetLn);
                        doc.insertString(idx, "#");
                        PsiDocumentManager.getInstance(project).commitDocument(doc);
                        FileDocumentManager.getInstance().saveDocument(doc);
                    }
                }
            });
        }
    }

    private void removeSQLObject(Project project, XqlTreeNodeData nodeSource) {
        @SuppressWarnings("unchecked") var sqlMeta = (Quadruple<String, String, XQLFileManager.Sql, XQLConfigManager.Config>) nodeSource.source();
        var alias = sqlMeta.getItem1();
        var sqlName = sqlMeta.getItem2();
        var config = sqlMeta.getItem4();
        int result = Messages.showOkCancelDialog(project,
                "Comment out the SQL '" + sqlName + "' from XQL file?",
                "Comment out SQL",
                "Ok",
                "Cancel",
                Messages.getQuestionIcon());
        if (result == Messages.OK) {
            var path = config.getXqlFileManager().getFiles().get(alias);
            var sqlVf = VirtualFileManager.getInstance().findFileByUrl(path);
            if (sqlVf == null) return;
            WriteCommandAction.runWriteCommandAction(project, MessageBundle.message("command.modify", config.getConfigName()), null, () -> {
                var doc = ApplicationManager.getApplication().runReadAction((Computable<Document>) () ->
                        FileDocumentManager.getInstance().getDocument(sqlVf));
                if (doc != null) {
                    int startLine = -1;
                    for (int i = 0; i < doc.getLineCount(); i++) {
                        var line = doc.getText(new TextRange(doc.getLineStartOffset(i), doc.getLineEndOffset(i)));
                        if (line.contains(sqlName)) {
                            var m = XQLFileManager.KEY_PATTERN.matcher(line);
                            if (m.matches()) {
                                startLine = i;
                                break;
                            }
                        }
                    }
                    if (startLine != -1) {
                        while (startLine < doc.getLineCount()) {
                            int idx = doc.getLineStartOffset(startLine);
                            doc.insertString(idx, "-- ");
                            var line = doc.getText(new TextRange(doc.getLineStartOffset(startLine), doc.getLineEndOffset(startLine)));
                            if (line.trim().endsWith(";")) {
                                break;
                            }
                            startLine++;
                        }
                        PsiDocumentManager.getInstance(project).commitDocument(doc);
                        FileDocumentManager.getInstance().saveDocument(doc);
                    }
                }
            });
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (Objects.isNull(project)) {
            return;
        }
        var nodeSource = SwingUtil.getTreeSelectionNodeUserData(tree);
        if (nodeSource == null) return;
        if (nodeSource.type() == XqlTreeNodeData.Type.XQL_FRAGMENT) {
            @SuppressWarnings("unchecked") var sqlMeta = (Quadruple<String, String, XQLFileManager.Sql, XQLConfigManager.Config>) nodeSource.source();
            var alias = sqlMeta.getItem1();
            var config = sqlMeta.getItem4();
            var path = config.getXqlFileManager().getFiles().get(alias);
            if (!ProjectFileUtil.isLocalFileUri(path)) {
                e.getPresentation().setEnabled(false);
            }
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
