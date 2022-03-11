package com.shu.common;


/**
 * @author oxotn3
 * @create 2022-03-10
 * @description DFA自动机状态
 */
public enum State {
    START(0),
    INNUM,
    INID, // 标识符或关键字
    INBECOMES,
    BECOMES,
    GTR,
    GEQ,
    LES,
    LEQ,
    END,
    COMMENT;

    private int val;

    State() {
        this(Counter.nextVal);
    }

    State(int val) {
        this.val = val;
        Counter.nextVal = val + 1;
    }

    public int getVal() {
        return val;
    }

    private static class Counter {
        private static int nextVal = 0;
    }

}
