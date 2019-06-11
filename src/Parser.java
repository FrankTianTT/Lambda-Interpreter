package cn.seecoder;

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


