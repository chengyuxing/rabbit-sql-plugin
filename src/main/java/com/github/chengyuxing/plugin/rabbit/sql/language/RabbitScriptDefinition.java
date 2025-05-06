package com.github.chengyuxing.plugin.rabbit.sql.language;

import com.github.chengyuxing.plugin.rabbit.sql.language.psi.RabbitScriptTypes;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

public class RabbitScriptDefinition implements ParserDefinition {
    public static final IFileElementType FILE = new IFileElementType(RabbitScriptLanguage.INSTANCE);

    @Override
    public @NotNull Lexer createLexer(Project project) {
        return new RabbitScriptLexerAdapter();
    }

    @Override
    public @NotNull PsiParser createParser(Project project) {
        return new RabbitScriptParser();
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return FILE;
    }

    @Override
    public @NotNull TokenSet getCommentTokens() {
        return TokenSet.EMPTY;
    }

    @Override
    public @NotNull TokenSet getStringLiteralElements() {
        return TokenSet.EMPTY;
    }

    @Override
    public @NotNull PsiElement createElement(ASTNode astNode) {
        return RabbitScriptTypes.Factory.createElement(astNode);
    }

    @Override
    public @NotNull PsiFile createFile(@NotNull FileViewProvider fileViewProvider) {
        return new RabbitScriptFile(fileViewProvider);
    }
}
