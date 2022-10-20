package co.casterlabs.commons.async.queue;

import java.util.function.Supplier;

import co.casterlabs.commons.async.Promise;

/**
 * An interface for implementing a queue that guarantees an order-of-execution.
 * 
 * @see {@link SyncQueue}
 * @see {@link ThreadQueue}
 */
public interface ExecutionQueue {

    /**
     * Submits the given task and waits for completion, returning the value returned
     * by the supplier.
     * 
     * @param task the task to execute.
     * 
     * @return the value returned by the task.
     * 
     * @throws Any Throwable thrown by task, it is assumed that you know the
     *             exceptions in advance.
     */
    public <T> T execute(Supplier<T> task);

    /**
     * Submits the given task and waits for completion.
     * 
     * @param task the task to execute.
     * 
     * @throws Any Throwable thrown by task, it is assumed that you know the
     *             exceptions in advance.
     */
    default void execute(Runnable task) {
        this.execute(() -> {
            task.run();
            return null;
        });
    }

    /**
     * Submits the given task and waits for completion, returning a {@link Promise}
     * that
     * will either resolve with the value returned by the supplier or reject with
     * any thrown exceptions.
     * 
     * @param task the task to execute.
     * 
     * @return a completion {@link Promise} the task.
     */
    default <T> Promise<T> executeWithPromise(Supplier<T> task) {
        return new Promise<>(() -> {
            return this.execute(task);
        });
    }

}
