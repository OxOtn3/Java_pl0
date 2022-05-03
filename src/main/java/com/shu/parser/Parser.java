package com.shu.parser;

import com.shu.common.Token;
import com.shu.lexer.Lexer;
import com.shu.lexer.Symbol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.shu.parser.Fct.*;

/**
 * @author oxotn3
 * @create 2022-04-23
 * @description
 */
public class Parser {
    public static final int MAX_LEVEL = 3;  // 嵌套的最大层数

    // 符号表中的符号
    private class IdentItem {
        String lit;             // 字面量
        IdentType identType;    // 标识符类型
        int level;              // 所在层级
        int value;              // 无符号整数的值
        int addr;               // 地址 dx
    }

    Lexer lexer;        // 词法分析器
    int dx;             // 地址
    int level;          // 嵌套层数
    IdentItem[] table;  // 符号表
    int tx;
    List<Asm> codes;        //中间代码
    int cidx;
    int curSymbolIndex; // 当前扫描到的symbol下标

    static List<String> parserErrors = new ArrayList<>();

    static {
        String[] errors = {
                /*  0 */ "未定错误",
                /*  1 */ "找到‘:=’，但是期望的是‘=’。",
                /*  2 */ "‘=’后面必须接一个数字。",
                /*  3 */ "标识符后面必须是‘=’。",
                /*  4 */ "在‘const’, ‘var’, 或‘procedure’后面必须是一个标识符。",
                /*  5 */ "缺少‘,’或‘;’。",
                /*  6 */ "过程名错误，未找到该过程，或者连同名的常量变量都没有",
                /*  7 */ "期待一个语句。",
                /*  8 */ "语句后面是错误的符号。",
                /*  9 */ "期待‘.’。",
                /* 10 */ "期待‘.’。",
                /* 11 */ "未声明的标识符。",
                /* 12 */ "非法声明。",
                /* 13 */ "期待赋值号‘:=’。",
                /* 14 */ "在‘call’后面必须接一个标识符(过程)。",
                /* 15 */ "常量和变量(非过程标识符)不能被call调用。",
                /* 16 */ "'then' expected.",
                /* 17 */ "';' or 'end' expected.",
                /* 18 */ "'do' expected.",
                /* 19 */ "Incorrect symbol.",
                /* 20 */ "Relative operators expected.",
                /* 21 */ "Procedure identifier can not be in an expression.",
                /* 22 */ "Missing ')'.",
                /* 23 */ "The symbol can not be followed by a factor.",
                /* 24 */ "The symbol can not be as the beginning of an expression.",
                /* 25 */ "不能改变常量的值",
                /* 26 */ "变量未定义，无法赋值",
                /* 27 */ "call后面应该是一个过程类型的标识符，这里连标识符都不是",
                /* 28 */ "不能输出一个过程",
                /* 29 */ "if后缺失then",
                /* 30 */ "begin后缺失end",
                /* 31 */ "数太大了。",
                /* 32 */ "层次太深了。",
                /* 33 */ "while后缺失do",
                /* 34 */ "read传入了非标识符",
                /* 35 */ "read后缺失左括号",
                /* 36 */ "read后缺失右括号",
                /* 37 */ "缺失分号",
                /* 38 */ "非法变量名",
                /* 39 */ "procedure后必须是一个标识符",
                /* 40 */ "const 等号后不是一个数字"};
        parserErrors = Arrays.asList(errors.clone());
    }

    public Parser(String filepath) {
        lexer = new Lexer(filepath);
        table = new IdentItem[100];
        for (int i = 0; i < table.length; i++) {
            table[i] = new IdentItem();
        }
        codes = new ArrayList<>(100);
    }

    public void error(int t) {
        System.out.printf("Error %d: %s\t on curLine: %d\n", t, parserErrors.get(t), getCurSymbol().line);
    }

    // 获取当前扫描到的符号
    public Symbol getCurSymbol() {
        if (curSymbolIndex > lexer.symbols.size()) {
            return new Symbol();
        }
        return lexer.symbols.get(curSymbolIndex);
    }

    // 扫描下一个符号
    public void goNextSymbol() {
        curSymbolIndex++;
    }

    // 添加符号至符号表
    public void enter(IdentType identType) {
       tx++;
       IdentItem tmp = new IdentItem();
       tmp.lit = getCurSymbol().getLit();
       tmp.level = level;
       tmp.identType = identType;
       tmp.value = getCurSymbol().num;
       tmp.addr = dx;
       if (identType.isVariable()) {
           // 变量 需要在数据栈里留空间
           dx++;
       }
       table[tx] = tmp;
    }

    // 获取标识符在符号表的位置(若有)
    public IdentItem checkInTable(String lit) {
        for (IdentItem identItem :table) {
            if (lit.equals(identItem.lit)) {
                return identItem;
            }
        }
        return null;
    }

    // 生成中间代码
    public void gen(Fct fct, int y, int z) {
        Asm asm = new Asm();
        asm.fct = fct;
        asm.l = y;
        asm.a = z;
        codes.add(asm);
        cidx++;
    }

    // 检查token是否在tokens中
    public boolean tokenIsInTokens(List<Token> toks, Token tok) {
        for (Token t: toks) {
            if (t == tok) {
                return true;
            }
        }
        return false;
    }

    // 检查当前扫描到的符号是不是在select集中
    public void test(List<Token> toks, int t) {
        if (!tokenIsInTokens(toks, getCurSymbol().id)) {
            System.out.printf("出错的当前单词: %s, %s\n", getCurSymbol().getDesc(), getCurSymbol().getLit());
            while (!tokenIsInTokens(toks, getCurSymbol().id) && getCurSymbol().id != Token.PERIODSYM) {
                // 向后扫描
                goNextSymbol();
            }
        }
    }

    // 常量定义 识别const后进入此过程
    // <常量定义> → <标识符>=<无符号整数>
    public void constDeclaration() {
        if (getCurSymbol().id.isIdent()) {
            enter(IdentType.Constant);
            goNextSymbol();
            if (getCurSymbol().id == Token.EQLSYM || getCurSymbol().id == Token.BECOMESSYM) {
                // 等号或赋值号
                if (getCurSymbol().id == Token.BECOMESSYM) {
                    // 容错处理，报错，但是当做等号使用
                    error(0);
                }
                goNextSymbol();
                // 数字就加入符号表
                if (getCurSymbol().id.isNumber()) {
                    goNextSymbol();
                } else {
                    // 等号后不是数字
                    error(40);
                }
            } else {
                // 不是等号或赋值号，非常量声明部分
                error(0);
            }
        } else {
            error(0);
        }
    }

    // 变量声明 识别var后进入此过程 其实就是识别标识符
    public void varDeclaration() {
        if (getCurSymbol().id.isIdent()) {
            enter((IdentType.Variable));
            goNextSymbol();
        } else {
            error(38);
        }
        while (getCurSymbol().id.isComma()) {
            goNextSymbol();
            if (getCurSymbol().id.isIdent()) {
                enter(IdentType.Variable);
                goNextSymbol();
            } else {
                error(38);
            }
        }
    }

    // 因子的产生式
    // <因子> → <标识符>|<无符号整数>|(<表达式>)
    public void factor(List<Token> toks) {
        List<Token> tmp = new ArrayList<>(toks);
        tmp.addAll(SelectSet.factorSelect);
        test(tmp, 24);
        while (tokenIsInTokens(SelectSet.factorSelect, getCurSymbol().id)) {
            switch (getCurSymbol().id) {
                // 标识符
                case IDENTSYM:
                    // 判断标识符是否已经定义 并且生成中间代码
                    IdentItem id = checkInTable(getCurSymbol().getLit());
                    if (id != null) {
                        // 已经定义
                        switch (id.identType) {
                            // 可能是常量或者变量
                            case Constant:
                                gen(Fct.Lit, 0, id.value);
                                break;
                            case Variable:
                                gen(Fct.Lod, level - id.level, id.addr);
                                break;
                            case Proc:
                                // 不允许接受过程
                                error(21);
                        }
                    } else {
                        // 标识符未定义
                        error(11);
                    }
                    goNextSymbol();
                    break;
                // 无符号整数
                case NUMBERSYM:
                    gen(Fct.Lit, 0, getCurSymbol().num);
                    goNextSymbol();
                    break;
                // 左括号
                case LPARENTSYM:
                    goNextSymbol();
                    List<Token> tmp3 = new ArrayList<>(toks);
                    tmp3.add(Token.RPARENTSYM);
                    expression(tmp3);
                    // 判断表达式结束后是不是右括号
                    if (getCurSymbol().id == Token.RPARENTSYM) {
                        goNextSymbol();
                    } else {
                        // 缺少右括号
                        error(22);
                    }
                    break;
            }
            List<Token> tmp2 = new ArrayList<>(toks);
            tmp2.add(Token.LPARENTSYM);
            test(tmp2, 23);
        }
    }

    // 项的产生式
    // <项> → <因子>{<乘除运算符><因子>}
    public void term(List<Token> toks) {
        // <因子>
        List<Token> newToks = new ArrayList<>(toks);
        newToks.add(Token.MULSYM);
        newToks.add(Token.SLASHSYM);
        factor(newToks);
        // {<乘除运算符><因子>}
        while (getCurSymbol().id == Token.MULSYM || getCurSymbol().id == Token.SLASHSYM) {
            Token opt = getCurSymbol().id;
            goNextSymbol();
            factor(newToks);
            if (opt == Token.MULSYM) {
                // 乘法
                gen(Opr, 0, 4);
            } else {
                // 除法
                gen(Opr, 0, 5);
            }
        }
    }

    public void expression(List<Token> toks) {
        Token opt;
        List<Token> newToks = new ArrayList<>(toks);
        newToks.add(Token.PLUSSYM);
        newToks.add(Token.MINUSYM);
        // [+|-]<项>
        if (getCurSymbol().id == Token.PLUSSYM || getCurSymbol().id == Token.MINUSYM) {
            // 可能出现正负号
            opt = getCurSymbol().id;
            goNextSymbol();
            term(newToks);
            if (opt == Token.MINUSYM) {
                gen(Opr, 0, 1);
            }
        } else {
            term(newToks);
        }
        // {<加减运算符><项>}
        while (getCurSymbol().id == Token.PLUSSYM || getCurSymbol().id == Token.MINUSYM) {
            opt = getCurSymbol().id;
            goNextSymbol();
            term(newToks);
            if (opt == Token.PLUSSYM) {
                // 加法
                gen(Opr, 0, 2);
            } else {
                // 减法
                gen(Opr, 0, 3);
            }
        }
    }

    // 条件的产生式
    // <条件> → <表达式><关系运算符><表达式>|odd<表达式>
    public void condition(List<Token> toks) {
        if (getCurSymbol().id == Token.ODDSYM) {
            // odd<表达式>
            goNextSymbol();;
            expression(toks);
            gen(Opr, 0, 6);
        } else {
            // <表达式><关系运算符><表达式>
            List<Token> tmp = new ArrayList<>(toks);
            tmp.addAll(SelectSet.expressionSelect);
            expression(tmp);
            // 是关系运算符
            if (getCurSymbol().id.isRelationOpr()) {
                Token relopt = getCurSymbol().id;
                goNextSymbol();
                expression(toks);
                switch (relopt) {
                    case EQLSYM:
                        // =
                        gen(Opr, 0, 8);
                        break;
                    case NEQSYM:
                        // !=
                        gen(Opr, 0, 9);
                        break;
                    case LESSYM:
                        // <
                        gen(Opr, 0, 10);
                        break;
                    case GTRSYM:
                        // >
                        gen(Opr, 0, 11);
                        break;
                    case LEQSYM:
                        // <=
                        gen(Opr, 0, 12);
                        break;
                    case GEQSYM:
                        // >=
                        gen(Opr, 0, 13);
                        break;
                }
            }
        }
    }

    // 语句的产生式
    // <语句> → <赋值语句>|<条件语句>|<当型循环语句>|<过程调用语句>|<读语句>|<写语句>|<复合语句>|<空>
    // <赋值语句> → <标识符>:=<表达式>
    // <复合语句> → begin<语句>{;<语句>}end
    // <条件语句> → if<条件>then<语句>
    // <过程调用语句> → call<标识符>
    // <当循环语句> → while<条件>do<语句>
    // <读语句> → read(<标识符>{,<标识符>})
    // <写语句> → write(<标识符>{,<标识符>})
    public void statement(List<Token> toks) {
        switch (getCurSymbol().id) {
            case IDENTSYM:
                // <赋值语句> → <标识符>:=<表达式>
                IdentItem idName = checkInTable(getCurSymbol().getLit());
                boolean ok = idName != null;
                if (ok) {
                    if (idName.identType.isConstant()) {
                        // 不能改变常量的值
                        error(25);
                        ok = false;
                    }
                } else {
                    // 变量未定义，不能赋值
                    error(26);
                }
                goNextSymbol();
                // :=
                if (getCurSymbol().id.isBecome()) {
                    goNextSymbol();
                } else {
                    error(13);
                }
                // 表达式
                expression(toks);
                if (ok) {
                    gen(Sto, level - idName.level, idName.addr);
                }
                break;
            case CALLSYM:
                // <过程调用语句> → call<标识符>
                goNextSymbol();
                if (getCurSymbol().id.isIdent()) {
                    IdentItem id = checkInTable(getCurSymbol().getLit());
                    boolean ok2 = id != null;
                    if (ok2) {
                        if (id.identType.isProc()) {
                            gen(Cal, level - id.level, id.addr);
                        } else {
                            // 非过程标识符不可被调用
                            error(15);
                        }
                    } else {
                        // 未找到调用的过程
                        error(6);
                    }
                    goNextSymbol();
                } else {
                    // 不是过程调用语句
                    error(27);
                }
                break;
            case IFSYM:
                // <条件语句> → if<条件>then<语句>
                goNextSymbol();
                // 条件
                List<Token> tmp = new ArrayList<>(toks);
                tmp.add(Token.THENSYM);
                tmp.add(Token.DOSYM);
                condition(tmp);
                if (getCurSymbol().id.isThen()) {
                    goNextSymbol();
                } else {
                    // 未找到then
                    error(29);
                }
                int _cidx = cidx;
                gen(Jpc, 0, 0);
                // 递归语句
                statement(toks);
                codes.get(_cidx).a = cidx;
                break;
            case BEGINSYM:
                // <复合语句> → begin<语句>{;<语句>}end
                goNextSymbol();
                List<Token> newToks = new ArrayList<>(toks);
                newToks.add(Token.SEMICOLONSYM);
                newToks.add(Token.ENDSYM);
                statement(newToks);
                while (getCurSymbol().id.isSemicolon()) {
                    goNextSymbol();
                    statement(newToks);
                }
                if (getCurSymbol().id.isEnd()) {
                    goNextSymbol();
                } else {
                    // 没有结束符号
                    error(30);
                }
                break;
            case WHILESYM:
                // <当循环语句> → while<条件>do<语句>
                // 判断前面，循环体结束后需要跳过来
                int cidx1 = cidx;
                goNextSymbol();
                List<Token> tmp2 = new ArrayList<>(toks);
                tmp2.add(Token.DOSYM);
                condition(tmp2);
                // 退出循环体的地址后面分配好代码后回填
                int cidx2 = cidx;
                gen(Jpc, 0, 0);
                if (getCurSymbol().id.isDo()) {
                    goNextSymbol();
                } else {
                    // 缺少do
                    error(33);
                }
                // 语句
                statement(toks);
                gen(Jmp, 0, cidx1);
                codes.get(cidx2).a = cidx;
                break;
            case READSYM:
                //  <读语句> → read(<标识符>{,<标识符>})
                goNextSymbol();
                // 左括号
                if (getCurSymbol().id.isLparent()) {
                    goNextSymbol();
                    // <标识符>
                    if (getCurSymbol().id.isIdent()) {
                        // 检查变量表 应该是一个已经定义的变量
                        IdentItem id2 = checkInTable(getCurSymbol().getLit());
                        boolean ok2 = id2 != null;
                        if (ok2) {
                            if (!id2.identType.isVariable()) {
                                // 不能改变常量的值
                                error(25);
                                ok2 = false;
                            }
                        } else {
                            // 变量未定义
                            error(26);
                        }
                        if (ok2) {
                            // 读入数字放栈顶
                            gen(Opr, 0, 14);
                            // 读入数字放栈顶
                            gen(Sto, level - id2.level, id2.addr);
                        }
                    } else {
                        // 不是标识符
                        error(34);
                    }
                } else {
                    // 缺失左括号
                    error(35);
                }
                goNextSymbol();
                // {,<标识符>}
                while (getCurSymbol().id.isComma()) {
                    goNextSymbol();
                    if (getCurSymbol().id.isIdent()) {
                        // 检查变量表 应该是一个已经定义的变量
                        IdentItem id3 = checkInTable(getCurSymbol().getLit());
                        boolean ok3 = id3 != null;
                        if (ok3) {
                            if (id3.identType.isConstant()) {
                                // 不能改变常量的值
                                error(25);
                                ok3 = false;
                            }
                        } else {
                            // 变量未定义
                            error(26);
                        }
                        if (ok3) {
                            // 读入数字放栈顶
                            gen(Opr, 0, 14);
                            // 从栈顶放到相应位置
                            gen(Sto, level - id3.level, id3.addr);
                        }
                    } else {
                        // 不是标识符
                        error(34);
                    }
                    goNextSymbol();
                }
                if (getCurSymbol().id.isRparent()) {
                    goNextSymbol();
                } else {
                    // 缺失右括号
                    error(36);
                }
                break;
            case WRITESYM:
                // <写语句> → write(<标识符>{，<标识符>})
                goNextSymbol();
                // 左括号
                if (getCurSymbol().id.isLparent()) {
                    goNextSymbol();
                    // <标识符>
                    if (getCurSymbol().id.isIdent()) {
                        // 检查变量表 应该是一个已经定义的变量
                        IdentItem id4 = checkInTable(getCurSymbol().getLit());
                        boolean ok4 = id4 != null;
                        if (ok4) {
                            if (id4.identType.isProc()) {
                                // 不能读过程
                                error(28);
                                ok4 = false;
                            } else if (id4.identType.isConstant()) {
                                //从相应位置读到栈顶
                                gen(Lit, 0, id4.value);
                                //从栈顶显示出来
                                gen(Opr, 0, 15);
                                ok4 = false;
                            }
                        } else {
                            // 变量未定义
                            error(26);
                        }
                        if (ok4) {
                            // 从相应位置读到栈顶
                            gen(Lod, level - id4.level, id4.addr);
                            // 从栈顶显示出来
                            gen(Opr, 0, 15);
                        }
                    } else {
                        // 不是标识符
                        error(34);
                    }
                } else {
                    // 缺失左括号
                    error(35);
                }
                goNextSymbol();
                // {,<标识符>}
                while (getCurSymbol().id.isComma()) {
                    goNextSymbol();
                    if (getCurSymbol().id.isIdent()) {
                        IdentItem id5 = checkInTable(getCurSymbol().getLit());
                        boolean ok5 = id5 != null;
                        if (ok5) {
                            if (id5.identType.isProc()) {
                                // 不能读过程
                                error(28);
                                ok5 = false;
                            } else if (id5.identType.isConstant()){
                                // 从相应位置读到栈顶
                                gen(Lit, 0, id5.value);
                                // 从栈顶显示出来
                                gen(Opr, 0, 15);
                                ok5 = false;
                            }
                        } else {
                            // 变量未定义
                            error(26);
                        }
                        if (ok5) {
                            gen(Lod, level - id5.level, id5.addr);
                            gen(Opr, 0, 15);
                        }
                    } else {
                        // 不是标识符
                        error(34);
                    }
                    goNextSymbol();
                }
                if (getCurSymbol().id.isRparent()) {
                    goNextSymbol();
                } else {
                    // 缺失右括号
                    error(36);
                }
                break;
        }
        test(toks, 19);
    }


    // <程序>→<分程序>.
    // <分程序>→ [<常量说明部分>][<变量说明部分>][<过程说明部分>]〈语句〉
    // <常量说明部分> → CONST<常量定义>{ ,<常量定义>}；
    // <变量说明部分> → VAR<标识符>{ ,<标识符>}；
    // <过和说明部分> → <过程首部><分程度>；{<过程说明部分>}
    // <过程首部> → procedure<标识符>；
    public void block(List<Token> toks) {
        int tx0 = tx;
        table[tx].addr = cidx;
        gen(Jmp, 0, 0);

        if (level > MAX_LEVEL) {
            // 嵌套层次太大
            error(32);
        }
        // 声明部分
        while (true) {
            // 常量说明部分
            if (getCurSymbol().id.isConst()) {
                // <常量说明部分> → CONST<常量定义>{,<常量定义>};
                goNextSymbol();
                while (getCurSymbol().id.isIdent()) {
                    // <常量定义>
                    constDeclaration();
                    // {,<常量定义>}
                    while (getCurSymbol().id.isComma()) {
                        goNextSymbol();
                        constDeclaration();
                    }
                    // 分号
                    if (getCurSymbol().id.isSemicolon()) {
                        goNextSymbol();
                    } else {
                        // 缺少分号
                        error(37);
                    }
                }
            }
            // 变量说明部分
            if (getCurSymbol().id.isVar()) {
                // <变量说明部分> → VAR<标识符>{,<标识符>};
                goNextSymbol();
                while (getCurSymbol().id.isIdent()) {
                    varDeclaration();
                    // 重复
                    while (getCurSymbol().id.isComma()) {
                        goNextSymbol();
                        varDeclaration();
                    }
                    // 分号
                    if (getCurSymbol().id.isSemicolon()) {
                        goNextSymbol();
                    } else {
                        // 缺少分号
                        error(37);
                    }
                }
            }
            // 过程声明部分
            // <过程说明部分> → <过程首部><分程序>;{<过程说明部分>}
            while (getCurSymbol().id.isProc()) {
                // <过程首部> → procedure<标识符>;
                goNextSymbol();
                if (getCurSymbol().id.isIdent()) {
                    enter(IdentType.Proc);
                    goNextSymbol();
                } else {
                    // 非标识符
                    error(39);
                }
                if (getCurSymbol().id.isSemicolon()) {
                    goNextSymbol();
                } else {
                    // 过程首部里面缺少分号
                    error(37);
                }
                // <分程序>
                level++;
                int tx1 = tx;
                int dx1 = dx;
                List<Token> tmp2 = new ArrayList<>(toks);
                tmp2.add(Token.SEMICOLONSYM);
                block(tmp2);
                level--;
                tx = tx1;
                dx = dx1;

                if (getCurSymbol().id.isSemicolon()) {
                    goNextSymbol();
                    List<Token> tmp3 = new ArrayList<>(toks);
                    tmp3.addAll(SelectSet.statementSelect);
                    tmp3.add(Token.IDENTSYM);
                    tmp3.add(Token.PROCSYM);
                    test(tmp3, 6);
                } else {
                    // 缺少分号
                    error(37);
                }
            }
            List<Token> tmp4 = new ArrayList<>();
            tmp4.addAll(SelectSet.statementSelect);
            tmp4.addAll(SelectSet.declareSelect);
            tmp4.add(Token.IDENTSYM);
            test(tmp4, 7);
            if (!tokenIsInTokens(SelectSet.declareSelect, getCurSymbol().id)) {
                break;
            }
        }
        codes.get(table[tx0].addr).a = cidx;
        table[tx0].addr = cidx;
        gen(Int, 0, dx);
        List<Token> tmp5 = new ArrayList<>(toks);
        tmp5.add(Token.SEMICOLONSYM);
        tmp5.add(Token.ENDSYM);
        statement(tmp5);
        gen(Opr, 0, 0);
        test(toks, 8);
    }

    public void lex() {
        lexer.getSym();
    }

    public void parse() {
        List<Token> tmp = new ArrayList<>();
        tmp.addAll(SelectSet.declareSelect);
        tmp.addAll(SelectSet.statementSelect);
        tmp.add(Token.PERIODSYM);
        block(tmp);
        System.out.println("结束");
    }
}
