import javafx.util.Pair;

import java.util.*;

/**
 * Create by NOSAE on 2021/4/13
 */
public class GrammarAnalyzer {

    private final Map<String, List<List<String>>> grammar = new LinkedHashMap<>();
    private final List<String> nonTerminals = new ArrayList<>(Arrays.asList(
            "program", "block", "decls", "decl", "type",
            "stmts", "stmt", "stmt1", "loc", "bool", "join",
            "equality", "rel", "rel1", "expr", "term", "unary",
            "factor"
    ));
    private HashSet<String> terminals = new HashSet<>(Arrays.asList(
            "{", "}", "id", ";", "[",
            "]", "num", "basic", "=",
            "if", "(", ")", "else", "while",
            "do", "break", "||", "&&", "==", "!=",
            "<", "<=", ">=", ">", "+",
            "-", "*", "/", "!", "real",
            "true", "false"
    ));

    public GrammarAnalyzer() {
        grammar.put("program", new ArrayList<>(Collections.singletonList(
                new ArrayList<>(Collections.singletonList("block"))
        )));
        grammar.put("block", new ArrayList<>(Collections.singletonList(
                new ArrayList<>(Arrays.asList("{", "decls", "stmts", "}"))
        )));
        grammar.put("decls", new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList("decls", "decl")),
                new ArrayList<>(Collections.singletonList("ε"))
        )));
        grammar.put("decl", new ArrayList<>(Collections.singletonList(
                new ArrayList<>(Arrays.asList("type", "id", ";"))
        )));
        grammar.put("type", new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList("type", "[", "num", "]")),
                new ArrayList<>(Collections.singletonList("basic"))
        )));
        grammar.put("stmts", new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList("stmts", "stmt")),
                new ArrayList<>(Collections.singletonList("ε"))
        )));
        grammar.put("stmt", new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList("loc", "=", "bool", ";")),
                new ArrayList<>(Arrays.asList("if", "(", "bool", ")", "stmt", "stmt1")),
                // new ArrayList<>(Arrays.asList("if", "(", "bool", ")", "else", "stmt")),
                new ArrayList<>(Arrays.asList("while", "(", "bool", ")", "stmt")),
                new ArrayList<>(Arrays.asList("do", "stmt", "while", "(", "bool", ")", ";")),
                new ArrayList<>(Arrays.asList("break", ";")),
                new ArrayList<>(Collections.singletonList("block"))
        )));
        grammar.put("stmt1", new ArrayList<>(Arrays.asList(
                new ArrayList<>(Collections.singletonList("ε")),
                new ArrayList<>(Arrays.asList("else", "stmt"))
        )));
        grammar.put("loc", new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList("loc", "[", "bool", "]")),
                new ArrayList<>(Collections.singletonList("id"))
        )));
        grammar.put("bool", new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList("bool", "||", "join")),
                new ArrayList<>(Collections.singletonList("join"))
        )));
        grammar.put("join", new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList("join", "&&", "equality")),
                new ArrayList<>(Collections.singletonList("equality"))
        )));
        grammar.put("equality", new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList("equality", "==", "rel")),
                new ArrayList<>(Arrays.asList("equality", "!=", "rel")),
                new ArrayList<>(Collections.singletonList("rel"))
        )));
        grammar.put("rel", new ArrayList<>(Collections.singletonList(
                new ArrayList<>(Arrays.asList("expr", "rel1"))
                // new ArrayList<>(Arrays.asList("expr", "<", "expr")),
                // new ArrayList<>(Arrays.asList("expr", "<=", "expr")),
                // new ArrayList<>(Arrays.asList("expr", ">=", "expr")),
                // new ArrayList<>(Arrays.asList("expr", ">", "expr")),
                // new ArrayList<>(Collections.singletonList("expr"))
        )));
        grammar.put("rel1", new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList("<", "expr")),
                new ArrayList<>(Arrays.asList("<=", "expr")),
                new ArrayList<>(Arrays.asList(">=", "expr")),
                new ArrayList<>(Arrays.asList(">", "expr")),
                new ArrayList<>(Collections.singletonList("ε"))
        )));
        grammar.put("expr", new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList("expr", "+", "term")),
                new ArrayList<>(Arrays.asList("expr", "-", "term")),
                new ArrayList<>(Collections.singletonList("term"))
        )));
        // grammar.put("expr1", new ArrayList<>(Arrays.asList(
        //         new ArrayList<>(Arrays.asList("+", "term")),
        //         new ArrayList<>(Arrays.asList("-", "term"))
        // )));
        grammar.put("term", new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList("term", "*", "unary")),
                new ArrayList<>(Arrays.asList("term", "/", "unary")),
                new ArrayList<>(Collections.singletonList("unary"))
        )));
        grammar.put("unary", new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList("!", "unary")),
                new ArrayList<>(Arrays.asList("-", "unary")),
                new ArrayList<>(Collections.singletonList("factor"))
        )));
        grammar.put("factor", new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList("(", "bool", ")")),
                new ArrayList<>(Collections.singletonList("loc")),
                new ArrayList<>(Collections.singletonList("num")),
                new ArrayList<>(Collections.singletonList("real")),
                new ArrayList<>(Collections.singletonList("true")),
                new ArrayList<>(Collections.singletonList("false"))
        )));
        eliminateRecursion(false);
        initFirstSet(false);
        initFollowSet(false);
        initSelectSet(false);
    }

    private final Queue<String> recursionQueue = new LinkedList<>();
    private final Queue<String> tempRecursionQueue = new LinkedList<>();
    private final Set<String> recursionNonTerminal = new HashSet<>(); // 记录直接左递归的非终结符
    private final Map<String, List<List<String>>> newGrammar = new HashMap<>();

    private void eliminateRecursion(boolean print) {
        grammar.forEach((k, v) -> {
            recursionQueue.offer(k);
            eliminateRecursionNext(k);
            recursionQueue.poll();
        });
        recursionNonTerminal.forEach(v -> grammar.get(v).forEach(o -> {
            if (o.get(0).equals("ε"))
                o.set(0, v + "'");
            else
                o.add(v + "'");
        }));
        grammar.putAll(newGrammar);
        newGrammar.forEach((k, v) -> nonTerminals.add(k));
        // debug
        if (print) {
            grammar.forEach((k, v) -> {
                System.out.print(k + ": ");
                v.forEach(l -> {
                    System.out.print("| ");
                    for (String s : l) {
                        System.out.print(s + " ");
                    }
                });
                System.out.println();
            });
        }
        System.out.println();
    }

    /**
     * @param key
     * @return 需要进行间接左递归替换的串
     */
    private List<List<String>> eliminateRecursionNext(String key) {
        List<List<String>> res = Collections.emptyList();
        List<List<String>> outputs = grammar.get(key);

        Iterator<List<String>> it = outputs.iterator();

        while (it.hasNext()) {
            List<String> o = it.next();
            String first = o.get(0);

            boolean isNonTerminal = nonTerminals.contains(first);

            if (recursionQueue.contains(first)) {
                while (!recursionQueue.peek().equals(first)) {
                    tempRecursionQueue.offer(recursionQueue.poll());
                }

                if (recursionQueue.size() == 1) {
                    // 直接左递归
                    recursionNonTerminal.add(key);

                    String newKey = key + "'";
                    List<List<String>> newOutput = newGrammar.get(newKey);
                    if (newOutput == null)
                        newOutput = new ArrayList<>(Collections.singletonList(Collections.singletonList("ε")));
                    newGrammar.put(newKey, newOutput);

                    List<String> sub = new ArrayList<>(o.subList(1, o.size()));
                    sub.add(newKey);
                    newOutput.add(sub);
                    it.remove();
                } else {
                    // 间接左递归
                    return outputs;
                }
            } else if (isNonTerminal) {
                // 非终结符，进入下一层
                recursionQueue.offer(first);
                List<List<String>> replaceList = eliminateRecursionNext(first);
                recursionQueue.poll();
                while (!tempRecursionQueue.isEmpty())
                    recursionQueue.offer(tempRecursionQueue.poll());
                if (!replaceList.isEmpty()) {
                    it.remove();
                    List<String> sub = o.subList(1, o.size());
                    replaceList.forEach(rl -> rl.addAll(sub));
                    outputs.addAll(replaceList);
                }
            }
        }

        while (!recursionQueue.peek().equals(key)) {
            tempRecursionQueue.offer(recursionQueue.poll());
        }

        return res;
    }

    public void printGrammar() {
        grammar.forEach((k, v) -> {
            System.out.print(k + ": ");
            for (List<String> strings : v) {
                for (String s : strings) {
                    System.out.print(s + " ");
                }
                System.out.print("| ");
            }
            System.out.println();
        });
    }

    private Map<String, Set<String>> firstSet;

    public Map<String, Set<String>> getFirstSet() {
        return new HashMap<>(firstSet);
    }

    /**
     * 获取first集
     */
    private void initFirstSet(boolean print) {
        Map<String, Set<String>> res = new HashMap<>();
        firstSet = res;
        grammar.forEach((k, v) -> {
            Set<String> set = res.get(k);
            if (set == null) {
                initFirstSetInternal(grammar, res, k);
            }
        });
        LinkedHashMap<String, Set<String>> sortedRes = new LinkedHashMap<>();
        nonTerminals.forEach(t -> {
            sortedRes.put(t, firstSet.get(t));
        });
        firstSet = sortedRes;
        // debug
        if (print) {
            System.out.println("First Set");
            sortedRes.forEach((k, v) -> {
                System.out.print(k + ": ");
                for (String s : v) {
                    System.out.print(s + " ");
                }
                System.out.println();
            });
        }
        System.out.println();
    }

    private Set<String> initFirstSetInternal(Map<String, List<List<String>>> grammar, Map<String, Set<String>> res, String key) {
        List<List<String>> outputs = grammar.get(key);
        HashSet<String> set = new HashSet<>();
        outputs.forEach(output -> {
            for (String o : output) {
                if (nonTerminals.contains(o)) {
                    // 非终结符
                    Set<String> fs = res.get(o);
                    if (fs != null) {
                        set.addAll(fs);
                    } else {
                        set.addAll(initFirstSetInternal(grammar, res, o));
                    }
                    boolean hasEmpty = false;
                    for (List<String> ntOutputs : grammar.get(o)) {
                        if (ntOutputs.get(0).equals("ε")) {
                            hasEmpty = true;
                            break;
                        }
                    }
                    if (!hasEmpty) {
                        break;
                    }
                } else {
                    // 终结符
                    set.add(o);
                    break;
                }
            }

        });
        res.put(key, set);
        return set;
    }

    private Map<String, Set<String>> followSet;

    public Map<String, Set<String>> getFollowSet() {
        return new HashMap<>(followSet);
    }

    /**
     * 获取follow集
     */
    private void initFollowSet(boolean print) {
        HashMap<String, Set<String>> res = new HashMap<>();
        followSet = res;
        for (String s : nonTerminals) {
            res.put(s, new HashSet<>());
        }
        res.get("program").add("$");
        nonTerminals.forEach(s -> initFollowSetInternal(res, s));
        LinkedHashMap<String, Set<String>> sortedRes = new LinkedHashMap<>();
        nonTerminals.forEach(t -> {
            sortedRes.put(t, followSet.get(t));
        });
        followSet = sortedRes;
        // debug
        if (print) {
            System.out.println("Follow Set");
            sortedRes.forEach((k, v) -> {
                System.out.print(k + ": ");
                for (String s : v) {
                    System.out.print(s + " ");
                }
                System.out.println();
            });
        }
        System.out.println();
    }

    private final Queue<String> fRecursionQueue = new LinkedList<>();

    private Set<String> initFollowSetInternal(HashMap<String, Set<String>> res, String key) {
        Set<String> set = res.get(key);
        if (set == null) {
            System.out.println(key);
        }

        grammar.forEach((k, v) -> v.forEach(output -> {
            boolean start = false;
            boolean isFinished = false;
            for (String s : output) {
                if (!start && s.equals(key)) {
                    start = true;
                } else if (start && !isFinished) {
                    if (nonTerminals.contains(s)) {
                        Set<String> first = firstSet.get(s);
                        set.addAll(first);
                        if (!set.remove("ε")) {
                            isFinished = true;
                        }
                    } else {
                        set.add(s);
                        isFinished = true;
                    }
                }
            }
            if (start && !isFinished && !fRecursionQueue.contains(k)) {
                fRecursionQueue.offer(key);
                set.addAll(initFollowSetInternal(res, k));
                fRecursionQueue.poll();
            }
        }));
        return set;
    }

    private Map<String, Map<String, List<String>>> selectSet;

    public Map<String, Map<String, List<String>>> getSelectSet() {
        return new HashMap<>(selectSet);
    }

    private void initSelectSet(boolean print) {
        selectSet = new HashMap<>();
        grammar.forEach((k, v) -> {
            HashMap<String, List<String>> map = new HashMap<>();
            selectSet.put(k, map);
            v.forEach(output -> {
                HashSet<String> set = new HashSet<>();
                Iterator<String> it = output.iterator();
                boolean addFollow = false;
                while (it.hasNext()) {
                    String o = it.next();
                    if (nonTerminals.contains(o)) {
                        // 非终结符
                        set.addAll(firstSet.get(o));
                        if (!set.remove("ε")) {
                            break;
                        } else if (!it.hasNext()) {
                            addFollow = true;
                        }
                    } else if (!o.equals("ε")) {
                        // 终结符
                        set.add(o);
                        break;
                    } else {
                        // 空
                        if (!it.hasNext()) {
                            addFollow = true;
                        }
                    }
                }
                if (addFollow) {
                    set.addAll(followSet.get(k));
                }

                set.forEach(t ->
                        map.put(t, output)
                );
                // debug
                terminals.forEach(t -> map.putIfAbsent(t, Collections.emptyList()));
            });
        });

        Map<String, Map<String, List<String>>> sortedRes = new LinkedHashMap<>();
        nonTerminals.forEach(t -> {
            sortedRes.put(t, selectSet.get(t));
        });
        selectSet = sortedRes;
        // debug
        if (print) {
            System.out.println("LL(1)");
            System.out.printf("%-20s|", "");
            terminals.forEach(t -> System.out.printf("%-32s|", t));
            System.out.println();
            sortedRes.forEach((k, map) -> {
                System.out.printf("%-20s|", k);
                terminals.forEach(t -> {
                    List<String> ol = map.get(t);
                    final StringBuilder out = new StringBuilder();
                    if (!ol.isEmpty()) {
                        out.append(k);
                        out.append("->");
                        ol.forEach(o -> {
                            out.append(o);
                            out.append(" ");
                        });
                    }
                    System.out.printf("%-32s|", out.toString());
                });
                System.out.println();
            });
            System.out.println();
        }
    }

    private final List<String> directType = Arrays.asList("num", "real", "id", "basic");

    public void analyze(List<Pair<String, String>> input) {
        Stack<String> stack = new Stack<>();
        stack.push("$");
        stack.push("program");

        Stack<String> realInput = new Stack<>();
        realInput.push("$");
        input.forEach(pair -> {

        });
        for (int i = input.size() - 1; i >= 0; i--) {
            Pair<String, String> pair = input.get(i);
            String word = pair.getKey();
            String type = pair.getValue();

            if (directType.contains(type)) {
                realInput.push(type);
            } else {
                realInput.push(word);
            }
        }

        String format = "%-80s%-20s%-50s\n";
        System.out.println("Start match");
        System.out.printf(format, "Stack", "InputTop", "Output");
        System.out.printf(format, "program $", realInput.peek(), "");
        while (!stack.empty() && !realInput.empty()) {

            while (!stack.empty() && !realInput.empty() && stack.peek().equals(realInput.peek())) {
                StringBuilder stackBuilder = new StringBuilder();
                for (int i = stack.size() - 1; i >= 0; i--) {
                    stackBuilder.append(stack.get(i)).append(" ");
                }
                String pop = realInput.pop();
                System.out.printf(format, stackBuilder.toString(), pop, "match " + pop);
                stack.pop();
            }
            if (stack.isEmpty() || realInput.empty()) {
                break;
            }

            String iTop = realInput.peek();
            String top = stack.pop();

            if (top.equals("ε"))
                continue;

            // System.out.println(top);
            Map<String, List<String>> topM = selectSet.get(top);
            if (topM == null) {
                System.out.println("match fail: LL(1)[" + top + "]");
                System.exit(0);
            }
            List<String> output = topM.get(iTop);

            if (output != null && !output.isEmpty()) {
                for (int j = output.size() - 1; j >= 0; j--) {
                    stack.push(output.get(j));
                }
                StringBuilder stackBuilder = new StringBuilder();
                StringBuilder outputBuilder = new StringBuilder();
                // stack.forEach(s -> stackBuilder.append(s).append(" "));
                for (int i = stack.size() - 1; i >= 0; i--) {
                    stackBuilder.append(stack.get(i)).append(" ");
                }
                outputBuilder.append(top).append("->");
                output.forEach(s -> outputBuilder.append(s).append(" "));
                System.out.printf(format, stackBuilder.toString(), realInput.peek(), outputBuilder.toString());
            } else {
                System.out.println("match fail: LL(1)[" + top + "][" + iTop + "]");
                System.exit(0);
            }
        }
        if (stack.empty() && realInput.empty()) {
            System.out.println("match finish");
        } else {
            System.out.println("match fail: ");
            if (stack.empty()) {
                System.out.print("stack: ");
                for (int i = stack.size() - 1; i >= 0; i--) {
                    System.out.print(stack.get(i) + " ");
                }
            } else {
                System.out.print("input stack: ");
                for (int i = realInput.size() - 1; i >= 0; i--) {
                    System.out.print(realInput.get(i) + " ");
                }
            }
        }
    }
}
