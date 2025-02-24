/* 
Copyright 2025 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.async;

import java.util.concurrent.locks.ReentrantLock;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LockableResource<T> {
    private final ReentrantLock lock = new ReentrantLock();

    private volatile T resource;

    public void set(T resource) {
        this.lock.lock();
        try {
            this.resource = resource;
        } finally {
            this.lock.unlock();
        }
    }

    public T acquire() {
        this.lock.lock();
        return this.resource;
    }

    public T acquireUnsafe() {
        return this.resource;
    }

    public void release() {
        this.lock.unlock();
    }

}
