import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Create by NOSAE on 2021/4/11
 */
public class WordAnalyzer {

    private final List<String> keywords = Arrays.asList("if", "else", "while", "do", "break", "true", "false");
    private final List<String> basics = Arrays.asList("int", "float", "bool");
    private final List<Pair<String, String>> result = new ArrayList<>();
    private boolean isBack = false;

    public void analyzeWords(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        int c = ' ';
        while (true) {
            if (!isBack) {
                if ((c = reader.read()) == -1)
                    break;
            } else {
                isBack = false;
            }
            nextChar((char) c);
        }
        nextChar(' ');
    }

    private int state = 0;
    private final StringBuilder wordBuilder = new StringBuilder();
    private void nextChar(char c) {
        boolean isAppend = true;
        switch (state) {
            case 0:
                if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                    isAppend = false;
                    state = 0;
                } else if (c == '<') {
                    state = 1;
                } else if (c == '=') {
                    state = 4;
                } else if (c == '>') {
                    state = 6;
                } else if (c == '!') {
                    state = 8;
                } else {
                    isBack = true;
                    state = 10; // next graph
                }
                break;
            case 1:
                if (c == '=') {
                    state = 2;
                } else {
                    finalCase("lt");
                }
                break;
            case 2:
                finalCase("le"); // less equal
                break;
            case 3:
                finalCase("lt"); // less than
                break;
            case 4:
                if (c == '=') {
                    state = 6;
                } else {
                    finalCase("eq"); // equal
                }
                break;
            case 5:
                finalCase("mov"); // assignment
                break;
            case 6:
                if (c == '=') {
                    state = 7;
                } else {
                    finalCase("gt"); // greater than
                }
                break;
            case 7:
                finalCase("ge"); // greater equal
                break;
            case 8:
                if (c == '=') {
                    state = 9;
                } else {
                    finalCase("not"); // not
                }
                break;
            case 9:
                finalCase("ne"); // not equal
                break;
            case 10:
                if (Character.isLetter(c)) {
                    state = 11;
                } else {
                    isBack = true;
                    state = 12; // next graph
                }
                break;
            case 11:
                if (Character.isLetterOrDigit(c)) {
                    state = 11;
                } else {
                    finalCase("id");
                }
                break;
            case 12:
                if (Character.isDigit(c)) {
                    state = 13;
                } else {
                    isBack = true;
                    state = 19; // next graph
                }
                break;
            case 13:
                if (Character.isDigit(c)) {
                    state = 13;
                } else if (c == '.'){
                    state = 14;
                } else if (c == 'E' || c == 'e') {
                    state = 16;
                } else {
                    finalCase("num");
                }
                break;
            case 14:
                if (Character.isDigit(c)) {
                    state = 15;
                } else {
                    fail("小数点后面缺少数字");
                }
                break;
            case 15:
                if (Character.isDigit(c)) {
                    state = 15;
                } else if (c == 'E' || c == 'e') {
                    state = 16;
                } else {
                    finalCase("real"); // float
                }
                break;
            case 16:
                if (c == '+' || c == '-') {
                    state = 17;
                } else if (Character.isDigit(c)) {
                    state = 18;
                } else {
                    fail("E/e后面缺少值");
                }
                break;
            case 17:
                if (Character.isDigit(c)) {
                    state = 18;
                } else {
                    fail("E/e的符号后面缺少数字");
                }
                break;
            case 18:
                if (Character.isDigit(c)) {
                    state = 18;
                } else {
                    finalCase("real"); // float
                }
                break;
            case 19:
                if (c == '(' || c == ')' || c == '[' || c == ']' || c == '{' || c == '}' || c == ';') {
                    state = 20;
                } else {
                    isBack = true;
                    state = 21; // next graph
                }
                break;
            case 20:
                finalCase("dl"); // delimiter
                break;
            case 21:
                if (c == '+' || c == '-' || c == '*' || c == '/') {
                    state = 24;
                } else if (c == '&') {
                    state = 22;
                } else if (c == '|') {
                    state = 23;
                } else {
                    isBack = true;
                    state = 25;// next graph
                }
                break;
            case 22:
                if (c == '&') {
                    state = 24;
                } else {
                    fail("\"&\"不是操作符");
                }
                break;
            case 23:
                if (c == '|') {
                    state = 24;
                } else {
                    fail("\"|\"不是操作符");
                }
                break;
            case 24:
                finalCase("op"); // operator
                break;
            case 25:
                fail("未识别字符" + c);
        }
        if (!isBack && isAppend) {
            wordBuilder.append(c);
        }
    }

    private void finalCase(String type) {
        String word = wordBuilder.toString();
        if (type.equals("id")) {
            if (keywords.contains(word))
                result.add(new Pair<>(word, "key"));
            else if (basics.contains(word))
                result.add(new Pair<>(word, "basic"));
            else
                result.add(new Pair<>(word, "id"));
        } else {
            result.add(new Pair<>(word, type));
        }
        wordBuilder.setLength(0);
        state = 0;
        isBack = true;
    }

    private void fail(String reason) {
        System.out.println("fail: " + reason);
        System.exit(0);
    }

    public void outputWords() {
        result.forEach(p -> System.out.println(p.getKey() + " " + p.getValue()));
    }

    public List<Pair<String, String>> getResult() {
        return new ArrayList<>(result);
    }

}
