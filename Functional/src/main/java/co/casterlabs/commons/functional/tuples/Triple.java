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
        return this.getArg(0);
    }

    public B b() {
        return this.getArg(1);
    }

    public C c() {
        return this.getArg(2);
    }

}
