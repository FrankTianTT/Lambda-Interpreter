package cn.seecoder;

public class Interpreter {
    Parser parser;
    AST astAfterParser;

    public Interpreter(Parser p){
        parser = p;
        astAfterParser = p.parse();
        //System.out.println("After parser:"+astAfterParser.toString());
    }
    /*检查node是否为value，分几种情况
    * 1 Identifier一定是value
    * 2 Application分情况
    * 2.1 lhs为Identifier，看rhs
    * 2.2 lhs为abstraction，不是value
    * 3 Abstraction比较复杂
    * 3.1 每个body都是Abstraction，是value
    * 3.2 body中有application 且lhs为Identifier，看rls
    * 3.3 body中有application 且lhs为abstraction，不是value
    **/
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

    public AST eval(){

        AST a =  eval(astAfterParser);

        return a;
    }


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

    private AST copy(AST ast){
        if(ast instanceof Identifier){
            return new Identifier(((Identifier) ast).name,((Identifier) ast).value);
        }
        if(ast instanceof Abstraction){
            return new Abstraction(((Abstraction) ast).param,copy(((Abstraction) ast).body));
        }
        if(ast instanceof Application){
            return new Application(copy(((Application) ast).lhs),copy(((Application) ast).rhs));
        }
        return null;
    }
    private AST substitute(AST node,AST value){
        //return subst(node,value,0);
        //node 和value可能是一个引用，这样会出问题，需要把node或者value先备份一下

        return shift(-1,subst(node,shift(1,copy(value),0),0),0);

    }

    /**
     *  value替换node节点中的变量：
     *  如果节点是Applation，分别对左右树替换；
     *  如果node节点是abstraction，替入node.body时深度得+1；
     *  如果node是identifier，则替换De Bruijn index值等于depth的identifier（替换之后value的值加深depth）

     *@param value 替换成为的value
     *@param node 被替换的整个节点
     *@param depth 外围的深度


     *@return AST
     */
    private AST subst(AST node, AST value, int depth){
        if(node instanceof Application){
            return new Application(subst(((Application) node).lhs,value,depth),subst(((Application) node).rhs,value,depth));
        }
        else if(node instanceof Abstraction){
            return new Abstraction(((Abstraction) node).param,subst(((Abstraction) node).body,value,depth+1));
        }
        else if(node instanceof Identifier){
            if(((Identifier) node).value == depth){
                return shift(depth,copy(value),0);


            }else {
                return node;
            }
        }
        return null;
    }





    /**
     *  De Bruijn index值位移   //这个是用来处理自由变量用的
     *  如果节点是Applation，分别对左右树位移；
     *  如果node节点是abstraction，新的body等于旧node.body位移by（from得+1）；
     *  如果node是identifier，则新的identifier的De Bruijn index值如果大于等于from则加by，否则加0（超出内层的范围的外层变量才要shift by位）.
     *@param by 位移的距离
     *@param node 位移的节点
     *@param from 内层的深度


     *@return AST


     */

    private AST shift(int by, AST node,int from){
        if(node instanceof Application){
            return new Application(shift(by,((Application) node).lhs,from),shift(by,((Application) node).rhs,from));
        }
        else if(node instanceof Abstraction){
            return new Abstraction(((Abstraction) node).param,shift(by,((Abstraction) node).body,from+1));
        }
        else if(node instanceof Identifier){
            if(((Identifier) node).value>=from){
                ((Identifier) node).value += by;
                return node;
            }
            else return node;
        }

        return null;

    }
    static String ZERO = "(\\f.\\x.x)";
    static String SUCC = "(\\n.\\f.\\x.f (n f x))";
    static String ONE = app(SUCC, ZERO);
    static String TWO = app(SUCC, ONE);
    static String THREE = app(SUCC, TWO);
    static String FOUR = app(SUCC, THREE);
    static String FIVE = app(SUCC, FOUR);
    static String PLUS = "(\\m.\\n.((m "+SUCC+") n))";
    static String POW = "(\\b.\\e.e b)";       // POW not ready
    static String PRED = "(\\n.\\f.\\x.n(\\g.\\h.h(g f))(\\u.x)(\\u.u))";
    static String SUB = "(\\m.\\n.n"+PRED+"m)";
    static String TRUE = "(\\x.\\y.x)";
    static String FALSE = "(\\x.\\y.y)";
    static String AND = "(\\p.\\q.p q p)";
    static String OR = "(\\p.\\q.p p q)";
    static String NOT = "(\\p.\\a.\\b.p b a)";
    static String IF = "(\\p.\\a.\\b.p a b)";
    static String ISZERO = "(\\n.n(\\x."+FALSE+")"+TRUE+")";
    static String LEQ = "(\\m.\\n."+ISZERO+"("+SUB+"m n))";
    static String EQ = "(\\m.\\n."+AND+"("+LEQ+"m n)("+LEQ+"n m))";
    static String MAX = "(\\m.\\n."+IF+"("+LEQ+" m n)n m)";
    static String MIN = "(\\m.\\n."+IF+"("+LEQ+" m n)m n)";

    private static String app(String func, String x){
        return "(" + func + x + ")";
    }
    private static String app(String func, String x, String y){
        return "(" +  "(" + func + x +")"+ y + ")";
    }
    private static String app(String func, String cond, String x, String y){
        return "(" + func + cond + x + y + ")";
    }

    public static void main(String[] args) {
        // write your code here


        String[] sources = {
                ZERO,//0
                ONE,//1
                TWO,//2
                THREE,//3
                app(PLUS, ZERO, ONE),//4
                app(PLUS, TWO, THREE),//5
                app(POW, TWO, TWO),//6
                app(PRED, ONE),//7
                app(PRED, TWO),//8
                app(SUB, FOUR, TWO),//9
                app(AND, TRUE, TRUE),//10
                app(AND, TRUE, FALSE),//11
                app(AND, FALSE, FALSE),//12
                app(OR, TRUE, TRUE),//13
                app(OR, TRUE, FALSE),//14
                app(OR, FALSE, FALSE),//15
                app(NOT, TRUE),//16
                app(NOT, FALSE),//17
                app(IF, TRUE, TRUE, FALSE),//18
                app(IF, FALSE, TRUE, FALSE),//19
                app(IF, app(OR, TRUE, FALSE), ONE, ZERO),//20
                app(IF, app(AND, TRUE, FALSE), FOUR, THREE),//21
                app(ISZERO, ZERO),//22
                app(ISZERO, ONE),//23
                app(LEQ, THREE, TWO),//24
                app(LEQ, TWO, THREE),//25
                app(EQ, TWO, FOUR),//26
                app(EQ, FIVE, FIVE),//27
                app(MAX, ONE, TWO),//28
                app(MAX, FOUR, TWO),//29
                app(MIN, ONE, TWO),//30
                app(MIN, FOUR, TWO),//31
        };



    //    for(int i=0;i<sources.length;i++){
        int i=6;

            String source = sources[i];

            Lexer lexer = new Lexer(source);

            Parser parser = new Parser(lexer);
System.out.println(parser.parse().toGraph());
            Interpreter interpreter = new Interpreter(parser);

            AST result = interpreter.eval();

            System.out.println(result.toString());

       // }
    }
}

