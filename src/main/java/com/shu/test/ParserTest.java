package com.shu.test;

import com.shu.lexer.Lexer;
import com.shu.parser.Parser;

/**
 * @author oxotn3
 * @create 2022-05-03
 * @description 实验三
 */
public class ParserTest {
    public static void main(String[] args) {
//        TestParser();
        TestParserSave();
    }


    public static void TestParser() {
        String filepath = "src/main/java/com/shu/assets/b.txt";
        Parser p = new Parser(filepath);
        p.lex();
        p.parse();
    }

    public static void TestParserSave() {
        String filepath = "src/main/java/com/shu/assets/b.txt";
        Lexer lexer = new Lexer(filepath);
        lexer.getSym();

        String savePath = "src/main/java/com/shu/assets/b-result.txt";
        lexer.save(savePath);
    }
}
