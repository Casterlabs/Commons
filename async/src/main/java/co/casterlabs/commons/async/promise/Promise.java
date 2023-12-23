/* 
Copyright 2023 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.async.promise;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.commons.async.promise.PromiseFunctionalInterface.PromiseConsumer;
import co.casterlabs.commons.async.promise.PromiseFunctionalInterface.PromiseFunction;
import co.casterlabs.commons.async.promise.PromiseFunctionalInterface.PromiseRunnable;
import co.casterlabs.commons.async.promise.PromiseFunctionalInterface.PromiseRunnableWithHandle;
import co.casterlabs.commons.async.promise.PromiseFunctionalInterface.PromiseSupplier;
import lombok.NonNull;

@SuppressWarnings("unchecked")
public class Promise<T> {
    private final Object lock = new Object();
    private final Deque<Consumer<PromiseResult>> chain = new LinkedList<>();

    private PromiseResult result = null; // ALWAYS null while pending.

    private final PromiseResolver<T> resolver = new PromiseResolver<T>(this) {
        @Override
        protected void handle(@NonNull PromiseResult result) {
            if (Promise.this.result != null) {
                throw new IllegalStateException();
            }

            Promise.this.result = result;
            Promise.this.propagateResult();
        }
    };

    private void propagateResult() {
        synchronized (this.lock) {
            this.lock.notifyAll();
        }

        for (Iterator<Consumer<PromiseResult>> it = this.chain.iterator(); it.hasNext();) {
            Consumer<PromiseResult> downstream = it.next();
            it.remove();
            AsyncTask.create(() -> {
                downstream.accept(this.result);
            });
        }
    }

    private void chainTo(Consumer<PromiseResult> downstream) {
        synchronized (this.lock) {
            if (this.result == null) {
                this.chain.add(downstream);
            } else {
                AsyncTask.create(() -> {
                    downstream.accept(this.result);
                });
            }
        }
    }

    /* ---------------- */
    /* Constructing     */
    /* ---------------- */

    public Promise(@NonNull PromiseRunnable task) {
        this((handle) -> {
            task.run();
            handle.resolve(); // Result with null.
        });
    }

    public Promise(@NonNull PromiseRunnable task, @NonNull Consumer<Runnable> threadConstructor) {
        this((handle) -> {
            task.run();
            handle.resolve(); // Result with null.
        }, threadConstructor);
    }

    public Promise(@NonNull PromiseSupplier<T> task) {
        this((handle) -> {
            T result = task.get();
            handle.resolve(result);
        });
    }

    public Promise(@NonNull PromiseSupplier<T> task, @NonNull Consumer<Runnable> threadConstructor) {
        this((handle) -> {
            T result = task.get();
            handle.resolve(result);
        }, threadConstructor);
    }

    public Promise(@NonNull PromiseRunnableWithHandle<T> task) {
        this(task, AsyncTask::create);
    }

    public Promise(@NonNull PromiseRunnableWithHandle<T> task, @NonNull Consumer<Runnable> threadConstructor) {
        threadConstructor.accept(() -> {
            try {
                task.run(this.resolver);
            } catch (Throwable t) {
                this.resolver.reject(t);
            }
        });
    }

    private Promise() {} // Used by withResolvers(), resolved(), and rejected().

    public static <T> PromiseResolver<T> withResolvers() {
        return new Promise<T>().resolver;
    }

    public static <T> Promise<T> resolve(@Nullable T value) {
        PromiseResolver<T> resolver = new Promise<T>().resolver;
        resolver.resolve(value);
        return resolver.promise;
    }

    public static <T> Promise<T> reject(@NonNull Throwable error) {
        PromiseResolver<T> resolver = new Promise<T>().resolver;
        resolver.reject(error);
        return resolver.promise;
    }

    /* ---------------- */
    /* State            */
    /* ---------------- */

    public PromiseState getState() {
        if (this.result == null) {
            return PromiseState.PENDING;
        } else if (this.result.rejected) {
            return PromiseState.REJECTED;
        } else {
            return PromiseState.FULFILLED;
        }
    }

    public boolean isSettled() {
        return this.result != null;
    }

    public boolean isPending() {
        return this.result == null;
    }

    @Override
    public String toString() {
        if (this.result == null) {
            return "Promise {<pending>}";
        } else if (this.result.rejected) {
            return "Promise {<rejected>: " + this.result.result + "}";
        } else {
            return "Promise {<fulfilled>: " + this.result.result + "}";
        }
    }

    /* ---------------- */
    /* .await() chaining */
    /* ---------------- */

    /**
     * Returns the result of this Promise, waiting for it to complete if necessary.
     * 
     * @return                      A value, if fulfilled.
     * 
     * @throws Throwable            if the Promise rejected.
     * @throws InterruptedException if this Thread is interrupted whilst awaiting
     *                              the result.
     */
    public T await() throws Throwable {
        synchronized (this.lock) {
            if (this.result == null) {
                this.lock.wait();
            }
        }
        if (this.result.rejected) {
            throw (Throwable) this.result.result;
        } else {
            return (T) this.result.result;
        }
    }

    /* ---------------- */
    /* .then() chaining */
    /* ---------------- */

    /**
     * Asynchronously executes the handler handler with the resolved value when this
     * Promise fulfills.
     * 
     * @return a sub-Promise which will either fulfill with the return value of this
     *         handler or will reject with any exception that is either thrown by
     *         the handler or from the rejected parent Promise.
     */
    public <R> Promise<R> then(@NonNull PromiseFunction<T, R> handler) {
        PromiseResolver<R> subpromiseResolver = withResolvers();
        this.chainTo((result) -> {
            if (result.rejected) {
                subpromiseResolver.reject((Throwable) result.result);
                return;
            }
            try {
                R handlerResult = handler.apply((T) result.result);
                subpromiseResolver.resolve(handlerResult);
            } catch (Throwable t) {
                subpromiseResolver.reject(t);
            }
        });
        return subpromiseResolver.promise;
    }

    /**
     * Asynchronously executes the handler handler with the resolved value when this
     * Promise fulfills.
     * 
     * @return a sub-Promise which will either fulfill with the return value of this
     *         handler or will reject with any exception that is either thrown by
     *         the handler or from the rejected parent Promise.
     */
    public <R> Promise<R> then(@NonNull PromiseSupplier<R> handler) {
        return this.then((_unused) -> {
            return handler.get();
        });
    }

    /**
     * Asynchronously executes the handler handler with the resolved value when this
     * Promise fulfills.
     * 
     * @return   a sub-Promise which will either fulfill with the return value of
     *           this handler or will reject with any exception that is either
     *           thrown by the handler or from the rejected parent Promise.
     * 
     * @implSpec The sub-Promise will always return null.
     */
    public Promise<Void> then(@NonNull PromiseRunnable handler) {
        return this.then((_unused) -> {
            handler.run();
            return null;
        });
    }

    /**
     * Asynchronously executes the handler handler with the resolved value when this
     * Promise fulfills.
     * 
     * @return   a sub-Promise which will either fulfill with the return value of
     *           this handler or will reject with any exception that is either
     *           thrown by the handler or from the rejected parent Promise.
     * 
     * @implSpec The sub-Promise will always return null.
     */
    public Promise<Void> then(@NonNull PromiseConsumer<T> handler) {
        return this.then((value) -> {
            handler.accept(value);
            return null;
        });
    }

    /* ---------------- */
    /* .except() chaining */
    /* ---------------- */

    /**
     * Asynchronously executes the handler handler with the rejected Throwable when
     * this Promise rejects.
     * 
     * @return   a sub-Promise which will either fulfill with either the return
     *           value of this handler or from the fulfilled parent Promise or will
     *           reject with any exception that is thrown by the handler.
     * 
     * @implNote Due to type-strictness, the resulting Promise when fulfilled either
     *           be of type R or T. You will have to check yourself and cast it to
     *           the appropriate type.
     */
    public <R> Promise<?> except(@NonNull PromiseFunction<Throwable, R> handler) {
        PromiseResolver<Object> subpromiseResolver = withResolvers();
        this.chainTo((result) -> {
            try {
                if (result.rejected) {
                    Object handlerResult = handler.apply((Throwable) result.result);
                    subpromiseResolver.resolve(handlerResult);
                } else {
                    subpromiseResolver.resolve(result.result);
                }
            } catch (Throwable t) {
                subpromiseResolver.reject(t);
            }
        });
        return subpromiseResolver.promise;
    }

    /**
     * Asynchronously executes the handler handler with the rejected Throwable when
     * this Promise rejects.
     * 
     * @return   a sub-Promise which will either fulfill with either the return
     *           value of this handler or from the fulfilled parent Promise or will
     *           reject with any exception that is thrown by the handler.
     * 
     * @implNote Due to type-strictness, the resulting Promise when fulfilled either
     *           be of type Void or T. You will have to check yourself and cast it
     *           to the appropriate type.
     */
    public Promise<?> except(@NonNull PromiseConsumer<Throwable> handler) {
        return this.except((throwable) -> {
            handler.accept(throwable);
            return null;
        });
    }

    /* ---------------- */
    /* .thenFinally() chaining */
    /* ---------------- */

    /**
     * Asynchronously executes the handler handler when this Promise fulfills or
     * rejects.
     * 
     * @return a sub-Promise which will either fulfill with the return value of this
     *         handler or will reject with any exception that is thrown by the
     *         handler.
     */
    public <R> Promise<R> thenFinally(@NonNull PromiseSupplier<R> handler) {
        PromiseResolver<R> subpromiseResolver = withResolvers();
        this.chainTo((_unused) -> {
            try {
                R handlerResult = handler.get();
                subpromiseResolver.resolve(handlerResult);
            } catch (Throwable t) {
                subpromiseResolver.reject(t);
            }
        });
        return subpromiseResolver.promise;
    }

    /**
     * Asynchronously executes the handler handler when this Promise fulfills or
     * rejects.
     * 
     * @return   a sub-Promise which will either fulfill with the return value of
     *           this handler (always null/void) or will reject with any exception
     *           that is thrown by the handler.
     * 
     * @implSpec The sub-Promise will always return null.
     */
    public Promise<Void> thenFinally(@NonNull PromiseRunnable handler) {
        return this.thenFinally(() -> {
            handler.run();
            return null;
        });
    }

    /* ---------------- */
    /* Static all()     */
    /* ---------------- */

    /**
     * @return   A Promise which will either fulfill with all of the results of the
     *           provided <i>promises</i> or reject if any of the <i>promises</i>
     *           reject.
     * 
     * @implNote A null entry in <i>promises</i> will always resolve with null.
     */
    public static Promise<Object[]> all(@NonNull Promise<?>... promises) {
        if (promises.length == 0) {
            return Promise.resolve(new Object[0]);
        }

        return new Promise<>(() -> {
            Object[] results = new Object[promises.length];
            for (int idx = 0; idx < promises.length; idx++) {
                if (promises[idx] == null) continue; // Skip it.
                results[idx] = promises[idx].await(); // This'll throw and reject this whole chain.
            }
            return results;
        });
    }

    /**
     * @return   A Promise which will either fulfill with all of the results of the
     *           provided <i>promises</i> or reject if any of the <i>promises</i>
     *           reject.
     * 
     * @implNote A null entry in <i>promises</i> will always resolve with null.
     */
    public static Promise<Object[]> all(@NonNull Collection<Promise<?>> promises) {
        return all(promises.toArray(new Promise<?>[0]));
    }

    /* ---------------- */
    /* Static allSettled() */
    /* ---------------- */

    /**
     * @return   A Promise which will fulfill when all of the <i>promises</i>
     *           settle.
     * 
     * @implNote The <i>promises</i> parameter is passed back to you transparently.
     */
    public static Promise<Promise<?>[]> allSettled(@NonNull Promise<?>... promises) {
        if (promises.length == 0) {
            return Promise.resolve(new Promise<?>[0]);
        }

        return new Promise<>(() -> {
            for (int idx = 0; idx < promises.length; idx++) {
                if (promises[idx] == null) continue; // Skip it.
                try {
                    promises[idx].await();
                } catch (Throwable ignored) {}
            }
            return promises;
        });
    }

    /**
     * @return   A Promise which will fulfill when all of the <i>promises</i>
     *           settle.
     * 
     * @implNote The <i>promises</i> parameter is passed back to you transparently.
     */
    public static Promise<Promise<?>[]> allSettled(@NonNull Collection<Promise<?>> promises) {
        return allSettled(promises.toArray(new Promise<?>[0]));
    }

    /* ---------------- */
    /* Static race()    */
    /* ---------------- */

    /**
     * @return A Promise which will settle with the value of the first provided
     *         <i>promises</i> that settles.
     */
    public static Promise<?> race(@NonNull Promise<?>... promises) {
        if (promises.length == 0) {
            return Promise.resolve(null);
        }

        return new Promise<>((resolver) -> {
            for (Promise<?> promise : promises) {
                if (promise == null) {
                    resolver.reject(new IllegalArgumentException("A null Promise was provided."));
                }

                promise
                    .then((v) -> {
                        try {
                            resolver.resolve(v);
                        } catch (IllegalStateException ignored) {} // Already settled.
                    })
                    .except((t) -> {
                        try {
                            resolver.reject(t);
                        } catch (IllegalStateException ignored) {} // Already settled.
                    });
            }
        });
    }

    /**
     * @return A Promise which will settle with the value of the first provided
     *         <i>promises</i> that settles.
     */
    public static Promise<?> race(@NonNull Collection<Promise<?>> promises) {
        return race(promises.toArray(new Promise<?>[0]));
    }

    /* ---------------- */
    /* Static any()     */
    /* ---------------- */

    /**
     * @return A Promise which will settle with the value of the first provided
     *         <i>promises</i> that settles or will reject if all Promises have
     *         rejected.
     */
    public static Promise<?> any(@NonNull Promise<?>... promises) {
        if (promises.length == 0) {
            return Promise.reject(new Exception("All Promises have rejected."));
        }

        return new Promise<>((resolver) -> {
            AtomicInteger rejectedCountDown = new AtomicInteger(promises.length);

            for (Promise<?> promise : promises) {
                if (promise == null) {
                    resolver.reject(new IllegalArgumentException("A null Promise was provided."));
                }

                promise
                    .then((v) -> {
                        try {
                            resolver.resolve(v);
                        } catch (IllegalStateException ignored) {} // Already settled.
                    })
                    .except((t) -> {
                        if (rejectedCountDown.decrementAndGet() == 0) {
                            resolver.reject(new Exception("All Promises have rejected."));
                        }
                    });
            }
        });
    }

    /**
     * @return A Promise which will settle with the value of the first provided
     *         <i>promises</i> that settles.
     */
    public static Promise<?> any(@NonNull Collection<Promise<?>> promises) {
        return race(promises.toArray(new Promise<?>[0]));
    }

}
