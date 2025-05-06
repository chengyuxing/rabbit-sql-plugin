// This is a generated file. Not intended for manual editing.
package com.github.chengyuxing.plugin.rabbit.sql.language.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface RabbitScriptDefaultCaseBlock extends PsiElement {

  @NotNull
  List<RabbitScriptChooseStmt> getChooseStmtList();

  @NotNull
  List<RabbitScriptForStmt> getForStmtList();

  @NotNull
  List<RabbitScriptIfStmt> getIfStmtList();

  @NotNull
  List<RabbitScriptSwitchStmt> getSwitchStmtList();

}
