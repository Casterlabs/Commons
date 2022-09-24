package co.casterlabs.commons.functional.tuples;

import org.jetbrains.annotations.Nullable;

/**
 * A {@link Tuple} of four types.
 */
public class Quadruple<A, B, C, D> extends Tuple {

    public Quadruple(@Nullable A valA, @Nullable B valB, @Nullable C valC, @Nullable D valD) {
        super(valA, valB, valC);
    }

    public A a() {
        return this.getArg(0);
    }

    public B b() {
        return this.getArg(1);
    }

    public C c() {
        return this.getArg(2);
    }

    public D d() {
        return this.getArg(2);
    }

}
