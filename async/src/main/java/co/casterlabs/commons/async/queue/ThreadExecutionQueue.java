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
import co.casterlabs.commons.async.promise.Promise;
import co.casterlabs.commons.async.queue.ThreadExecutionQueue.Impl;
import lombok.NonNull;
import lombok.SneakyThrows;

/**
 * ThreadQueue simplifies running thread-critical code. (It synchronizes the
 * thread of execution)
 * 
 * @see {@link SyncExecutionQueue} if you only need to synchronize the timing of
 *      execution.
 */
@SuppressWarnings("unused")
public class ThreadExecutionQueue implements ExecutionQueue {
    private Impl impl;

    /**
     * Creates a new ThreadQueue with a new execution thread (the default
     * implementation).
     * 
     * @see {@link #ThreadQueue(Impl)}
     */
    public ThreadExecutionQueue() {
        this(new DefaultImpl());
    }

    /**
     * Creates a new ThreadQueue with the given implementation.
     * 
     * @see {@link #ThreadQueue()} for the default behavior.
     */
    public ThreadExecutionQueue(@NonNull Impl impl) {
        this.impl = impl;
    }

    /* ---------------- */
    /* Task Submission */
    /* ---------------- */

    @Override
    public void execute(@NonNull Runnable task) {
        if (this.isMainThread()) {
            task.run();
        }

        this.impl.submitTask(task);
    }

    @SneakyThrows // For the Promise `Throwable`.
    @Override
    public <T> T execute(Supplier<T> task) {
        if (this.isMainThread()) {
            return task.get();
        }

        try {
            return this.executeWithPromise(task).await();
        } catch (InterruptedException e) {
            // Silently pass the interrupt.
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Override
    public <T> Promise<T> executeWithPromise(@NonNull Supplier<T> task) {
        if (this.isMainThread()) {
            try {
                return Promise.resolve(task.get());
            } catch (Throwable t) {
                return Promise.reject(t);
            }
        }

        return new Promise<T>((_unused) -> task.get(), this::execute);
    }

    /* ---------------- */
    /* Helpers */
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
    /* Implementation */
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
