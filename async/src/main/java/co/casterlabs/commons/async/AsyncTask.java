/* 
Copyright 2022 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import lombok.NonNull;

/**
 * @apiNote It is probably better that you use Thread.ofVirtual() if your JVM
 *          supports it.
 */
public class AsyncTask {
    private static final ExecutorService DAEMON_THREAD_POOL = new ThreadPoolExecutor(
        0, Integer.MAX_VALUE,
        5, TimeUnit.SECONDS,
        new SynchronousQueue<Runnable>(),
        (r) -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("Async Task - Daemon Thread Pool Executor");
            return thread;
        }
    );
    private static final ExecutorService NONDAEMON_THREAD_POOL = new ThreadPoolExecutor(
        0, Integer.MAX_VALUE,
        5, TimeUnit.SECONDS,
        new SynchronousQueue<Runnable>(),
        (r) -> {
            Thread thread = new Thread(r);
            thread.setDaemon(false);
            thread.setName("Async Task - Non-Daemon Thread Pool Executor");
            return thread;
        }
    );

    private Future<?> future;

    private AsyncTask(@NonNull Future<?> future) {
        this.future = future;
    }

    /**
     * Cancels the task if not already completed.
     */
    public void cancel() {
        this.future.cancel(true);
    }

    /**
     * Starts a new async task.
     *
     * @param    run the task to run
     * 
     * @implNote     The spawned thread will be a daemon thread.
     */
    public static AsyncTask create(@NonNull Runnable run) {
        return new AsyncTask(DAEMON_THREAD_POOL.submit(run));
    }

    /**
     * Starts a new async task.
     *
     * @param    run the task to run
     * 
     * @implNote     The spawned thread will be a non-daemon thread.
     */
    public static AsyncTask createNonDaemon(@NonNull Runnable run) {
        return new AsyncTask(NONDAEMON_THREAD_POOL.submit(run));
    }

}
