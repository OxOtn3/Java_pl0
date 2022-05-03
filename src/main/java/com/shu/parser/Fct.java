package com.shu.parser;

import java.util.HashMap;
import java.util.Map;

/**
 * @author oxotn3
 * @create 2022-04-23
 * @description
 */
public enum Fct {
    Lit(0, "LIT"),
    Opr("OPR"),
    Lod("LOD"),
    Sto("STO"),
    Cal("CAL"),
    Int("INT"),
    Jmp("JMP"),
    Jpc("JPC");

    public int val;

    public String str;

    private static Map<Integer, Fct> fctHash;

    static {
        initFctHash();
    }

    private static void initFctHash() {
        fctHash = new HashMap<>();
        for (Fct fct: Fct.values()) {
            fctHash.put(fct.val, fct);
        }
    }

    Fct(int val, String str) {
        this.val = val;
        this.str = str;
        Counter.nextVal = val + 1;
    }

    Fct(String str) {
        this(Counter.nextVal, str);
    }

    @Override
    public String toString() {
        return str;
    }

    private static class Counter {
        private static int nextVal = 0;
    }


}
