// This is a generated file. Not intended for manual editing.
package com.github.chengyuxing.plugin.rabbit.sql.language.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface RabbitScriptChooseStmt extends PsiElement {

  @Nullable
  RabbitScriptDefaultBlock getDefaultBlock();

  @NotNull
  List<RabbitScriptWhenBlock> getWhenBlockList();

}
