/* 
Copyright 2022 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.async;

import lombok.NonNull;

public class AsyncTask {
    private static int taskId = 0;

    private Thread t;

    private AsyncTask(@NonNull Runnable run, boolean daemon) {
        this.t = new Thread(run);

        this.t.setName("AsyncTask #" + taskId++);
        this.t.setDaemon(daemon);
        this.t.start();
    }

    /**
     * Cancels the task if not already completed.
     */
    @SuppressWarnings("deprecation")
    public void cancel() {
        if (this.t.isAlive()) {
            this.t.interrupt();
            this.t.stop();
        }
    }

    /**
     * Starts a new async task.
     *
     * @param    run the task to run
     * 
     * @implNote     The spawned thread will be a daemon thread.
     */
    public static AsyncTask create(@NonNull Runnable run) {
        return new AsyncTask(run, true);
    }

    /**
     * Starts a new async task.
     *
     * @param    run the task to run
     * 
     * @implNote     The spawned thread will be a non-daemon thread.
     */
    public static AsyncTask createNonDaemon(@NonNull Runnable run) {
        return new AsyncTask(run, false);
    }

}
