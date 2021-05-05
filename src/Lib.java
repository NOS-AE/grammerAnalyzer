import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Lib {

    /**
     *
     * @param input 文法
     */
    public static Map<String, Set<Character>> getFirst(Map<String, String> input) {
        Map<String, Set<Character>> res = new HashMap<>();
        input.forEach((k, v) -> {
            Set<Character> set = res.get(k);
            if (set == null)
                getCharFirst(input, res, k);
        });
        return res;
    }

    private static Set<Character> getCharFirst(Map<String, String> input, Map<String, Set<Character>> res, String k) {
        Set<Character> set = new HashSet<>();
        // 寻找First(k)
        String v = input.get(k);
        String[] vs = v.split("\\|");
        for (String s : vs) {
            char first = s.charAt(0);
            if (first == 'ε' || !Character.isUpperCase(first)) {
                // 空 | 终结符
                set.add(first);
            } else {
                // 非终结符
                String nextK = String.valueOf(s.charAt(0));
                if (s.length() > 1 && s.charAt(1) == '\'') {
                    nextK += '\'';
                }
                set.addAll(getCharFirst(input, res, nextK));
            }
        }
        // 寻找完毕，加入结果集中
        res.put(k, set);
        return set;
    }
}
