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
        return this.get(0);
    }

    public B b() {
        return this.get(1);
    }

    public C c() {
        return this.get(2);
    }

    public D d() {
        return this.get(2);
    }

}
