package com.shu.lexer;

import com.shu.common.State;
import com.shu.common.Token;
import com.shu.fp.File;
import com.shu.util.CharUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static com.shu.common.Token.*;

/**
 * @author oxotn3
 * @create 2022-03-10
 * @description
 */
public class Lexer {
    private static final int MAX_TOKEN_SIZE = 10;
    private static final int MAX_NUM_SIZE = 25;

    private File file;

    private int curLine;          // 当前所在行号

    public List<Symbol> symbols = new ArrayList<>();  // 符号数组

    public Lexer(String filepath) {
        try {
            this.file = new File(filepath);
            curLine = 1;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // 读取一个字符
    public char getCh() {
        char ch = 0;
        try {
            ch = (char)file.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (ch == '\n') {
            curLine++;
        }
        ch = CharUtil.toLowerCase(ch);
        return ch;
    }

    // DFA方式获取符号
    public void getSym() {
        // 当前识别的数字
        int num = 0;
        // 当前识别的数字长度
        int numLen = 0;
        // 当前识别的标识符或关键字
        char[] cr = new char[MAX_TOKEN_SIZE];
        // 当前识别的标识符或关键字的索引
        int charIndex = 0;
        // 当前状态
        State curState = State.START;
        char ch = getCh();
        boolean isEnd = false;
    LOOP:
        while (!isEnd) {
            switch (curState) {
                case START:
                    if (CharUtil.isSpace(ch)) {
                        // 空格，啥都不做
                    } else if (ch == '{') {
                        // 注释开头
                        curState = State.COMMENT;
                    } else if (CharUtil.isDigit(ch)) {
                        // 数字开头
                        curState = State.INNUM;
                        num = num * 10 + (ch - '0');
                        numLen++;
                    } else if (CharUtil.isLetter(ch)) {
                        // 标识符开头
                        if (charIndex >= MAX_TOKEN_SIZE) {
                            System.out.println("标识符或关键字过长");
                            return;
                        }
                        curState = State.INID;
                        cr[charIndex] = ch;
                        charIndex++;
                    } else if (ch == '<') {
                        curState = State.LES;
                    } else if (ch == '>') {
                        curState = State.GTR;
                    } else if (ch == ':') {
                        curState = State.INBECOMES;
                    } else {
                        // 单独字符
                        curState = State.START;
                        Token optToken = Token.getOptToken(String.valueOf(ch));
                        if (optToken != BADTOKEN) {
                            symbols.add(new Symbol(optToken, curLine));
                        } else {
                            System.out.println("未知字符: " + ch);
                        }
                    }
                    break;
                case INNUM:
                    if (CharUtil.isDigit(ch)) {
                        num = num * 10 + (ch - '0');
                        numLen++;
                    } else {
                        // 数字结束
                        curState = State.START;
                        if (numLen > MAX_NUM_SIZE) {
                            System.out.println("数字过长");
                            return;
                        } else {
                            symbols.add(new Symbol(NUMBERSYM, num, curLine));
                        }
                        num = 0;
                        numLen = 0;
                        // 暂停对下一个字符的读取
                        continue LOOP;
                    }
                    break;
                case COMMENT:
                    if (ch == '}') {
                        // 注释结束
                        curState = State.START;
                    }
                    break;
                case INID:
                    if (CharUtil.isLetter(ch) || CharUtil.isDigit(ch)) {
                        if (charIndex >= MAX_TOKEN_SIZE) {
                            System.out.println("标识符或关键字过长");
                            return;
                        }
                        cr[charIndex] = ch;
                        charIndex++;
                    } else {
                        // 标识符结束
                        curState = State.START;
                        Token idToken = getIdToken(new String(cr).substring(0, charIndex));
                        if (idToken == IDENTSYM) {
                            int[] newVal = new int[charIndex];
                            for (int i = 0; i < charIndex; i++) {
                                newVal[i] = cr[i];
                            }
                            symbols.add(new Symbol(idToken, newVal, curLine));
                        } else {
                            symbols.add(new Symbol(idToken, curLine));
                        }
                        charIndex = 0;
                        // 暂停对下一个字符的读取
                        continue LOOP;
                    }
                    break;
                case INBECOMES:
                    if (ch == '=') {
                        curState = State.BECOMES;
                    } else {
                        curState = State.START;
                        continue LOOP;
                    }
                    break;
                case GTR:
                    if (ch == '=') {
                        curState = State.GEQ;
                    } else {
                        curState = State.START;
                        symbols.add(new Symbol(GEQSYM, curLine));
                        continue LOOP;
                    }
                    break;
                case LES:
                    if (ch == '=') {
                        curState = State.LEQ;
                    } else {
                        curState = State.START;
                        symbols.add(new Symbol(LEQSYM, curLine));
                        continue LOOP;
                    }
                    break;
                case BECOMES:
                    curState = State.START;
                    symbols.add(new Symbol(BECOMESSYM, curLine));
                    continue LOOP;
                case GEQ:
                    curState = State.START;
                    symbols.add(new Symbol(GEQSYM, curLine));
                    continue LOOP;
                case LEQ:
                    curState = State.START;
                    symbols.add(new Symbol(LEQSYM, curLine));
                    continue LOOP;
            }
            // 读取下一个字符
            ch = getCh();
            if (ch == '\uFFFF') isEnd = true;

        }
    }

    // 保存词法分析结果
    public void save(String path) {
        StringBuilder sb = new StringBuilder();
        for (Symbol sym : symbols) {
            sb.append(sym.toString());
            sb.append("\n");
        }
        try {
            PrintWriter pw = new PrintWriter(path);
            pw.print(sb.toString());
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
