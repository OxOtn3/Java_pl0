package com.shu.lexer;

import com.shu.common.Token;
import com.shu.parser.SelectSet;
import sun.jvm.hotspot.debugger.cdbg.Sym;
import sun.text.resources.el.CollationData_el;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author oxotn3
 * @create 2022-03-10
 * @description
 */
public class Symbol {
    public Token id;
    public int[] value; // 用户自定义的标识符值(若有)
    public int num;     // 用户自定义的数(若有)
    public int line;    // 符号所在行

    @Override
    public String toString() {
        if (id.isIdent()) {
            StringBuilder sb = new StringBuilder();
            for (int v: value) {
                sb.append((char)v);
            }
            return "(ident, " + sb.toString() + ")";
        } else if (id.isNumber()) {
            return "(number, " + num + ")";
        }
        return "(" + id.toString() + ", " + id.stringInCode() + ")";
    }

    public Symbol(){}

    public Symbol(Token id) {
        this.id = id;
    }

    public Symbol(Token id, int[] value) {
        this.id = id;
        this.value = value;
    }

    public Symbol(Token id, int line) {
        this.id = id;
        this.line = line;
    }

    public Symbol(Token id, int num, int line) {
        this.id = id;
        this.num = num;
        this.line = line;
    }

    public Symbol(Token id, int[] value, int line) {
        this.id = id;
        this.value = value;
        this.line = line;
    }

    // 返回符号的描述
    public String getDesc() {
        if (id.isIdent()) {
            return "ident";
        } else if (id.isNumber()) {
            return "number";
        } else {
            return id.getDesc();
        }
    }

    // 返回符号的字面量
    public String getLit() {
        if (id.isIdent()) {
            StringBuilder sb = new StringBuilder();
            for (int v: value) {
                sb.append((char)v);
            }
            return sb.toString();
        } else if (id.isNumber()) {
            return String.valueOf(num);
        } else {
            return id.getLit();
        }
    }

}
