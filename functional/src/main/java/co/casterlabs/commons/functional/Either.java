/* 
Copyright 2022 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.functional;

import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * A helper class which allows you to return two different types without needing
 * to resort to using Object.
 * 
 * @see {@link Either.newA(Object)}
 * @see {@link Either.newB(Object)}
 *
 */
@SuppressWarnings("unchecked") // The compilier does not consider our `isA` sufficient to bypass this warning.
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Either<A, B> {
    private Object value;
    private @Getter boolean isA;

    /**
     * @return                       the value as type A.
     * 
     * @throws IllegalStateException if the value is actually of type B.
     */
    public A a() {
        if (!this.isA) throw new IllegalStateException("Unable to get the value as type A because the value is of type B.");

        return (A) this.value;
    }

    /**
     * @return                       the value as type B.
     * 
     * @throws IllegalStateException if the value is actually of type A.
     */
    public B b() {
        if (this.isA) throw new IllegalStateException("Unable to get the value as type B because the value is of type A.");

        return (B) this.value;
    }

    /**
     * Executes the given {@link Consumer} if the value is of type A, does nothing
     * if otherwise.
     * 
     * @param  the consumer
     * 
     * @return     this instance, for chaining.
     */
    public Either<A, B> ifA(Consumer<A> then) {
        if (this.isA) {
            then.accept((A) this.value);
        }

        return this;
    }

    /**
     * Executes the given {@link Consumer} if the value is of type B, does nothing
     * if otherwise.
     * 
     * @param  the consumer
     * 
     * @return     this instance, for chaining.
     */
    public Either<A, B> ifB(Consumer<B> then) {
        if (!this.isA) {
            then.accept((B) this.value);
        }

        return this;
    }

    /* ---------------- */
    /* ---------------- */
    /* ---------------- */

    /**
     * Constructs a new Either with a value of type A.
     */
    public static <A, B> Either<A, B> newA(@NonNull A val) {
        return new Either<A, B>(val, true);
    }

    /**
     * Constructs a new Either with a value of type B.
     */
    public static <A, B> Either<A, B> newB(@NonNull B val) {
        return new Either<A, B>(val, false);
    }

}
