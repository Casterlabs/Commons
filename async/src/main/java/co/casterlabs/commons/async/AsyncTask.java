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
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import lombok.NonNull;

public class AsyncTask {
    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

    private Future<?> future;

    private AsyncTask(@NonNull Runnable run) {
        this.future = THREAD_POOL.submit(run);
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
        return new AsyncTask(run);
    }

}
