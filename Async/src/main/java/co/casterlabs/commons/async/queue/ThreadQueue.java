/* 
Copyright 2022 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.async.queue;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.commons.async.Promise;
import lombok.NonNull;

/**
 * ThreadQueue simplifies running thread-critical code. (It synchronizes the
 * thread of execution)
 * 
 * @see {@link SyncQueue} if you only need to synchronize the timing of
 *      execution.
 */
public class ThreadQueue {
    private Impl impl;

    /**
     * Creates a new ThreadQueue with a new execution thread (the default
     * implementation).
     * 
     * @see {@link #ThreadQueue(Impl)}
     */
    public ThreadQueue() {
        this(new DefaultImpl());
    }

    /**
     * Creates a new ThreadQueue with the given implementation.
     * 
     * @see {@link #ThreadQueue()} for the default behavior.
     */
    public ThreadQueue(@NonNull Impl impl) {
        this.impl = impl;
    }

    /* ---------------- */
    /* Task Submission  */
    /* ---------------- */

    /**
     * Submits a task to the thread without waiting for completion.
     * 
     * @param    task the task to submit.
     * 
     * @see           {@link #submitTaskAndWait(Runnable)} if you wish to wait for
     *                completion.
     * 
     * @implNote      This method will synchronously call the task if the current
     *                thread IS the execution thread.
     */
    public void submitTask(@NonNull Runnable task) {
        if (isMainThread()) {
            task.run();
        } else {
            this.impl.submitTask(task);
        }
    }

    /**
     * Submits a task to the thread and waits for completion.
     * 
     * @param    task the task to submit.
     * 
     * @see           {@link #submitTaskAndWait(Runnable)} if you don't wish to wait
     *                for completion.
     * 
     * @implNote      This method will synchronously call the task if the current
     *                thread IS the execution thread.
     */
    public void submitTaskAndWait(@NonNull Runnable task) throws InterruptedException, Throwable {
        this.submitTaskWithPromise(task).await();
    }

    /**
     * Submits a task to the thread, returning a {@link Promise}.
     * 
     * @param    task the task to submit.
     * 
     * @implNote      This method will synchronously call the task if the current
     *                thread IS the execution thread.
     */
    public Promise<Void> submitTaskWithPromise(@NonNull Runnable task) {
        return this.submitTaskWithPromise(() -> {
            task.run();
            return null;
        });
    }

    /**
     * Submits a task to the thread, returning a {@link Promise}.
     * 
     * @param    task the task to submit.
     * 
     * @implNote      This method will synchronously call the task if the current
     *                thread IS the execution thread.
     */
    public <T> Promise<T> submitTaskWithPromise(@NonNull Supplier<T> task) {
        if (isMainThread()) {
            try {
                return Promise.newResolved(task.get());
            } catch (Throwable t) {
                return Promise.newRejected(t);
            }
        } else {
            return new Promise<T>(() -> task.get(), this::submitTask);
        }
    }

    /* ---------------- */
    /* Helpers          */
    /* ---------------- */

    /**
     * Executes the given task off of the main thread, either synchronously or in a
     * new thread. This allows you to avoid potentially blocking the main thread for
     * long-running tasks.
     * 
     * @param    task the task to execute off of the main thread.
     * 
     * @implNote      The spawned thread will be a daemon thread.
     */
    public void executeOffOfMainThread(@NonNull Runnable task) {
        if (isMainThread()) {
            AsyncTask.create(task);
        } else {
            task.run();
        }
    }

    public boolean isMainThread() {
        return Thread.currentThread() == this.impl.getThread();
    }

    /**
     * Asserts that the current thread is the ThreadQueue's main thread.
     * 
     * @throws IllegalAccessException
     */
    public void assertMainThread() {
        if (Thread.currentThread() != this.impl.getThread()) {
            new IllegalAccessException("This call must be made from the main thread.");
        }
    }

    /* ---------------- */
    /* Implementation   */
    /* ---------------- */

    /**
     * This is the underlying implementation interface for ThreadQueue. Useful for
     * creating interop with native APIs like EclipseSWT or Rococoa.
     */
    public interface Impl {

        /**
         * @return the main thread, or Thread.currentThread() if unsure.
         */
        public Thread getThread();

        /**
         * Submits the given task to your implementation.
         */
        public void submitTask(@NonNull Runnable task);

    }

    private static final class DefaultImpl implements Impl {
        private Thread thread;
        private Deque<Runnable> taskQueue;
        private Object logicLock;

        private DefaultImpl() {
            this.thread = new Thread(this::_logic);
            this.thread.start();
        }

        private void _logic() {
            // Create the resource on THIS thread.
            this.logicLock = new Object();
            this.taskQueue = new ArrayDeque<>();

            while (true) {
                while (!this.taskQueue.isEmpty()) {
                    try {
                        Runnable popped = this.taskQueue.pop();

                        try {
                            popped.run();
                        } catch (Throwable t) {
                            System.err.println("An exception occurred whilst processing task in the queue:");
                            t.printStackTrace();
                        }
                    } catch (NoSuchElementException ignored) {}
                }

                try {
                    Thread.yield(); // The thread may lie dormant for a while.

                    synchronized (this.logicLock) {
                        // Sleep until we get another task.
                        this.logicLock.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public Thread getThread() {
            return this.thread;
        }

        @Override
        public void submitTask(@NonNull Runnable task) {
            this.taskQueue.add(task);

            synchronized (this.logicLock) {
                this.logicLock.notify();
            }
        }

    }

}
