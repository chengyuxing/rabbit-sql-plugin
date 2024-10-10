package com.github.chengyuxing.plugin.rabbit.sql.ui;

import com.github.chengyuxing.plugin.rabbit.sql.common.Global;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.NewXQLForm;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class NewXqlDialog extends DialogWrapper {
    private final static Logger log = Logger.getInstance(NewXqlDialog.class);
    private static final Pattern INVALID_CHAR = Pattern.compile("\\s+|//|\\\\");
    private final Project project;
    private final XQLConfigManager.Config config;
    private final Document doc;
    private final Map<String, String> anchors;
    private String defaultAlias = "";
    private boolean enableAutoGenAlias = true;
    private String templateContent = "";
    private Consumer<PsiElement> whenComplete = psi -> {
    };
    private NewXQLForm newXqlFileForm = null;
    private List<String> pathPrefix = List.of();

    public NewXqlDialog(Project project, XQLConfigManager.Config config, Document doc, Map<String, String> anchors) {
        super(true);
        this.project = project;
        this.config = config;
        this.doc = doc;
        this.anchors = anchors;
        setOKActionEnabled(false);
        setSize(500, 160);
        setTitle("New XQL File");
    }

    public void initContent() {
        this.newXqlFileForm = new NewXQLForm(getAbResourceRoot() + "/" + String.join("/", pathPrefix));
        this.newXqlFileForm.setAnchors(anchors);
        this.newXqlFileForm.setDefaultAlias(defaultAlias);
        this.newXqlFileForm.setAliasEditable(enableAutoGenAlias);
        this.newXqlFileForm.setInputChanged(data -> {
            var alias = data.getItem1();
            var abPath = data.getItem2();
            var inputFileName = data.getItem3();
            if (abPath.isEmpty() || alias.isEmpty()) {
                setOKActionEnabled(false);
                return;
            }
            if (!abPath.endsWith(".xql")) {
                this.newXqlFileForm.alert("File Extension is required.");
                setOKActionEnabled(false);
                return;
            }
            if (inputFileName.startsWith("[") && inputFileName.endsWith("]")) {
                var parts = inputFileName.substring(1, inputFileName.length() - 1).split(",", -1);
                for (var part : parts) {
                    var pt = part.trim();
                    if (pt.isEmpty() || INVALID_CHAR.matcher(pt).find()) {
                        this.newXqlFileForm.alert("Invalid path part founded.");
                        setOKActionEnabled(false);
                        return;
                    }
                    if (pt.startsWith("*")) {
                        var name = pt.substring(1);
                        if (!anchors.containsKey(name)) {
                            this.newXqlFileForm.alert("Cannot find '" + name + "' anchor value.");
                            setOKActionEnabled(false);
                            return;
                        }
                    }
                }
            }
            if (INVALID_CHAR.matcher(alias).find() || INVALID_CHAR.matcher(abPath).find()) {
                this.newXqlFileForm.alert("Invalid character founded.");
                setOKActionEnabled(false);
                return;
            }
            setOKActionEnabled(true);
        });
        this.newXqlFileForm.init();
        init();
    }

    private String getAbResourceRoot() {
        var resourcePath = config.getResourcesRoot();
        var modulePath = config.getModulePath();
        return modulePath.getParent().relativize(resourcePath).toString();
    }

    private String formatUserInputPath(String userInputPath) {
        var trimUserInputPath = userInputPath.trim();
        if (pathPrefix.isEmpty()) {
            if (newXqlFileForm.isYmlListType(trimUserInputPath)) {
                return "!path " + trimUserInputPath;
            }
            return trimUserInputPath;
        }
        var pathArray = new ArrayList<>(pathPrefix);
        if (newXqlFileForm.isYmlListType(trimUserInputPath)) {
            var paths = trimUserInputPath.substring(1, trimUserInputPath.length() - 1).split(",");
            for (int i = 0, j = paths.length; i < j; i++) {
                var path = paths[i].trim();
                if (!path.endsWith(".xql") && i == j - 1) {
                    path += ".xql";
                }
                pathArray.add(path);
            }
            return "!path [ " + String.join(", ", pathArray) + " ]";
        }
        return String.join("/", pathArray) + "/" + trimUserInputPath;
    }

    private String formatAbPath(String abPath) {
        if (pathPrefix.isEmpty()) {
            return abPath;
        }
        return String.join("/", pathPrefix) + "/" + abPath;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return newXqlFileForm;
    }

    @Override
    protected void doOKAction() {
        var data = newXqlFileForm.getData();
        var alias = data.getItem1();
        var userInput = formatUserInputPath(data.getItem2());
        var abPath = formatAbPath(data.getItem3());
        var description = data.getItem4();
        var file = config.getResourcesRoot().resolve(abPath);

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Create xql file.", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    if (config.getXqlFileManagerConfig().getFiles().containsKey(alias)) {
                        newXqlFileForm.alert("Alias '" + alias + "' already configured.");
                        return;
                    }
                    if (Files.exists(file)) {
                        newXqlFileForm.alert("File '" + abPath + "' already exists.");
                        return;
                    }
                    var xqlFt = FileTemplateManager.getInstance(project).getTemplate("XQL File.xql");
                    var path = file.getParent();
                    if (!Files.exists(path)) {
                        Files.createDirectories(path);
                    }
                    var args = Global.usefulArgs()
                            .add("Description", description);
                    var template = xqlFt.getText(args);
                    if (!templateContent.isEmpty()) {
                        template += "\n" + templateContent;
                    }
                    Files.writeString(file, template, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public void onSuccess() {
                ApplicationManager.getApplication().invokeLater(() -> {
                    var newVf = VirtualFileManager.getInstance().refreshAndFindFileByNioPath(file);
                    if (Objects.isNull(newVf)) {
                        return;
                    }
                    var psi = PsiManager.getInstance(project).findFile(newVf);
                    if (Objects.isNull(psi)) {
                        return;
                    }
                    NavigationUtil.activateFileWithPsiElement(psi);
                    whenComplete.accept(psi);

                    ApplicationManager.getApplication().runWriteAction(() ->
                            WriteCommandAction.runWriteCommandAction(project, "Modify '" + config.getConfigName() + "'", null, () -> {
                                int filesNodeIndex = -1;
                                for (int i = 0; i < doc.getLineCount(); i++) {
                                    var line = doc.getText(new TextRange(doc.getLineStartOffset(i), doc.getLineEndOffset(i)));
                                    if (line.trim().equals("files:")) {
                                        filesNodeIndex = doc.getLineEndOffset(i);
                                        break;
                                    }
                                }
                                var content = "  " + alias + ": " + userInput + "\n";
                                if (filesNodeIndex != -1) {
                                    doc.insertString(filesNodeIndex + 1, content);
                                } else {
                                    content = "files: \n" + content;
                                    int start = doc.getTextLength();
                                    if (start != 0) {
                                        content = "\n" + content;
                                    }
                                    doc.insertString(doc.getTextLength(), content);
                                }
                                PsiDocumentManager.getInstance(project).commitDocument(doc);
                                FileDocumentManager.getInstance().saveDocument(doc);
                                // set false for open file after dialog closed.
                                LocalFileSystem.getInstance().refresh(false);
                            }));
                });
            }

            @Override
            public void onThrowable(@NotNull Throwable error) {
                ApplicationManager.getApplication().invokeLater(() -> newXqlFileForm.alert(error.getMessage()));
                log.warn(error);
            }
        });
        dispose();
    }

    public void setDefaultAlias(String defaultAlias) {
        if (Objects.nonNull(defaultAlias))
            this.defaultAlias = defaultAlias;
    }

    public void setEnableAutoGenAlias(boolean enableAutoGenAlias) {
        this.enableAutoGenAlias = enableAutoGenAlias;
    }

    public void setTemplateContent(String templateContent) {
        if (Objects.nonNull(templateContent))
            this.templateContent = templateContent;
    }

    public void setWhenComplete(Consumer<PsiElement> whenComplete) {
        if (Objects.nonNull(whenComplete))
            this.whenComplete = whenComplete;
    }

    public void setPathPrefix(List<String> pathPrefix) {
        this.pathPrefix = pathPrefix;
    }
}
