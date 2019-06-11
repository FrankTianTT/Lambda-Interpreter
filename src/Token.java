package cn.seecoder;

public class Token {
    public static enum Type{EOF,LPAREN, RPAREN, LAMBDA, DOT, LCID}   //token枚举类型

    final Type type;
    final String value;

    Token(Type type, String value) {
        this.type = type;
        this.value = value;
    }
}
