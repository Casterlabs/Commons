package co.casterlabs.commons.functional.tuples;

import org.jetbrains.annotations.Nullable;

/**
 * A helper for returning multiple arguments in a method without needing
 * barbaric solutions.
 * 
 * @see {@link Pair} for a 2 argument Tuple.
 * @see {@link Triple} for a 3 argument Tuple.
 * @see {@link Quadruple} for a 4 argument Tuple.
 * 
 * @see {@link VariableTuple} for a arbitrary-length argument Tuple.
 */
public abstract class Tuple {
    private final Object[] args;

    /**
     * The size of the Tuple.
     */
    public final int size;

    Tuple(Object... args) {
        this.args = args;
        this.size = args.length;
    }

    /**
     * Retrieves an argument based on it's position, performing an unchecked cast to
     * your type of choice.
     * 
     * @throws ArrayIndexOutOfBoundsException
     */
    @SuppressWarnings("unchecked")
    public <T> @Nullable T getArg(int position) {
        return (T) this.args[position];
    }

    /**
     * @return a clone of the raw arguments array. Use this only if necessary (e.g
     *         interop with a serialization framework).
     */
    @Deprecated
    public Object[] raw() {
        return this.args.clone();
    }

}
