package com.shu.common;

import java.util.HashMap;
import java.util.Map;

/**
 * @author oxotn3
 * @create 2022-03-10
 * @description 符号枚举,通过static变量实现自增
 */
public enum Token {
    BADTOKEN(0, "无效字符", "无效字符"), // 无效字符

    literal_beg,
    IDENTSYM("变量标识符", "ident"),  // 标识符
    NUMBERSYM("数字", "number"), // 数
    literal_end,

    operator_beg,
    // Operators
    PLUSSYM("+", "plus"),  // +
    MINUSYM("-", "minus"),  // -
    MULSYM("*", "times"),   // *
    SLASHSYM("/", "slash"), // /

    relation_optr_beg,
    EQLSYM("=", "equ"), // =
    NEQSYM("#", "neq"), // #
    LESSYM("<", "lss"), // <
    LEQSYM("<=", "leq"), // <=
    GTRSYM(">", "gtr"), // >
    GEQSYM(">=", "geq"), // >=
    relation_optr_end,

    LPARENTSYM("(", "lparen"),   // (
    RPARENTSYM(")", "rparen"),   // )
    COMMASYM(",", "comma"),     // ,
    SEMICOLONSYM(";", "semicolon"), // ;
    PERIODSYM(".", "period"),    // .
    BECOMESSYM(":=", "becomes"),   // :=
    operator_end,

    keyword_beg,
    // Keywords
    BEGINSYM("begin", "beginsym"), // begin
    ENDSYM("end", "endsym"),   // end
    IFSYM("if", "ifsym"),    // if
    ELSESYM("else", "elsesym"),  // else
    THENSYM("then", "thensym"),  // then
    WHILESYM("while", "whilesym"), // while
    DOSYM("do", "dosym"),    // do
    CALLSYM("call", "callsym"),  // call
    CONSTSYM("const", "constsym"), // const
    VARSYM("var", "varsym"),   // var
    PROCSYM("procedure", "procsym"),  // procedure
    ODDSYM("odd", "oddsym"),   // odd
    WRITESYM("write", "writesym"), // write
    READSYM("read", "readsym"),  // read
    keyword_end,

    EOFSYM("文档已结束", "EOF"); // EOF

    private int val;
    private String str;
    private String desc;

    private static Map<String, Token> tokens;
    private static Map<String, Token> tokenDesc;

    private static Map<String, Token> keywordMap;
    private static Map<String, Token> operatorMap;

    static {
        initTokens();
        initTokenDesc();
        initKwMap();
        initOptMap();
    }

    private static void initTokens() {
        tokens = new HashMap<>();
        for (Token t: Token.values()) {
            tokens.put(t.str, t);
        }
    }

    private static void initTokenDesc() {
        tokenDesc = new HashMap<>();
        for (Token t: Token.values()) {
            tokenDesc.put(t.desc, t);
        }
    }


    private static void initKwMap() {

    }

    private static void initOptMap() {

    }


    Token() {
        this(Counter.nextVal, "", "");
    }

    Token(int val) {
        this(val, "", "");
    }

    Token(String str, String desc) {
        this(Counter.nextVal, str, desc);
    }

    Token(int val, String str, String desc) {
        this.val = val;
        this.str = str;
        this.desc = desc;
        Counter.nextVal = val + 1;
    }

    public int getVal() {
        return val;
    }

    public String getStr() {
        return str;
    }

    public String getDesc() {
        return desc;
    }

    public String getLit() {
        if (val >= 0 && val < tokens.size()) {
            return desc;
        } else {
            return "token(" + val + ")";
        }

    }

    @Override
    public String toString() {
        if (0 <= val && val < Token.values().length) {
            return desc;
        } else {
            return "token(" + val + ")";
        }
    }

    public String stringInCode() {
        if (0 <= val && val < Token.values().length) {
            return str;
        } else {
            return "token(" + val + ")";
        }
    }

    private static class Counter {
        private static int nextVal = 0;
    }

    public boolean isLiteral() {
        return val > literal_beg.val && val < literal_end.val;
    }

    public boolean isIdent() {
        return val == IDENTSYM.getVal();
    }
    public boolean isNumber() {
        return val == NUMBERSYM.getVal();
    }
    public boolean isBecome() {
        return val == BECOMESSYM.getVal();
    }
    public boolean isCall() {
        return val == CALLSYM.getVal();
    }
    public boolean isThen() {
        return val == THENSYM.getVal();
    }
    public boolean isSemicolon() {
        return val == SEMICOLONSYM.getVal();
    }
    public boolean isEnd() {
        return val == ENDSYM.getVal();
    }
    public boolean isDo() {
        return val == DOSYM.getVal();
    }
    public boolean isConst() {
        return val == CONSTSYM.getVal();
    }
    public boolean isComma() {
        return val == COMMASYM.getVal();
    }
    public boolean isVar() {
        return val == VARSYM.getVal();
    }
    public boolean isProc() {
        return val == PROCSYM.getVal();
    }
    public boolean isLparent() {
        return val == LPARENTSYM.getVal();
    }
    public boolean isRparent() {
        return val == RPARENTSYM.getVal();
    }

    public boolean isOperator() {
        return val > operator_beg.val && val < operator_end.val ;
    }

    public boolean isKeyword() {
        return val > keyword_beg.val && val < keyword_end.val;
    }

    public boolean isRelationOpr() {
        return val > relation_optr_beg.val && val < relation_optr_end.val;
    }

    // O(1)
    public static Token getIdToken(String ident) {
        if (tokens.containsKey(ident)) {
            Token t = tokens.get(ident);
            if (t.val > keyword_beg.val && t.val < keyword_end.val) {
                return t;
            }
        }
        return IDENTSYM;
    }

    // O(1)
    public static Token getOptToken(String opt) {
        if (tokens.containsKey(opt)) {
            Token t = tokens.get(opt);
            if (t.val > operator_beg.val && t.val < operator_end.val && t.val != relation_optr_beg.val && t.val != relation_optr_end.val) {
                return t;
            }
        }
        return BADTOKEN;
    }

}
