package com.shu.lexer;

import com.shu.common.Token;
import sun.jvm.hotspot.debugger.cdbg.Sym;

import java.util.Arrays;

/**
 * @author oxotn3
 * @create 2022-03-10
 * @description
 */
public class Symbol {
    public Token id;
    public int[] value; // 用户自定义的标识符值(若有)
    public int num;     // 用户自定义的数(若有)

    @Override
    public String toString() {
        if (id.isIdent()) {
            return "(ident, " + Arrays.toString(value) + ")";
        } else if (id.isNumber()) {
            return "(number, " + num + ")";
        }
        return "(" + id.toString() + ", " + id.stringInCode() + ")";
    }

    public Symbol(Token id) {
        this.id = id;
    }
    public Symbol(Token id, int num) {
        this.id = id;
        this.num = num;
    }

    public Symbol(Token id, int[] value) {
        this.id = id;
        this.value = value;
    }
}
