package cn.seecoder;

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
