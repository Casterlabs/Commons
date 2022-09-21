/* 
Copyright 2022 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.async;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.NonNull;

public class Promise<T> {
    private final Object awaitLock = new Object();
    private @Getter boolean isCompleted;

    private T result;
    private Throwable err;

    private Consumer<T> then;
    private Consumer<Throwable> catcher = (t) -> t.printStackTrace();

    private Promise() {}; // See resolved and rejected.

    public Promise(@NonNull Runnable task) {
        this(
            () -> {
                task.run();
                return null;
            },
            AsyncTask::new
        );
    }

    public Promise(@NonNull Supplier<T> producer) {
        this(producer, AsyncTask::new);
    }

    public Promise(@NonNull Runnable task, @NonNull Consumer<Runnable> threadSubmit) {
        this(
            () -> {
                task.run();
                return null;
            },
            threadSubmit
        );
    }

    public Promise(@NonNull Supplier<T> producer, @NonNull Consumer<Runnable> threadSubmit) {
        threadSubmit.accept(() -> {
            try {
                T result = producer.get();

                this.resolve(result);
            } catch (Throwable t) {
                this.reject(t);
            }
        });
    }

    private void resolve(T result) {
        this.result = result;
        this.isCompleted = true;

        synchronized (this.awaitLock) {
            this.awaitLock.notifyAll();
        }

        if (this.then != null) {
            this.then.accept(this.result);
        }
    }

    private void reject(Throwable err) {
        this.err = err;
        this.isCompleted = true;

        synchronized (this.awaitLock) {
            this.awaitLock.notifyAll();
        }

        if (this.catcher != null) {
            this.catcher.accept(this.err);
        }
    }

    /**
     * Executes if the Promise resolves.
     */
    public void then(Consumer<T> then) {
        this.then = then;

        if (this.isCompleted && this.hasCompletedSuccessfully()) {
            this.then.accept(this.result);
        }
    }

    /**
     * Executes if the Promise rejects.
     */
    public void except(Consumer<Throwable> catcher) {
        this.catcher = catcher;

        if (this.isCompleted && !this.hasCompletedSuccessfully()) {
            this.catcher.accept(this.err);
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
        if (!this.isCompleted) {
            synchronized (this.awaitLock) {
                this.awaitLock.wait();
            }
        }

        if (this.hasCompletedSuccessfully()) {
            return this.result;
        } else {
            throw this.err;
        }
    }

    public boolean hasCompletedSuccessfully() {
        return this.err == null;
    }

    /**
     * Returns a promise that immediately resolves with the result.
     * 
     * @param  result the result to resolve with
     * 
     * @return        the promise
     */
    public static <T> Promise<T> resolved(@Nullable T result) {
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
    public static <T> Promise<T> rejected(@NonNull Throwable err) {
        Promise<T> promise = new Promise<>();
        promise.reject(err);
        return promise;
    }

}
