package com.shu.lexer;

import com.shu.common.State;
import com.shu.common.Token;
import com.shu.fp.File;
import com.shu.util.CharUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
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

    private int line;          // 当前所在行号

    public List<Symbol> symbols = new ArrayList<>();  // 符号数组

    public Lexer(String filepath) {
        try {
            this.file = new File(filepath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public char getCh() {
        char ch = 0;
        try {
            ch = (char)file.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (ch == '\n') {
            this.line++;
        }
        ch = CharUtil.toLowerCase(ch);
        return ch;
    }

    public void getSym() {
        int num = 0;
        int numLen = 0;
        char[] cr = new char[MAX_TOKEN_SIZE];
        int charIndex = 0;
        State curState = State.START;
        char ch = getCh();
        boolean isEnd = false;
    LOOP:
        while (!isEnd) {
            switch (curState) {
                case START:
                    if (CharUtil.isSpace(ch)) {

                    } else if (ch == '{') {
                        curState = State.COMMENT;
                    } else if (CharUtil.isDigit(ch)) {
                        curState = State.INNUM;
                        num = num * 10 + (ch - '0');
                        numLen++;
                    } else if (CharUtil.isLetter(ch)) {
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
                        curState = State.START;
                        Token optToken = Token.getOptToken(String.valueOf(ch));
                        if (optToken != BADTOKEN) {
                            symbols.add(new Symbol(optToken));
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
                        curState = State.START;
                        if (numLen > MAX_NUM_SIZE) {
                            System.out.println("数字过长");
                            return;
                        } else {
                            symbols.add(new Symbol(NUMBERSYM, num));
                        }
                        num = 0;
                        numLen = 0;
                        continue LOOP;
                    }
                    break;
                case COMMENT:
                    if (ch == '}') {
                        // 注释结束
                        curState = State.START;
                    }
                    continue;
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
                            symbols.add(new Symbol(idToken, newVal));
                        } else {
                            symbols.add(new Symbol(idToken));
                        }
                        charIndex = 0;
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
                        symbols.add(new Symbol(GEQSYM));
                        continue LOOP;
                    }
                    break;
                case LES:
                    if (ch == '=') {
                        curState = State.LEQ;
                    } else {
                        curState = State.START;
                        symbols.add(new Symbol(LEQSYM));
                        continue LOOP;
                    }
                    break;
                case BECOMES:
                    curState = State.START;
                    symbols.add(new Symbol(BECOMESSYM));
                    continue LOOP;
                case GEQ:
                    curState = State.START;
                    symbols.add(new Symbol(GEQSYM));
                    continue LOOP;
                case LEQ:
                    curState = State.START;
                    symbols.add(new Symbol(LEQSYM));
                    continue LOOP;
            }
            ch = getCh();
            if (ch == -1 || ch == '\uFFFF') isEnd = true;

        }
    }

}
