package com.shu.fp;

import com.shu.lexer.Lexer;
import com.shu.lexer.Symbol;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author oxotn3
 * @create 2022-03-10
 * @description
 */
public class File {
    private FileReader fileReader;

    public File(String filepath) throws FileNotFoundException {
        this.fileReader = new FileReader(filepath);
    }

    public int read() throws IOException {
        int cur = fileReader.read();
        if (cur == -1) fileReader.close();
        return cur;
    }
}
