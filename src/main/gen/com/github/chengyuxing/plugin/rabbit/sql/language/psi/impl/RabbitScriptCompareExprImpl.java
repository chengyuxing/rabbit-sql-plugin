// This is a generated file. Not intended for manual editing.
package com.github.chengyuxing.plugin.rabbit.sql.language.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.github.chengyuxing.plugin.rabbit.sql.language.psi.RabbitScriptTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.github.chengyuxing.plugin.rabbit.sql.language.psi.*;

public class RabbitScriptCompareExprImpl extends ASTWrapperPsiElement implements RabbitScriptCompareExpr {

  public RabbitScriptCompareExprImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull RabbitScriptVisitor visitor) {
    visitor.visitCompareExpr(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof RabbitScriptVisitor) accept((RabbitScriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public RabbitScriptValPipeOpt getValPipeOpt() {
    return findNotNullChildByClass(RabbitScriptValPipeOpt.class);
  }

  @Override
  @NotNull
  public RabbitScriptVarPipeExpr getVarPipeExpr() {
    return findNotNullChildByClass(RabbitScriptVarPipeExpr.class);
  }

}
