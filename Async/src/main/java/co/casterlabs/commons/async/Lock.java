/* 
Copyright 2022 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.async;

import java.util.function.Supplier;

public class Lock {
    private final Object lock = new Object();
    private int executing = 0;

    public void execute(Runnable run) throws InterruptedException {
        this.execute(() -> {
            run.run();
            return null;
        });
    }

    public synchronized <T> T execute(Supplier<T> task) throws InterruptedException {
        boolean shouldWait = this.executing > 0;
        this.executing++;

        if (shouldWait) {
            synchronized (this.lock) {
                this.lock.wait();
            }
        }

        try {
            return task.get();
        } finally {
            this.executing--;

            synchronized (this.lock) {
                this.lock.notify();
            }
        }
    }

}
