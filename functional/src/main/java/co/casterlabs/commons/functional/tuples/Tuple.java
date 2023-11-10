/* 
Copyright 2022 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
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
    public <T> @Nullable T get(int position) {
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
