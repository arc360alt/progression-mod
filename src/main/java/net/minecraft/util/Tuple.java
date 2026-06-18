package net.minecraft.util;

// Compatibility shim: net.minecraft.util.Tuple was removed in MC 26.2 but cloth-config still
// references it. This restores the class so the config screen works until cloth-config updates.
public class Tuple<A, B> {
    private A a;
    private B b;

    public Tuple(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A getA() { return a; }
    public void setA(A a) { this.a = a; }
    public B getB() { return b; }
    public void setB(B b) { this.b = b; }
}
