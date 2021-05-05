import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Create by NOSAE on 2021/4/13
 */
public class Main {
    public static void main(String[] args) throws IOException {
        WordAnalyzer wa = new WordAnalyzer();
        wa.analyzeWords(new File("input.txt"));
        List<Pair<String, String>> input = wa.getResult();

        GrammarAnalyzer analyzer = new GrammarAnalyzer();
        analyzer.analyze(input);
        System.out.println("\n");
        // Map<String, Set<String>> firstSet = analyzer.getFirstSet();
        // firstSet.forEach((k, v) -> {
        //     System.out.print(k + ": ");
        //     for (String s : v) {
        //         System.out.print(s + " ");
        //     }
        //     System.out.println();
        // });
    }
}
