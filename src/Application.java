package cn.seecoder;

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
