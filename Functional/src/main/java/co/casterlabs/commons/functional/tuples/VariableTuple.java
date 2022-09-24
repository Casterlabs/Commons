package co.casterlabs.commons.functional.tuples;

/**
 * A variable-length tuple without any types. Use this if you need a dirty way
 * of extending the existing line-up of Tuples.
 * 
 * @apiNote If you frequently need an additional Tuple, open a issue on
 *          github.com/Casterlabs/Commons and we'll add it for you :^)
 */
public class VariableTuple extends Tuple {

    /**
     * @param args The arguments to add, any of which can be null.
     */
    public VariableTuple(Object... args) {
        super(args.clone());
    }

}
