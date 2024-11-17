/* 
Copyright 2024 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.async;

import java.util.concurrent.locks.ReentrantLock;

import lombok.SneakyThrows;

/**
 * Similar to the synchronized keyword, but with the added safety when using
 * Virtual Threads.
 */
public class Lock {
    private final ReentrantLock lock = new ReentrantLock();

    @SneakyThrows
    public void execute(LockRunnable run) {
        this.lock.lock();
        try {
            run.run();
        } finally {
            this.lock.unlock();
        }
    }

    @SneakyThrows
    public <T> T execute(LockSupplier<T> supp) {
        this.lock.lock();
        try {
            return supp.get();
        } finally {
            this.lock.unlock();
        }
    }

    @FunctionalInterface
    public static interface LockRunnable {
        public void run() throws Throwable;
    }

    @FunctionalInterface
    public static interface LockSupplier<T> {
        public T get() throws Throwable;
    }

}
