package com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.popup;

import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.impl.Constant;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.impl.PipeName;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.impl.SqlFragment;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.impl.XqlFile;
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
        if (nodeSource instanceof XqlFile xqlFile) {
            removeXQLFile(project, xqlFile);
            return;
        }
        if (nodeSource instanceof PipeName pipeName) {
            removePipe(project, pipeName);
            return;
        }
        if (nodeSource instanceof SqlFragment sqlFragment) {
            removeSQLObject(project, sqlFragment);
            return;
        }
        if (nodeSource instanceof Constant constant) {
            removeConstant(project, constant);
        }
    }

    private void removeXQLFile(Project project, XqlFile xqlFile) {
        var alias = xqlFile.alias();
        var config = xqlFile.config();
        int result = Messages.showOkCancelDialog(project,
                MessageBundle.message("action.remove.confirm.xql.message", alias),
                MessageBundle.message("action.remove.confirm.xql.title"),
                MessageBundle.message("confirm.ok"),
                MessageBundle.message("confirm.cancel"),
                Messages.getQuestionIcon());
        if (result == Messages.OK) {
            WriteCommandAction.runWriteCommandAction(project, MessageBundle.message("command.modify", config.getConfigName()), null, () -> {
                var doc = ApplicationManager.getApplication().runReadAction((Computable<Document>) () ->
                        FileDocumentManager.getInstance().getDocument(config.getConfigVfs()));
                if (doc != null) {
                    removeSecondNode(doc, "files", alias, () -> {
                        PsiDocumentManager.getInstance(project).commitDocument(doc);
                        FileDocumentManager.getInstance().saveDocument(doc);
                    });
                }
            });
        }
    }

    private void removePipe(Project project, PipeName pipeName) {
        var config = pipeName.config();
        int result = Messages.showOkCancelDialog(project,
                MessageBundle.message("action.remove.confirm.pipe.message", pipeName.name()),
                MessageBundle.message("action.remove.confirm.pipe.title"),
                MessageBundle.message("confirm.ok"),
                MessageBundle.message("confirm.cancel"),
                Messages.getQuestionIcon());
        if (result == Messages.OK) {
            WriteCommandAction.runWriteCommandAction(project, MessageBundle.message("command.modify", config.getConfigName()), null, () -> {
                var doc = ApplicationManager.getApplication().runReadAction((Computable<Document>) () ->
                        FileDocumentManager.getInstance().getDocument(config.getConfigVfs()));
                if (doc != null) {
                    removeSecondNode(doc, "pipes", pipeName.name(), () -> {
                        PsiDocumentManager.getInstance(project).commitDocument(doc);
                        FileDocumentManager.getInstance().saveDocument(doc);
                    });
                }
            });
        }
    }

    private void removeConstant(Project project, Constant constant) {
        var config = constant.config();
        int result = Messages.showOkCancelDialog(project,
                MessageBundle.message("action.remove.confirm.constant.message", constant.name()),
                MessageBundle.message("action.remove.confirm.constant.title"),
                MessageBundle.message("confirm.ok"),
                MessageBundle.message("confirm.cancel"),
                Messages.getQuestionIcon());
        if (result == Messages.OK) {
            WriteCommandAction.runWriteCommandAction(project, MessageBundle.message("command.modify", config.getConfigName()), null, () -> {
                var doc = ApplicationManager.getApplication().runReadAction((Computable<Document>) () ->
                        FileDocumentManager.getInstance().getDocument(config.getConfigVfs()));
                if (doc != null) {
                    removeSecondNode(doc, "constants", constant.name(), () -> {
                        PsiDocumentManager.getInstance(project).commitDocument(doc);
                        FileDocumentManager.getInstance().saveDocument(doc);
                    });
                }
            });
        }
    }

    private void removeSecondNode(Document doc, String root, String secondNode, Runnable then) {
        int startIdx = -1;
        for (int i = 0; i < doc.getLineCount(); i++) {
            var line = doc.getText(new TextRange(doc.getLineStartOffset(i), doc.getLineEndOffset(i)));
            if (line.matches("^" + root + ": *")) {
                startIdx = i;
                break;
            }
        }
        if (startIdx != -1) {
            startIdx++;
            while (startIdx < doc.getLineCount()) {
                var line = doc.getText(new TextRange(doc.getLineStartOffset(startIdx), doc.getLineEndOffset(startIdx)));
                if (line.startsWith("  " + secondNode + ":")) {
                    int idx = doc.getLineStartOffset(startIdx);
                    doc.insertString(idx, "#");
                    then.run();
                    break;
                }
                if (line.matches("^\\w+.*")) {
                    break;
                }
                startIdx++;
            }
        }
    }

    private void removeSQLObject(Project project, SqlFragment sqlFragment) {
        var alias = sqlFragment.xqlAlias();
        var sqlName = sqlFragment.sqlName();
        var config = sqlFragment.config();
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
            WriteCommandAction.runWriteCommandAction(project, MessageBundle.message("command.modify", sqlName), null, () -> {
                var doc = ApplicationManager.getApplication().runReadAction((Computable<Document>) () ->
                        FileDocumentManager.getInstance().getDocument(sqlVf));
                if (doc != null) {
                    int startLine = -1;
                    for (int i = 0; i < doc.getLineCount(); i++) {
                        var line = doc.getText(new TextRange(doc.getLineStartOffset(i), doc.getLineEndOffset(i)));
                        var m = XQLFileManager.KEY_PATTERN.matcher(line);
                        if (m.matches() && Objects.equals(m.group("sqlName"), sqlName)) {
                            startLine = i;
                            break;
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
        if (nodeSource instanceof SqlFragment sqlFragment) {
            var alias = sqlFragment.xqlAlias();
            var config = sqlFragment.config();
            var path = config.getXqlFileManager().getFiles().get(alias);
            if (!ProjectFileUtil.isLocalFileUri(path)) {
                e.getPresentation().setEnabled(false);
            }
            return;
        }
        if (nodeSource instanceof PipeName pipeName) {
            e.getPresentation().setEnabled(!pipeName.builtin());
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
