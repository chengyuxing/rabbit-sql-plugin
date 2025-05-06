// This is a generated file. Not intended for manual editing.
package com.github.chengyuxing.plugin.rabbit.sql.language.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.github.chengyuxing.plugin.rabbit.sql.language.psi.impl.*;

public interface RabbitScriptTypes {

  IElementType AND_EXPR = new RabbitScriptElementType("AND_EXPR");
  IElementType CASE_BLOCK = new RabbitScriptElementType("CASE_BLOCK");
  IElementType CHOOSE_STMT = new RabbitScriptElementType("CHOOSE_STMT");
  IElementType COMPARE_EXPR = new RabbitScriptElementType("COMPARE_EXPR");
  IElementType DEFAULT_BLOCK = new RabbitScriptElementType("DEFAULT_BLOCK");
  IElementType DEFAULT_CASE_BLOCK = new RabbitScriptElementType("DEFAULT_CASE_BLOCK");
  IElementType ELSE_BLOCK = new RabbitScriptElementType("ELSE_BLOCK");
  IElementType EXPR = new RabbitScriptElementType("EXPR");
  IElementType FI_TAIL = new RabbitScriptElementType("FI_TAIL");
  IElementType FOR_OPTS = new RabbitScriptElementType("FOR_OPTS");
  IElementType FOR_STMT = new RabbitScriptElementType("FOR_STMT");
  IElementType FOR_VARS = new RabbitScriptElementType("FOR_VARS");
  IElementType IF_HEADER = new RabbitScriptElementType("IF_HEADER");
  IElementType IF_STMT = new RabbitScriptElementType("IF_STMT");
  IElementType OR_EXPR = new RabbitScriptElementType("OR_EXPR");
  IElementType PAREN_EXPR = new RabbitScriptElementType("PAREN_EXPR");
  IElementType REL_EXPR = new RabbitScriptElementType("REL_EXPR");
  IElementType SWITCH_STMT = new RabbitScriptElementType("SWITCH_STMT");
  IElementType VALUE_LIST = new RabbitScriptElementType("VALUE_LIST");
  IElementType VAL_PIPE_OPT = new RabbitScriptElementType("VAL_PIPE_OPT");
  IElementType VAR_PIPE_EXPR = new RabbitScriptElementType("VAR_PIPE_EXPR");
  IElementType WHEN_BLOCK = new RabbitScriptElementType("WHEN_BLOCK");

  IElementType AND = new RabbitScriptTokenType("AND");
  IElementType BREAK = new RabbitScriptTokenType("BREAK");
  IElementType CASE = new RabbitScriptTokenType("CASE");
  IElementType CHOOSE = new RabbitScriptTokenType("CHOOSE");
  IElementType CLOSE = new RabbitScriptTokenType("CLOSE");
  IElementType DEFAULT = new RabbitScriptTokenType("DEFAULT");
  IElementType DELIMITER = new RabbitScriptTokenType("DELIMITER");
  IElementType DONE = new RabbitScriptTokenType("DONE");
  IElementType ELSE = new RabbitScriptTokenType("ELSE");
  IElementType END = new RabbitScriptTokenType("END");
  IElementType FI = new RabbitScriptTokenType("FI");
  IElementType FOR = new RabbitScriptTokenType("FOR");
  IElementType IDENTIFIER = new RabbitScriptTokenType("IDENTIFIER");
  IElementType IF = new RabbitScriptTokenType("IF");
  IElementType NEWLINE = new RabbitScriptTokenType("NEWLINE");
  IElementType NOT = new RabbitScriptTokenType("NOT");
  IElementType OF = new RabbitScriptTokenType("OF");
  IElementType OPEN = new RabbitScriptTokenType("OPEN");
  IElementType OR = new RabbitScriptTokenType("OR");
  IElementType PIPE = new RabbitScriptTokenType("PIPE");
  IElementType PLAIN_TEXT = new RabbitScriptTokenType("PLAIN_TEXT");
  IElementType REL_OP = new RabbitScriptTokenType("REL_OP");
  IElementType STRING = new RabbitScriptTokenType("STRING");
  IElementType SWITCH = new RabbitScriptTokenType("SWITCH");
  IElementType VALUE = new RabbitScriptTokenType("VALUE");
  IElementType VARIABLE = new RabbitScriptTokenType("VARIABLE");
  IElementType WHEN = new RabbitScriptTokenType("WHEN");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == AND_EXPR) {
        return new RabbitScriptAndExprImpl(node);
      }
      else if (type == CASE_BLOCK) {
        return new RabbitScriptCaseBlockImpl(node);
      }
      else if (type == CHOOSE_STMT) {
        return new RabbitScriptChooseStmtImpl(node);
      }
      else if (type == COMPARE_EXPR) {
        return new RabbitScriptCompareExprImpl(node);
      }
      else if (type == DEFAULT_BLOCK) {
        return new RabbitScriptDefaultBlockImpl(node);
      }
      else if (type == DEFAULT_CASE_BLOCK) {
        return new RabbitScriptDefaultCaseBlockImpl(node);
      }
      else if (type == ELSE_BLOCK) {
        return new RabbitScriptElseBlockImpl(node);
      }
      else if (type == EXPR) {
        return new RabbitScriptExprImpl(node);
      }
      else if (type == FI_TAIL) {
        return new RabbitScriptFiTailImpl(node);
      }
      else if (type == FOR_OPTS) {
        return new RabbitScriptForOptsImpl(node);
      }
      else if (type == FOR_STMT) {
        return new RabbitScriptForStmtImpl(node);
      }
      else if (type == FOR_VARS) {
        return new RabbitScriptForVarsImpl(node);
      }
      else if (type == IF_HEADER) {
        return new RabbitScriptIfHeaderImpl(node);
      }
      else if (type == IF_STMT) {
        return new RabbitScriptIfStmtImpl(node);
      }
      else if (type == OR_EXPR) {
        return new RabbitScriptOrExprImpl(node);
      }
      else if (type == PAREN_EXPR) {
        return new RabbitScriptParenExprImpl(node);
      }
      else if (type == REL_EXPR) {
        return new RabbitScriptRelExprImpl(node);
      }
      else if (type == SWITCH_STMT) {
        return new RabbitScriptSwitchStmtImpl(node);
      }
      else if (type == VALUE_LIST) {
        return new RabbitScriptValueListImpl(node);
      }
      else if (type == VAL_PIPE_OPT) {
        return new RabbitScriptValPipeOptImpl(node);
      }
      else if (type == VAR_PIPE_EXPR) {
        return new RabbitScriptVarPipeExprImpl(node);
      }
      else if (type == WHEN_BLOCK) {
        return new RabbitScriptWhenBlockImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
