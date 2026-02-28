package com.github.chengyuxing.plugin.rabbit.sql.extensions;

import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.file.XqlIcons;
import com.github.chengyuxing.plugin.rabbit.sql.util.ProjectFileUtil;
import com.github.chengyuxing.plugin.rabbit.sql.util.StringUtil;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.diagnostic.ControlFlowException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtilRt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.nio.file.Path;

public class XqlNameReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
    private static final Logger log = Logger.getInstance(XqlNameReference.class);
    protected final String key;
    protected final XQLConfigManager.Config config;

    public XqlNameReference(@NotNull PsiElement element, TextRange rangeInElement, String key) {
        super(element, rangeInElement);
        this.key = key;
        config = XQLConfigManager.getInstance().getActiveConfig(element);
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        if (config == null) {
            return ResolveResult.EMPTY_ARRAY;
        }
        if (key.isEmpty() || !key.contains(".")) {
            return ResolveResult.EMPTY_ARRAY;
        }
        var sqlRefParts = StringUtil.extraSqlReference(key);
        var alias = sqlRefParts.getItem1();
        var name = sqlRefParts.getItem2();
        if (alias.isEmpty() && name.isEmpty()) {
            return ResolveResult.EMPTY_ARRAY;
        }
        try {
            if (myElement.isValid()) {
                Project project = myElement.getProject();

                var allXqlFiles = config.getXqlFileManager().getFiles();
                if (allXqlFiles.containsKey(alias)) {
                    var xqlFilePath = allXqlFiles.get(alias);
                    if (!ProjectFileUtil.isLocalFileUri(xqlFilePath)) {
                        return ResolveResult.EMPTY_ARRAY;
                    }
                    var xqlPath = Path.of(URI.create(xqlFilePath));
                    var vf = VirtualFileManager.getInstance().findFileByNioPath(xqlPath);
                    if (vf == null) return ResolveResult.EMPTY_ARRAY;
                    var xqlFile = PsiManager.getInstance(project).findFile(vf);
                    if (xqlFile == null) return ResolveResult.EMPTY_ARRAY;

                    var comments = PsiTreeUtil.findChildrenOfType(xqlFile, PsiComment.class);
                    for (PsiComment comment : comments) {
                        if (StringUtil.isCommentSqlName(name, comment.getText())) {
                            return new ResolveResult[]{new PsiElementResolveResult(comment)};
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (e instanceof ControlFlowException) {
                throw e;
            }
            log.warn(e);
        }
        return ResolveResult.EMPTY_ARRAY;
    }

    @Override
    public @Nullable PsiElement resolve() {
        var multi = multiResolve(false);
        return multi.length == 1 ? multi[0].getElement() : null;
    }

    @Override
    public Object @NotNull [] getVariants() {
        if (config == null) {
            return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
        }
        var xqlFileManager = config.getXqlFileManager();
        return xqlFileManager.names()
                .stream()
                .map(name -> {
                    var description = xqlFileManager.getSqlObject(name).getDescription();
                    var alias = StringUtil.extraSqlReference(name).getItem1();
                    var resource = xqlFileManager.getResource(alias);
                    var filename = FileResource.getFileName(resource.getFilename(), true);
                    var icon = ProjectFileUtil.isLocalFileUri(resource.getFilename()) ? XqlIcons.XQL_FILE_ITEM : XqlIcons.XQL_FILE_ITEM_REMOTE;
                    return LookupElementBuilder.create(name)
                            .withIcon(icon)
                            .withTypeText(filename)
                            .withTailText(" " + description)
                            .withCaseSensitivity(true);
                }).toArray();
    }
}
