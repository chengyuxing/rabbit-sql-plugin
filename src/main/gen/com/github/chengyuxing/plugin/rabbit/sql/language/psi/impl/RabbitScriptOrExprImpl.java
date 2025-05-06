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

public class RabbitScriptOrExprImpl extends ASTWrapperPsiElement implements RabbitScriptOrExpr {

  public RabbitScriptOrExprImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull RabbitScriptVisitor visitor) {
    visitor.visitOrExpr(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof RabbitScriptVisitor) accept((RabbitScriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<RabbitScriptAndExpr> getAndExprList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, RabbitScriptAndExpr.class);
  }

}
