package com.shu.test;

import com.shu.lexer.Lexer;
import com.shu.lexer.Symbol;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author oxotn3
 * @create 2022-03-11
 * @description 简单测试
 */
public class Test {
    public static void test01() throws IOException {
        String filepath = "src/main/java/com/shu/assets/input01.txt";
        Lexer lexer = new Lexer(filepath);
        lexer.getSym();

        Map<String, Integer> res = new HashMap<>();
        for (Symbol s: lexer.symbols) {
            if (s.id.isIdent()) {
                StringBuilder sb = new StringBuilder();
                for (int it: s.value) sb.append((char)it);
                String k = sb.toString();
                res.put(k, res.getOrDefault(k, 0) + 1);
            }
        }
        for (String k: res.keySet()) {
            System.out.println("(" + k + "," + res.get(k) + ")" );
        }
    }

    public static void test02() throws IOException {
        String filepath = "src/main/java/com/shu/assets/input01.txt";
        Lexer lexer = new Lexer(filepath);
        lexer.getSym();
        for (Symbol s: lexer.symbols) {
            System.out.println(s.toString());
        }
    }

    public static void main(String[] args) throws IOException {
        test01();

        test02();
    }
}
