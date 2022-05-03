package com.shu.parser;

/**
 * @author oxotn3
 * @create 2022-04-23
 * @description 标识符类型
 */
public enum IdentType {
    Constant(0, "常量"),
    Variable("变量"),
    Proc("过程");

    public int val;

    public String str;

    IdentType(int val, String str) {
        this.val = val;
        this.str = str;
        Counter.nextVal = val + 1;
    }

    IdentType(String str) {
        this(Counter.nextVal, str);
    }

    @Override
    public String toString() {
        return str;
    }

    public boolean isVariable() {
        return val == Variable.val;
    }

    public boolean isConstant() {
        return val == Constant.val;
    }

    public boolean isProc() {
        return val == Proc.val;
    }


    private static class Counter {
        private static int nextVal = 0;
    }

}
