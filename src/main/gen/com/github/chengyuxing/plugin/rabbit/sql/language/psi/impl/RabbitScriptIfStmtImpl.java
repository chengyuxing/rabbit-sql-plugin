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

public class RabbitScriptIfStmtImpl extends ASTWrapperPsiElement implements RabbitScriptIfStmt {

  public RabbitScriptIfStmtImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull RabbitScriptVisitor visitor) {
    visitor.visitIfStmt(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof RabbitScriptVisitor) accept((RabbitScriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<RabbitScriptChooseStmt> getChooseStmtList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, RabbitScriptChooseStmt.class);
  }

  @Override
  @Nullable
  public RabbitScriptElseBlock getElseBlock() {
    return findChildByClass(RabbitScriptElseBlock.class);
  }

  @Override
  @NotNull
  public RabbitScriptFiTail getFiTail() {
    return findNotNullChildByClass(RabbitScriptFiTail.class);
  }

  @Override
  @NotNull
  public List<RabbitScriptForStmt> getForStmtList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, RabbitScriptForStmt.class);
  }

  @Override
  @NotNull
  public RabbitScriptIfHeader getIfHeader() {
    return findNotNullChildByClass(RabbitScriptIfHeader.class);
  }

  @Override
  @NotNull
  public List<RabbitScriptIfStmt> getIfStmtList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, RabbitScriptIfStmt.class);
  }

  @Override
  @NotNull
  public List<RabbitScriptSwitchStmt> getSwitchStmtList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, RabbitScriptSwitchStmt.class);
  }

}
