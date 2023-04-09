package com.github.chengyuxing.plugin.rabbit.sql.psi;

import com.github.chengyuxing.plugin.rabbit.sql.common.Store;
import com.github.chengyuxing.plugin.rabbit.sql.file.XqlIcons;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.search.PsiShortNamesCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

public class XqlNameReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
    private static final Logger log = Logger.getInstance(XqlNameReference.class);
    private final String key;

    public XqlNameReference(@NotNull PsiElement element, TextRange rangeInElement) {
        super(element, rangeInElement);
        key = element.getText().substring(rangeInElement.getStartOffset(), rangeInElement.getEndOffset());
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
            if (Store.INSTANCE.xqlFileManager.getFiles().containsKey(alias)) {
                var xqlFilePath = Store.INSTANCE.xqlFileManager.getFiles().get(alias);
                var xqlFileName = Path.of(xqlFilePath).getFileName().toString();
                Project project = myElement.getProject();
                PsiShortNamesCache shortNamesCache = PsiShortNamesCache.getInstance(project);
                var files = shortNamesCache.getFilesByName(xqlFileName);
                if (files.length > 0) {
                    PsiFile xqlFile = files[0];
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
        return Store.INSTANCE.xqlFileManager.names()
                .stream()
                .map(name -> LookupElementBuilder.create(name)
                        .withIcon(XqlIcons.XQL_ITEM)
                        .withTypeText(name.substring(0, name.indexOf(".")) + ".xql")
                        .withCaseSensitivity(false))
                .toArray();
    }
}
