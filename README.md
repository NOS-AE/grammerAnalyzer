# 语法分析

## 原始不明风格文法：
```
program → block
block→{ decls  stmts}
decls → decls  decl  | ε
decl → type  id;
type → type[num]  |  basic
stmts → stmts  stmt | ε

stmt → loc=bool;
      | if(bool)stmt
      | if(bool)stmt else stmt
      | while(bool)stmt
      | do stmt while(bool);
      | break;
      | block
loc → loc[num]  | id
bool →bool  ||  join   |  join
join → join  &&  equality  | equality
equality → equality==rel  | equality ！= rel  | rel
rel → expr<expr |expr<=expr|expr>=expr|expr>expr|expr
expr → expr+term |expr-term |term
term → term*unary|term/unary|unary
unary→！unary | -unary | factor
factor→ (bool) | loc | num | real | true |false
```

## 步骤
1. 手动提取左公因子
2. 消除左递归
3. 求First集
4. 求Follow集
5. 求LL(1)
6. 用LL(1)分析input.txt，打印分析栈(剩余输入过长，只打印栈顶内容)