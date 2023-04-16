package com.github.chengyuxing.plugin.rabbit.sql.psi;

import com.github.chengyuxing.plugin.rabbit.sql.common.ResourceCache;
import com.github.chengyuxing.plugin.rabbit.sql.file.XqlIcons;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

public class XqlNameReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
    private static final Logger log = Logger.getInstance(XqlNameReference.class);
    private final String key;
    private final ResourceCache.Resource resource;

    public XqlNameReference(@NotNull PsiElement element, TextRange rangeInElement) {
        super(element, rangeInElement);
        key = element.getText().substring(rangeInElement.getStartOffset(), rangeInElement.getEndOffset());
        resource = ResourceCache.getInstance().getResource(element);
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        if (key.equals("") || !key.contains(".")) {
            return ResolveResult.EMPTY_ARRAY;
        }
        var dotIdx = key.indexOf(".");
        var alias = key.substring(0, dotIdx).trim();
        var name = key.substring(dotIdx + 1).trim();
        if (alias.equals("") && name.equals("")) {
            return ResolveResult.EMPTY_ARRAY;
        }
        try {
            Project project = myElement.getProject();

            if (resource == null) return ResolveResult.EMPTY_ARRAY;

            var allXqlFiles = resource.getXqlFileManager().allFiles();
            if (allXqlFiles.containsKey(alias)) {
                var xqlFilePath = allXqlFiles.get(alias);
                var xqlPath = Path.of(URI.create(xqlFilePath));
                var vf = VirtualFileManager.getInstance().findFileByNioPath(xqlPath);
                if (vf == null) return ResolveResult.EMPTY_ARRAY;
                var xqlFile = PsiManager.getInstance(project).findFile(vf);
                if (xqlFile == null) return ResolveResult.EMPTY_ARRAY;
                AtomicReference<PsiElement> elem = new AtomicReference<>(null);
                xqlFile.acceptChildren(new PsiElementVisitor() {
                    @Override
                    public void visitComment(@NotNull PsiComment comment) {
                        if (comment.getText().matches("/\\*\\s*\\[\\s*(" + name + ")\\s*]\\s*\\*/")) {
                            elem.set(comment);
                        }
                    }
                });
                if (elem.get() != null) {
                    return new ResolveResult[]{new PsiElementResolveResult(elem.get())};
                }
            }
        } catch (Exception e) {
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
        if (resource == null) {
            return new Object[]{0};
        }
        return resource.getXqlFileManager().names()
                .stream()
                .map(name -> LookupElementBuilder.create(name)
                        .withIcon(XqlIcons.XQL_ITEM)
                        .withTypeText(name.substring(0, name.indexOf(".")) + ".xql")
                        .withCaseSensitivity(false))
                .toArray();
    }
}
