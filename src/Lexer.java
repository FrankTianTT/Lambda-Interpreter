package cn.seecoder;

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
