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

    public void then(Consumer<T> then) {
        this.then = then;

        if (this.isCompleted && this.hasCompletedSuccessfully()) {
            this.then.accept(this.result);
        }
    }

    public void except(Consumer<Throwable> catcher) {
        this.catcher = catcher;

        if (this.isCompleted && !this.hasCompletedSuccessfully()) {
            this.catcher.accept(this.err);
        }
    }

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

    public static <T> Promise<T> resolved(@Nullable T result) {
        Promise<T> promise = new Promise<>();
        promise.resolve(result);
        return promise;
    }

    public static <T> Promise<T> rejected(@NonNull Throwable err) {
        Promise<T> promise = new Promise<>();
        promise.reject(err);
        return promise;
    }

}
