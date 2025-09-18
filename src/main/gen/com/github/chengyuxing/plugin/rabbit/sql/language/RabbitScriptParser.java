// This is a generated file. Not intended for manual editing.
package com.github.chengyuxing.plugin.rabbit.sql.language;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.github.chengyuxing.plugin.rabbit.sql.language.psi.RabbitScriptTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class RabbitScriptParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return file(b, l + 1);
  }

  /* ********************************************************** */
  // rel_expr (AND rel_expr)*
  public static boolean and_expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "and_expr")) return false;
    if (!nextTokenIs(b, "<and expr>", NOT, VARIABLE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, AND_EXPR, "<and expr>");
    r = rel_expr(b, l + 1);
    r = r && and_expr_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (AND rel_expr)*
  private static boolean and_expr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "and_expr_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!and_expr_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "and_expr_1", c)) break;
    }
    return true;
  }

  // AND rel_expr
  private static boolean and_expr_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "and_expr_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, AND);
    r = r && rel_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // CASE value_list NEWLINE statement* BREAK NEWLINE
  public static boolean case_block(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "case_block")) return false;
    if (!nextTokenIs(b, CASE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CASE);
    r = r && value_list(b, l + 1);
    r = r && consumeToken(b, NEWLINE);
    r = r && case_block_3(b, l + 1);
    r = r && consumeTokens(b, 0, BREAK, NEWLINE);
    exit_section_(b, m, CASE_BLOCK, r);
    return r;
  }

  // statement*
  private static boolean case_block_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "case_block_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "case_block_3", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // CHOOSE NEWLINE when_block+ default_block? END NEWLINE
  public static boolean choose_stmt(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "choose_stmt")) return false;
    if (!nextTokenIs(b, CHOOSE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, CHOOSE, NEWLINE);
    r = r && choose_stmt_2(b, l + 1);
    r = r && choose_stmt_3(b, l + 1);
    r = r && consumeTokens(b, 0, END, NEWLINE);
    exit_section_(b, m, CHOOSE_STMT, r);
    return r;
  }

  // when_block+
  private static boolean choose_stmt_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "choose_stmt_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = when_block(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!when_block(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "choose_stmt_2", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // default_block?
  private static boolean choose_stmt_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "choose_stmt_3")) return false;
    default_block(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // var_pipe_expr REL_OP val_pipe_opt
  public static boolean compare_expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compare_expr")) return false;
    if (!nextTokenIs(b, VARIABLE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = var_pipe_expr(b, l + 1);
    r = r && consumeToken(b, REL_OP);
    r = r && val_pipe_opt(b, l + 1);
    exit_section_(b, m, COMPARE_EXPR, r);
    return r;
  }

  /* ********************************************************** */
  // DEFAULT NEWLINE statement* BREAK NEWLINE
  public static boolean default_block(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "default_block")) return false;
    if (!nextTokenIs(b, DEFAULT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, DEFAULT, NEWLINE);
    r = r && default_block_2(b, l + 1);
    r = r && consumeTokens(b, 0, BREAK, NEWLINE);
    exit_section_(b, m, DEFAULT_BLOCK, r);
    return r;
  }

  // statement*
  private static boolean default_block_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "default_block_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "default_block_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // DEFAULT NEWLINE statement* BREAK NEWLINE
  public static boolean default_case_block(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "default_case_block")) return false;
    if (!nextTokenIs(b, DEFAULT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, DEFAULT, NEWLINE);
    r = r && default_case_block_2(b, l + 1);
    r = r && consumeTokens(b, 0, BREAK, NEWLINE);
    exit_section_(b, m, DEFAULT_CASE_BLOCK, r);
    return r;
  }

  // statement*
  private static boolean default_case_block_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "default_case_block_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "default_case_block_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // ELSE NEWLINE statement* NEWLINE
  public static boolean else_block(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "else_block")) return false;
    if (!nextTokenIs(b, ELSE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, ELSE, NEWLINE);
    r = r && else_block_2(b, l + 1);
    r = r && consumeToken(b, NEWLINE);
    exit_section_(b, m, ELSE_BLOCK, r);
    return r;
  }

  // statement*
  private static boolean else_block_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "else_block_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "else_block_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // or_expr
  public static boolean expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr")) return false;
    if (!nextTokenIs(b, "<expr>", NOT, VARIABLE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPR, "<expr>");
    r = or_expr(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // FI
  public static boolean fi_tail(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fi_tail")) return false;
    if (!nextTokenIs(b, FI)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FI);
    exit_section_(b, m, FI_TAIL, r);
    return r;
  }

  /* ********************************************************** */
  // statement*
  static boolean file(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "file")) return false;
    while (true) {
      int c = current_position_(b);
      if (!statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "file", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // (DELIMITER STRING)? (OPEN STRING)? (CLOSE STRING)?
  public static boolean for_opts(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "for_opts")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FOR_OPTS, "<for opts>");
    r = for_opts_0(b, l + 1);
    r = r && for_opts_1(b, l + 1);
    r = r && for_opts_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (DELIMITER STRING)?
  private static boolean for_opts_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "for_opts_0")) return false;
    for_opts_0_0(b, l + 1);
    return true;
  }

  // DELIMITER STRING
  private static boolean for_opts_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "for_opts_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, DELIMITER, STRING);
    exit_section_(b, m, null, r);
    return r;
  }

  // (OPEN STRING)?
  private static boolean for_opts_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "for_opts_1")) return false;
    for_opts_1_0(b, l + 1);
    return true;
  }

  // OPEN STRING
  private static boolean for_opts_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "for_opts_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, OPEN, STRING);
    exit_section_(b, m, null, r);
    return r;
  }

  // (CLOSE STRING)?
  private static boolean for_opts_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "for_opts_2")) return false;
    for_opts_2_0(b, l + 1);
    return true;
  }

  // CLOSE STRING
  private static boolean for_opts_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "for_opts_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, CLOSE, STRING);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // FOR for_vars OF var_pipe_expr for_opts? NEWLINE statement* DONE NEWLINE
  public static boolean for_stmt(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "for_stmt")) return false;
    if (!nextTokenIs(b, FOR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FOR);
    r = r && for_vars(b, l + 1);
    r = r && consumeToken(b, OF);
    r = r && var_pipe_expr(b, l + 1);
    r = r && for_stmt_4(b, l + 1);
    r = r && consumeToken(b, NEWLINE);
    r = r && for_stmt_6(b, l + 1);
    r = r && consumeTokens(b, 0, DONE, NEWLINE);
    exit_section_(b, m, FOR_STMT, r);
    return r;
  }

  // for_opts?
  private static boolean for_stmt_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "for_stmt_4")) return false;
    for_opts(b, l + 1);
    return true;
  }

  // statement*
  private static boolean for_stmt_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "for_stmt_6")) return false;
    while (true) {
      int c = current_position_(b);
      if (!statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "for_stmt_6", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER (',' IDENTIFIER)?
  public static boolean for_vars(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "for_vars")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    r = r && for_vars_1(b, l + 1);
    exit_section_(b, m, FOR_VARS, r);
    return r;
  }

  // (',' IDENTIFIER)?
  private static boolean for_vars_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "for_vars_1")) return false;
    for_vars_1_0(b, l + 1);
    return true;
  }

  // ',' IDENTIFIER
  private static boolean for_vars_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "for_vars_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ",");
    r = r && consumeToken(b, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // IF expr NEWLINE
  public static boolean if_header(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "if_header")) return false;
    if (!nextTokenIs(b, IF)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IF);
    r = r && expr(b, l + 1);
    r = r && consumeToken(b, NEWLINE);
    exit_section_(b, m, IF_HEADER, r);
    return r;
  }

  /* ********************************************************** */
  // if_header statement* else_block? fi_tail
  public static boolean if_stmt(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "if_stmt")) return false;
    if (!nextTokenIs(b, IF)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = if_header(b, l + 1);
    r = r && if_stmt_1(b, l + 1);
    r = r && if_stmt_2(b, l + 1);
    r = r && fi_tail(b, l + 1);
    exit_section_(b, m, IF_STMT, r);
    return r;
  }

  // statement*
  private static boolean if_stmt_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "if_stmt_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "if_stmt_1", c)) break;
    }
    return true;
  }

  // else_block?
  private static boolean if_stmt_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "if_stmt_2")) return false;
    else_block(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // and_expr (OR and_expr)*
  public static boolean or_expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "or_expr")) return false;
    if (!nextTokenIs(b, "<or expr>", NOT, VARIABLE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, OR_EXPR, "<or expr>");
    r = and_expr(b, l + 1);
    r = r && or_expr_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (OR and_expr)*
  private static boolean or_expr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "or_expr_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!or_expr_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "or_expr_1", c)) break;
    }
    return true;
  }

  // OR and_expr
  private static boolean or_expr_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "or_expr_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OR);
    r = r && and_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '(' expr ')'
  public static boolean paren_expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "paren_expr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PAREN_EXPR, "<paren expr>");
    r = consumeToken(b, "(");
    r = r && expr(b, l + 1);
    r = r && consumeToken(b, ")");
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (NOT paren_expr) | compare_expr
  public static boolean rel_expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rel_expr")) return false;
    if (!nextTokenIs(b, "<rel expr>", NOT, VARIABLE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, REL_EXPR, "<rel expr>");
    r = rel_expr_0(b, l + 1);
    if (!r) r = compare_expr(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // NOT paren_expr
  private static boolean rel_expr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rel_expr_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NOT);
    r = r && paren_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // if_stmt|choose_stmt|switch_stmt|for_stmt|PLAIN_TEXT|NEWLINE
  static boolean statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statement")) return false;
    boolean r;
    r = if_stmt(b, l + 1);
    if (!r) r = choose_stmt(b, l + 1);
    if (!r) r = switch_stmt(b, l + 1);
    if (!r) r = for_stmt(b, l + 1);
    if (!r) r = consumeToken(b, PLAIN_TEXT);
    if (!r) r = consumeToken(b, NEWLINE);
    return r;
  }

  /* ********************************************************** */
  // SWITCH var_pipe_expr NEWLINE case_block+ default_case_block? END NEWLINE
  public static boolean switch_stmt(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switch_stmt")) return false;
    if (!nextTokenIs(b, SWITCH)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SWITCH);
    r = r && var_pipe_expr(b, l + 1);
    r = r && consumeToken(b, NEWLINE);
    r = r && switch_stmt_3(b, l + 1);
    r = r && switch_stmt_4(b, l + 1);
    r = r && consumeTokens(b, 0, END, NEWLINE);
    exit_section_(b, m, SWITCH_STMT, r);
    return r;
  }

  // case_block+
  private static boolean switch_stmt_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switch_stmt_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = case_block(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!case_block(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "switch_stmt_3", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // default_case_block?
  private static boolean switch_stmt_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switch_stmt_4")) return false;
    default_case_block(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // VALUE (PIPE IDENTIFIER)*
  public static boolean val_pipe_opt(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "val_pipe_opt")) return false;
    if (!nextTokenIs(b, VALUE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, VALUE);
    r = r && val_pipe_opt_1(b, l + 1);
    exit_section_(b, m, VAL_PIPE_OPT, r);
    return r;
  }

  // (PIPE IDENTIFIER)*
  private static boolean val_pipe_opt_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "val_pipe_opt_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!val_pipe_opt_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "val_pipe_opt_1", c)) break;
    }
    return true;
  }

  // PIPE IDENTIFIER
  private static boolean val_pipe_opt_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "val_pipe_opt_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, PIPE, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // VALUE (',' VALUE)*
  public static boolean value_list(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "value_list")) return false;
    if (!nextTokenIs(b, VALUE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, VALUE);
    r = r && value_list_1(b, l + 1);
    exit_section_(b, m, VALUE_LIST, r);
    return r;
  }

  // (',' VALUE)*
  private static boolean value_list_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "value_list_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!value_list_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "value_list_1", c)) break;
    }
    return true;
  }

  // ',' VALUE
  private static boolean value_list_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "value_list_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ",");
    r = r && consumeToken(b, VALUE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // VARIABLE (PIPE IDENTIFIER)*
  public static boolean var_pipe_expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "var_pipe_expr")) return false;
    if (!nextTokenIs(b, VARIABLE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, VARIABLE);
    r = r && var_pipe_expr_1(b, l + 1);
    exit_section_(b, m, VAR_PIPE_EXPR, r);
    return r;
  }

  // (PIPE IDENTIFIER)*
  private static boolean var_pipe_expr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "var_pipe_expr_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!var_pipe_expr_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "var_pipe_expr_1", c)) break;
    }
    return true;
  }

  // PIPE IDENTIFIER
  private static boolean var_pipe_expr_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "var_pipe_expr_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, PIPE, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // WHEN expr NEWLINE statement* BREAK NEWLINE
  public static boolean when_block(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "when_block")) return false;
    if (!nextTokenIs(b, WHEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, WHEN);
    r = r && expr(b, l + 1);
    r = r && consumeToken(b, NEWLINE);
    r = r && when_block_3(b, l + 1);
    r = r && consumeTokens(b, 0, BREAK, NEWLINE);
    exit_section_(b, m, WHEN_BLOCK, r);
    return r;
  }

  // statement*
  private static boolean when_block_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "when_block_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "when_block_3", c)) break;
    }
    return true;
  }

}
