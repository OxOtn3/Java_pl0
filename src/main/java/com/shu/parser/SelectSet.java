package com.shu.parser;

import com.shu.common.Token;
import com.sun.corba.se.impl.oa.toa.TOA;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author oxotn3
 * @create 2022-04-23
 * @description
 */
public class SelectSet {
    public static List<Token> declareSelect;

    public static List<Token> expressionSelect;

    public static List<Token> factorSelect;

    public static List<Token> statementSelect;

    private static void initDeclareSelect() {
        declareSelect = new ArrayList<>();
        declareSelect.add(Token.CONSTSYM);
        declareSelect.add(Token.VARSYM);
        declareSelect.add(Token.PROCSYM);
    }

    private static void initExpressionSelect() {
        expressionSelect = new ArrayList<>();
        expressionSelect.add(Token.EQLSYM);
        expressionSelect.add(Token.NEQSYM);
        expressionSelect.add(Token.LESSYM);
        expressionSelect.add(Token.LEQSYM);
        expressionSelect.add(Token.GTRSYM);
        expressionSelect.add(Token.GEQSYM);
    }

    private static void initFactorSelect() {
        factorSelect = new ArrayList<>();
        factorSelect.add(Token.IDENTSYM);
        factorSelect.add(Token.NUMBERSYM);
        factorSelect.add(Token.LPARENTSYM);
    }

    private static void initStatementSelect() {
        statementSelect = new ArrayList<>();
        statementSelect.add(Token.BEGINSYM);
        statementSelect.add(Token.CALLSYM);
        statementSelect.add(Token.IFSYM);
        statementSelect.add(Token.WHILESYM);
    }

    static {
        initDeclareSelect();
        initExpressionSelect();
        initFactorSelect();
        initStatementSelect();
    }
}
