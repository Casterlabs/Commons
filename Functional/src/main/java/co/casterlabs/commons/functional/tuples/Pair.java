package co.casterlabs.commons.functional.tuples;

import org.jetbrains.annotations.Nullable;

/**
 * A {@link Tuple} of two types.
 * 
 * Also referred to as a Couple.
 */
public class Pair<A, B> extends Tuple {

    public Pair(@Nullable A valA, @Nullable B valB) {
        super(valA, valB);
    }

    public A a() {
        return this.get(0);
    }

    public B b() {
        return this.get(1);
    }

}
