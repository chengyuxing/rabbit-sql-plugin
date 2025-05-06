// This is a generated file. Not intended for manual editing.
package com.github.chengyuxing.plugin.rabbit.sql.language.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface RabbitScriptIfStmt extends PsiElement {

  @NotNull
  List<RabbitScriptChooseStmt> getChooseStmtList();

  @Nullable
  RabbitScriptElseBlock getElseBlock();

  @NotNull
  RabbitScriptFiTail getFiTail();

  @NotNull
  List<RabbitScriptForStmt> getForStmtList();

  @NotNull
  RabbitScriptIfHeader getIfHeader();

  @NotNull
  List<RabbitScriptIfStmt> getIfStmtList();

  @NotNull
  List<RabbitScriptSwitchStmt> getSwitchStmtList();

}
