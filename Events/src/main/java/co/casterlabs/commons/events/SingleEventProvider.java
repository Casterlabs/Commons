/* 
Copyright 2022 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.events;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import lombok.NonNull;

public class SingleEventProvider<T> {
    private Map<Integer, Consumer<T>> listeners = new HashMap<>();

    /* on */

    public synchronized int on(@NonNull Consumer<T> listener) {
        int id = listener.hashCode();
        this.listeners.put(id, listener);

        return id;
    }

    public synchronized int on(@NonNull Runnable listener) {
        return this.on((aVoid) -> listener.run());
    }

    /* off */

    public synchronized void off(@NonNull Consumer<T> listener) {
        this.off(listener.hashCode());
    }

    public synchronized void off(@NonNull Runnable listener) {
        this.off(listener.hashCode());
    }

    public synchronized void off(int id) {
        this.listeners.remove(id);
    }

    /* Firing */

    public synchronized void fireEvent(@Nullable T data) {
        for (Consumer<T> listener : this.listeners.values()) {
            try {
                listener.accept(data);
            } catch (Throwable t) {
                System.err.println("An exception occurred whilst firing event:");
                t.printStackTrace();
            }
        }
    }

}
