/* 
Copyright 2022 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.async;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public class ThreadQueue {
    private Impl impl;

    public ThreadQueue() {
        this(new DefaultImpl());
    }

    public void submitTask(@NonNull Runnable task) {
        if (isMainThread()) {
            task.run();
        } else {
            this.impl.submitTask(task);
        }
    }

    public void submitTaskAndWait(@NonNull Runnable task) throws InterruptedException, Throwable {
        this.submitTaskWithPromise(task).await();
    }

    public Promise<Void> submitTaskWithPromise(@NonNull Runnable task) {
        return this.submitTaskWithPromise(() -> {
            task.run();
            return null;
        });
    }

    public <T> Promise<T> submitTaskWithPromise(@NonNull Supplier<T> task) {
        if (isMainThread()) {
            try {
                return Promise.resolved(task.get());
            } catch (Throwable t) {
                return Promise.rejected(t);
            }
        } else {
            return new Promise<T>(task, this::submitTask);
        }
    }

    public void executeOffOfMainThread(@NonNull Runnable task) {
        if (isMainThread()) {
            new AsyncTask(task);
        } else {
            task.run();
        }
    }

    public boolean isMainThread() {
        return Thread.currentThread() == this.impl.getThread();
    }

    /**
     * Asserts that the current thread is the ThreadQueue's thread.
     * 
     * @throws IllegalAccessException
     */
    public void assertThread() {
        if (Thread.currentThread() != this.impl.getThread()) {
            new IllegalAccessException("This call must be made from the main thread.");
        }
    }

    public interface Impl {

        public Thread getThread();

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
