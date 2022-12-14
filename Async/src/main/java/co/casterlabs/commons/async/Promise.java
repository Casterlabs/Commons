/* 
Copyright 2022 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.async;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * A much simpler version of {@link java.util.concurrent.CompletableFuture},
 * more akin to Javascript's Promise.
 */
public class Promise<T> {
    private final Object awaitLock = new Object();

    @Getter
    @Accessors(fluent = true)
    private volatile boolean hasCompleted;

    private T result;
    private Throwable err;

    private Consumer<T> thenHandler;
    private Consumer<Throwable> catcherHandler = (t) -> t.printStackTrace();

    /**
     * Creates the promise, executing the resolver in a new non-daemon thread.
     * 
     * @param    resolver The handler which resolves.
     * 
     * @implNote          The spawned thread will be a non-daemon thread.
     */
    public Promise(@NonNull PromiseSupplier<T> resolver) {
        this(resolver, AsyncTask::createNonDaemon);
    }

    /**
     * Creates the promise, executing the resolver in the given thread handler.
     * 
     * @param resolver     The handler which resolves.
     * @param threadSubmit the thread handler to execute in.
     */
    public Promise(@NonNull PromiseSupplier<T> resolver, @NonNull Consumer<Runnable> threadSubmit) {
        threadSubmit.accept(() -> {
            try {
                T result = resolver.get();

                this.resolve(result);
            } catch (Throwable t) {
                this.reject(t);
            }
        });
    }

    /**
     * Creates the promise, executing the handler in a new non-daemon thread.
     * 
     * @param    handler The handler which resolves or rejects.
     * 
     * @implNote         The spawned thread will be a non-daemon thread.
     */
    public Promise(@NonNull BiConsumer<Consumer<T>, Consumer<Throwable>> handler) {
        AsyncTask.createNonDaemon(() -> {
            try {
                handler.accept(this::resolve, this::reject);
            } catch (Throwable t) {
                this.reject(t);
            }
        });
    }

    /* ---------------- */
    /* Completion       */
    /* ---------------- */

    protected void resolve(T result) {
        this.result = result;
        this.hasCompleted = true;

        synchronized (this.awaitLock) {
            this.awaitLock.notifyAll();
        }

        if (this.thenHandler != null) {
            this.thenHandler.accept(this.result);
        }

        this.thenHandler = null;
        this.catcherHandler = null;
    }

    protected void reject(Throwable err) {
        this.err = err;
        this.hasCompleted = true;

        synchronized (this.awaitLock) {
            this.awaitLock.notifyAll();
        }

        if (this.catcherHandler != null) {
            this.catcherHandler.accept(this.err);
        }

        this.thenHandler = null;
        this.catcherHandler = null;
    }

    /* ---------------- */
    /* Handling         */
    /* ---------------- */

    /**
     * Executes the given handler if the Promise resolves.
     * 
     * @param handler the handler to execute.
     */
    public void then(Consumer<T> handler) {
        this.thenHandler = handler;

        if (this.hasCompleted && this.completedSuccessfully()) {
            this.thenHandler.accept(this.result);
        }
    }

    /**
     * Executes the given handler if the Promise rejects.
     * 
     * @param handler the handler to execute.
     */
    public void except(Consumer<Throwable> handler) {
        this.catcherHandler = handler;

        if (this.hasCompleted && !this.completedSuccessfully()) {
            this.catcherHandler.accept(this.err);
        }
    }

    /**
     * Awaits the result, either returning the value or throwing the reject.
     *
     * @return                      the resolved result
     * 
     * @throws Throwable            the throwable that caused the Promise to reject
     * @throws InterruptedException the interrupted exception
     */
    public T await() throws Throwable, InterruptedException {
        if (!this.hasCompleted) {
            synchronized (this.awaitLock) {
                this.awaitLock.wait();
            }
        }

        if (this.completedSuccessfully()) {
            return this.result;
        } else {
            throw this.err;
        }
    }

    /**
     * @return true if the Promise completed successfully (did not throw/reject),
     *         false if it rejected.
     */
    public boolean completedSuccessfully() {
        return this.err == null;
    }

    /* ---------------- */
    /* Static Helpers   */
    /* ---------------- */

    protected Promise() {}; // Also used in PromiseWithHandles.

    /**
     * Returns a promise that immediately resolves with the result.
     * 
     * @param  result the result to resolve with
     * 
     * @return        the promise
     */
    public static <T> Promise<T> newResolved(@Nullable T result) {
        Promise<T> promise = new Promise<>();
        promise.resolve(result);
        return promise;
    }

    /**
     * Returns a promise that immediately rejects with the result.
     * 
     * @param  err the Throwable to reject with
     * 
     * @return     the promise
     */
    public static <T> Promise<T> newRejected(@NonNull Throwable err) {
        Promise<T> promise = new Promise<>();
        promise.reject(err);
        return promise;
    }

    /* ---------------- */
    /* ---------------- */
    /* ---------------- */

    @FunctionalInterface
    public static interface PromiseSupplier<T> {
        public T get() throws Throwable;
    }

}
