package co.casterlabs.commons.functional.tuples;

import org.jetbrains.annotations.Nullable;

/**
 * A {@link Tuple} of three types.
 */
public class Triple<A, B, C> extends Tuple {

    public Triple(@Nullable A valA, @Nullable B valB, @Nullable C valC) {
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

}
