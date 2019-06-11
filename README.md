# Lambda-Interpreter
老师留了一个大作业，手写一个lambda interpreter，在这里记录一下实现过程。
#1 词法分析
>**词法分析**（英语：**lexical analysis**）是计算机科学中将字符序列转换为单词（Token）序列的过程。进行词法分析的程序或者函数叫作**[词法分析器](https://baike.baidu.com/item/%E8%AF%8D%E6%B3%95%E5%88%86%E6%9E%90%E5%99%A8)**（Lexical analyzer，简称Lexer），也叫**扫描器**（Scanner）。[词法分析器](https://baike.baidu.com/item/%E8%AF%8D%E6%B3%95%E5%88%86%E6%9E%90%E5%99%A8/4336210)一般以函数的形式存在，供[语法分析器](https://baike.baidu.com/item/%E8%AF%AD%E6%B3%95%E5%88%86%E6%9E%90%E5%99%A8/10598664)调用。 完成词法分析任务的程序称为词法分析程序或词法分析器或扫描器。

>完成词法分析任务的程序称为词法分析程序或词法分析器或扫描器。从左至右地对源程序进行扫描，按照语言的词法规则识别各类单词，并产生相应单词的属性字。

显然，所谓的lexer要做的工作就是把一个字符串string转化为一个由token组成的序列。

所谓的token可以翻译为标记，因此词法分析的过程也可以成为标记化tokenization。

每个token应该由至少两个属性，分别是type和value，分别是token的类型和值。

因此，Token类可以这样写：
```
public class Token {
    public static enum Type{LPAREN, RPAREN, LAMBDA, DOT, LCID}

    final Type type;
    final String value;

    Token(Type type, String value) {
        this.type = type;
        this.value = value;
    }
}
```
接下来，就是lexer的任务了，它需要将一个string转化为一系列token。

一个lambda表达式差不多长成这个样子：
```(λx. λy. x) (λy. y) (λx. x)```
为了方便，我们用"\"替代"λ"：
```(\x. \y. x) (\y. y) (\x. x)```
因为java的字符串转义，需要写成下面的样子：
```(\\x. \\y. x) (\\y. y) (\\x. x)```

那么lexer的任务就是一个个识别这些token，token由以下几种：
```
LPAREN: '('
RPAREN: ')'
LAMBDA: '\' 
DOT: '.'
LCID: /[a-z][a-zA-Z]*/ 
```
前四个都好说，最后一个所谓的LCID，其实就是表识符identifier，之前的x,y都是标识符。

ps：LCID是lambda calculator identifier的缩写。


lexer类的代码如下
```
import java.lang.reflect.Array;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer{                     //词法分析器

    public String source;               //接下来要处理的字符串
    public Token token ;                //当前的token

    public Lexer(String s){             //构造
        source = s;
        this.nextToken();               //lexer对象的初始值对应第一个token
    }
    /*
     *注意：
     *不能直接把字符串中的空格全部去掉，只能去掉最前面的空格
     **/
    private void nextToken(){                                   //封装，只接受同类调用
        String s = this.source;                                 //s作为lexer.source的拷贝
        char[] split = s.toCharArray();                         //分成字符数组
        if(split.length == 0){
            this.token = new Token(Token.Type.EOF,null);    //如果不为终止符EOF，继续
            System.out.println("EOF");
            return;
        }
        int skip = 0;                               //skip用来跳过字符串中的空格
        for(int i=0;i<split.length;i++){            //寻找最前面的空格
            if(split[i]!=' '){
                skip = i;
                break;
            }
        }
        s = s.substring(skip,s.length());           //重新定义字符串，把前面的空格删除
        split = s.toCharArray();
        switch(split[0]){                           //考虑第一个非空格字符的种类
            case '.':{
                this.token = new Token(Token.Type.DOT,".");
                this.source = s.substring(1,s.length());
                System.out.println("DOT");
                return;
            }
            case '\\':{
                this.token = new Token(Token.Type.LAMBDA,"\\");
                this.source = s.substring(1,s.length());
                System.out.println("LAMBDA");
                return;
            }
            case '(':{
                this.token = new Token(Token.Type.LPAREN,"(");
                this.source = s.substring(1,s.length());
                System.out.println("LPAREN");
                return;
            }
            case ')':{
                this.token = new Token(Token.Type.RPAREN,")");
                this.source = s.substring(1,s.length());
                System.out.println("RPAREN");
                return;
            }
            default:{                               //如果不是以上情况，默认为一个lcid，用正则表达式检查
                String lc = findlcid(s);
                this.token = new Token(Token.Type.LCID,lc);
                this.source = s.substring(lc.length());
                System.out.println("LCID");
                return;
            }
        }
    }

    private String findlcid(String s){                 //用正则表达式检查lcid
        String pattern = "^(\\p{Lower}*\\p{Upper}*)";
        Pattern r = Pattern.compile(pattern);

        Matcher m = r.matcher(s);
        if(m.find()){
            return m.group(1);
        }else {
            return null;
        }
    }


    /*
     *辅助函数
     *用来判断当前的token是否于参数相同
     **/
    public boolean next(Token.Type type){
        return type.equals(this.token.type);
    }
    /*
     *辅助函数
     *用来判断当前的lexer的token是否于参数相同
     * 是的话跳过这个token
     * 不是的话报错
     **/
    public void match(Token.Type type){
        if(this.next(type)){
            this.nextToken();
            return;
        }
        else {
            System.out.println("lambda wrong!");
        }
    }
    /*
     *辅助函数
     *返回token的值
     **/
    public String token(Token.Type type){
        if(type==null){
            return null;
        }

        Token t = this.token;
        this.match(type);
        return t.value;
    }
    /*
     *辅助函数
     *用来判断当前的lexer的token是否于参数相同
     * 是的话跳过，返回true
     * 不是的话返回false
     **/
    public boolean skip(Token.Type type){
        if(this.next(type)){
            this.nextToken();
            return true;
        }
        else {
            return false;
        }
    }
}

```

其中的四个辅助函数```next()``` ```match()``` ```token()``` ```skip()```，会在下面的语法分析环节被用到。

#2 语法分析
语法分析才是重头戏，之前的词法分析顶多算是foreplay（笑）。
>[语法](https://baike.baidu.com/item/%E8%AF%AD%E6%B3%95)[分析](https://baike.baidu.com/item/%E5%88%86%E6%9E%90)是编译过程的一个[逻辑](https://baike.baidu.com/item/%E9%80%BB%E8%BE%91/543)阶段。[语法](https://baike.baidu.com/item/%E8%AF%AD%E6%B3%95/2447258)[分析](https://baike.baidu.com/item/%E5%88%86%E6%9E%90)的[任务](https://baike.baidu.com/item/%E4%BB%BB%E5%8A%A1/33127)是在[词法分析](https://baike.baidu.com/item/%E8%AF%8D%E6%B3%95%E5%88%86%E6%9E%90/8853461)的基础上将[单词](https://baike.baidu.com/item/%E5%8D%95%E8%AF%8D/7629019)序列组合成各类语法短语，如“[程序](https://baike.baidu.com/item/%E7%A8%8B%E5%BA%8F/71525)”，“语句”，“[表达式](https://baike.baidu.com/item/%E8%A1%A8%E8%BE%BE%E5%BC%8F/7655228)”等等.语法[分析](https://baike.baidu.com/item/%E5%88%86%E6%9E%90)程序判断源[程序](https://baike.baidu.com/item/%E7%A8%8B%E5%BA%8F/71525)在结构上是否正确.源程序的结构由上下文无关文法描述.语法分析程序可以用YACC等工具自动生成。

在开始语法分析之前，先介绍几个概念。

###2.0.1 上下文无关文法Context-free grammar

这部分内容来自 CSDN，墨城之左 ，[Context-free grammar 与 BNF](https://blog.csdn.net/antony1776/article/details/86412211 ).

上下文无关文法，是一种形式文法（formal grammar）。```形式文法```是形式语言（formal language）的文法，由一组产生规则（production rules）组成，描述该形式语言中所有可能的字符串形式。

形式文法一般可分为四大类：无限制文法（unrestricted grammars），上下文相关文法，上下文无关文法和正则文法（Regular grammar）。

文法由三部分组成：

1. ```Terminal symbols```： 终结符，可以理解为基础符号，词法符号，是不可替代的，天然存在，不能通过文法规则生成！

2.``` Nonterminal symbols```： 非终结符，或者句法变量。

3. ```Production rules```： grammar 是由终结符集和、非终结符集和和产生规则共同组成。产生规则定义了符号之间如何转换替代。规则的左侧是规则头，是可以被替代的符号；右侧是规则体，是具体的内容。

例如：
```
<digit> ::= '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9'
<integer> ::= ['-'] <digit> {<digit>}
```

**（ps：上面的语句表示了一个```Production rules```）**

符号（-,0,1,2,3,4,5,6,7,8,9）是终结符，符号（<digit>,<integer>）是非终结符。那么根据 integer 的生成规则，字符串 “0056, 0000, -000, -111” 都符合文法，可被解析。

再例如：
```
S ::= cAd
A ::= a|ab
```

该例中，(a, b, c, d) 为终结符，(S, A) 为非终结符，字符串"cad"，“cabd” 符合文法规则。

**(ps:1. 我们通常习惯用小些字母lowercase表示终结符，用大写字母uppercase表示非终结符2. "|"表示“或”，代表"::="号左面的符号可以是右面的多种符号中的任意一个)**

###2.0.2 Backus normal form

其实就是计算机领域中的Context-free grammar。

BNF 主要用于对编程语言、文档格式、指令集、或者通信协议等的语法定义。

例如：
关键字
```
identifier          ::=  unquoted_identifier | quoted_identifier
unquoted_identifier ::=  re('[a-zA-Z][a-zA-Z0-9_]*')
quoted_identifier   ::=  '"' (any character where " can appear if doubled)+ '"'
```
常量
```
constant ::=  string | integer | float | boolean | uuid | blob | NULL
string   ::=  '\'' (any character where ' can appear if doubled)+ '\''
              '$$' (any character other than '$$') '$$'
integer  ::=  re('-?[0-9]+')
float    ::=  re('-?[0-9]+(\.[0-9]*)?([eE][+-]?[0-9+])?') | NAN | INFINITY
boolean  ::=  TRUE | FALSE
uuid     ::=  hex{8}-hex{4}-hex{4}-hex{4}-hex{12}
hex      ::=  re("[0-9a-fA-F]")
blob     ::=  '0' ('x' | 'X') hex+
```

再举一个例子，来自 CSDN  汪星人来地球 [上下文无关文法及其分析树](https://blog.csdn.net/hedan2013/article/details/53540721)

例如：考虑如下文法G，其非终结符集合为{L, D}，终结符集合为{0,1,2,…,9,+,-}，开始符号为L，产生式集合为

```
L::=L+D|L−D|D

D::=0|1|2|3|4|5|6|7|8|9
```
这里的生成规则相当简单，我们可以就此画出它的语法树。

例如：1+2-3的分析树如下：

![语法树](https://upload-images.jianshu.io/upload_images/15638690-1c5bc164933c6160.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

具体分析过程如下：
![分析过程](https://upload-images.jianshu.io/upload_images/15638690-7f9146385d1fd96e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


###2.0.3 迭代下降法

递归下降的语法分析是自顶向下语法分析的通用方法，这种方法可能需要进行回溯。

考虑文法：
```
S→cAd
A→ab|a
```
我们尝试对串cad构造语法分析树：

![](https://upload-images.jianshu.io/upload_images/15638690-383b4638e0c25b31.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

可以发现，在第一次对非终结符号A进行推导后，得到的语法分析树的句子cabd不匹配输入cad，因此需要进行回溯；在第二次对A进行推导后，得到的语法分析树的句子cad匹配输入cad，到此，成功构造了串cad的语法分析树，因此我们说串cad是符合该文法的。

###2.1 建立语法生成规则

我们的productions rules如下：

```
term ::= application
       | LAMBDA LCID DOT term

application ::= application atom
              | atom

atom ::= LPAREN term RPAREN
       | LCID
```
通过一个小技巧，避免出现左递归导致的死循环：

把```application ::= application atom | atom```拆成：
```
application ::= atom application'

application' ::= atom application'
               | ε  /* empty */
````
的形式。

值得一提的是，还有一条parser的命名规则：
>希腊字母表示任意由非终结符号和终结符号组成的串或者空串，如α、β、γ等。

适用于上面的情况。

所以，我们的production rules最后会变成：
```
term ::= application
       | LAMBDA LCID DOT term
application ::= atom application'

application' ::= atom application'
               | ε  /* empty */
atom ::= LPAREN term RPAREN
       | LCID
```

###2.2 AST建立
lambda interpreter中的AST有三个类别，分别是Application，Abstraction，Identifier。

结构很分别是：
#####Application
有lhs和rhs两个属性，都是AST类
#####Abstraction
有param和body两个属性，分别是Identifier类和AST类
#####Identifier
有name和value两个属性，分别是String和int类

代码如下
AST类是一个抽象类
```
public abstract class AST {
    public abstract String toString();
    public String toGraph(){
        return "";
    }
}
```

Application类
```
public class Application extends AST {
    AST lhs;//左树
    AST rhs;//右树

    Application(AST l, AST s) {
        lhs = l;
        rhs = s;
    }

    @Override
    public String toGraph() {
        String re = "[Lhs" + this.lhs.toGraph() + "][Rhs" + this.rhs.toGraph() + "]";
        re = "[Application" + re + "]";
        return re;
    }

    @Override
    public String toString() {
        return "("+this.lhs.toString() +" "+ this.rhs.toString()+")";
    }
}
```

Abstraction类
```
public class Abstraction extends AST {
    Identifier param;//变量
    AST body;//表达式

    Abstraction(Identifier p, AST b){
        param = p;
        body = b;
    }

    @Override
    public String toGraph(){
        String re = "[param[" + param.name +"]][body"+body.toGraph()+"]";
        return "[Abstraction" + re + "]";
    }
    @Override
    public String toString(){
        return  "\\"+"."+body.toString();
    }
}
```

Identifier类
```
public class Identifier extends AST {

    String name; //名字
    int value;//De Bruijn index值

    public Identifier(String n,int v){

        name = n;
        value = v;
    }
    @Override
    public String toGraph(){
        String re = "vaule["+this.value+"]";
        re = "[Identifier[" + re + "]]";
        return re;
    }
    @Override
    public String toString(){
        return  String.valueOf(this.value);
    }
}
```

值得注意的是，每个类还配套了两个方法，分别是toString和toGraph
toString是用来生成结果的
toGraph是用来生成图像的

toGraph是把AST结构转化成一种特殊的语法，再通过一个网站转化为树结构。
网站：[http://ironcreek.net/phpsyntaxtree/?](http://ironcreek.net/phpsyntaxtree/?)

接下来就是转化关系。
我们认为Application是一个有左右分支的结构，Abstraction是一个由lambda符号加一个term构成，而identifier，任何一个LCID都是一个identifier。

这样，我们就可以根据递归的思想写出语义分析器parser。

```
import java.util.ArrayList;

import static cn.seecoder.Token.Type.*;

public class Parser {                       //语法分析器
    private Lexer lexer;
    Parser(Lexer l){                        //构造函数，接受一个lexer
        this.lexer = l;
    }
    public AST parse() {                                //parse函数，构造语法树，返回一个语法树
        ArrayList<String> list = new ArrayList<>();          //声明一个列表，用来存储出现的parma
        AST result = this.term(list);                    //从term开始搜索
        return result;
    }

    // term ::= LAMBDA LCID DOT term
    //        | application
    public AST term(ArrayList<String> ctx) {
        if (this.lexer.skip(Token.Type.LAMBDA)) {
            String id = this.lexer.token(Token.Type.LCID);     //匹配LCID
            this.lexer.match(Token.Type.DOT);                   //匹配DOT
            ctx.add(0,id);                          //将匹配的LCID添加到ctx中

            AST term = this.term(ctx);                  //搜索term

            Identifier newid = new Identifier(id,ctx.indexOf(id));
            Abstraction abs = new Abstraction(newid, term);
            ctx.remove(ctx.indexOf(id));
            return abs;

        }
        else {
            return this.application(ctx);
        }
    }
    // application ::= atom application'
    public  AST application(ArrayList<String> ctx) {
        AST lhs = this.atom(ctx);                           //左枝搜索atom

        // application' ::= atom application'
        //                | ε
        while (true) {
            AST rhs = this.atom(ctx);                             //右枝搜索atom
            if (rhs == null) {
                return lhs;
            }
            else {
                lhs = new Application(lhs, rhs);
            }
        }
    }

    // atom ::= LPAREN term RPAREN
    //        | LCID
    AST atom(ArrayList<String> ctx) {
        if (this.lexer.skip(Token.Type.LPAREN)) {
            AST term = this.term(ctx);
            this.lexer.match(Token.Type.RPAREN);
            return term;
        }
        else if (this.lexer.next(Token.Type.LCID)) {
            String id = this.lexer.token(Token.Type.LCID);


            return new Identifier(id,ctx.indexOf(id) == -1?ctx.size():ctx.indexOf(id));
        } else {
            return null;
        }
    }
}
```

# 3. 求值
构建好了语法树，接下来要做的当然就是求值了。
通过之前的lexer和parser，我们可以生成一个类似如下的语法树：
![](https://upload-images.jianshu.io/upload_images/15638690-63e5e5665d649e93.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

这个语法树其实是ONE，也就是ZERO的后继：
  ```
((\n.\f.\x.f (n f x)) (\f.\x.x))
```

接下来，我们需要定义求值规则。

首先，定义一个抽象的概念，value。

所谓的value就是一段不能再求值，也即是已经最简了的lambda表达式。

通过简单的分析可以确定以下规则：
1. Identifier一定是value
2. Application分情况
2.1 lhs为Identifier，看rhs
2.2 lhs为abstraction，不是value
3. Abstraction比较复杂
3.1 每个body都是Abstraction，是value
3.2 body中有application 且lhs为Identifier，看rls
3.3 body中有application 且lhs为abstraction，不是value

其实实际写起来没那么复杂，用递归很简单就实现了。
```
private boolean isValue(AST node){
        if(node instanceof Identifier){
            return true;
        }
        else if(node instanceof Application){
            if(((Application) node).lhs instanceof Abstraction){
                return false;
            }
            else{
                return isValue(((Application) node).rhs) && isValue(((Application) node).lhs);
            }
        }
        else{
            return isValue(((Abstraction)node).body);
        }
    }
```

现在我们已经可以判断一个lambda表达式，或者说一个节点node，是否为value了。

接下来要做的是，把不是value的式子一步步化为value。所以，我们还要定义一个化简规则。

**值得注意的是，所有的化简都是相对于application而言的**

1. 如果application的lhs和rhs都是value，把rhs带入lhs
2. 如果application的rhs不是value，而lhs是value，化简rhs
3. 如果application的rhs和value都不是value，化简lhs

这样看起来已经解决问题了，但实际上，abstraction也可能不是value，因此，还需要在外面添上一条

- 如果abstraction不是value，化简它的body

到此为止，整个求值的规则已经写好了。

```
private AST eval(AST ast){
        while(!isValue(ast)){
            if(ast instanceof Application){
                Application subast = new Application(((Application) ast).lhs,((Application) ast).rhs);
                if((isValue(((Application) ast).rhs) && isValue(((Application) ast).lhs))) {

                    ast = substitute(((Abstraction)((Application) subast).lhs).body,((Application) subast).rhs );
                }
                else if(isValue(((Application) ast).lhs)){

                    ((Application)ast).rhs = eval(((Application)subast).rhs);
                }
                else{
                    ((Application)ast).lhs = eval(((Application)subast).lhs);
                }
            }

            if(ast instanceof Abstraction){
                return new Abstraction(((Abstraction) ast).param,eval(((Abstraction) ast).body));
            }
        }

        return ast;
    }
```
