/* 
Copyright 2022 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.events;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import lombok.NonNull;

public class MultiEventProvider<E extends Enum<?>, T> {
    private Map<E, Map<Integer, Consumer<T>>> listenerSections = new ConcurrentHashMap<>();
    private Set<Integer> allListeners = Collections.synchronizedSet(new HashSet<>());

    /* on */

    public synchronized int on(@NonNull E type, @NonNull Consumer<T> listener) {
        int id = listener.hashCode();
        Map<Integer, Consumer<T>> listenerSection = this.listenerSections.get(type);

        if (listenerSection == null) {
            listenerSection = new HashMap<>();
            this.listenerSections.put(type, listenerSection);
        }

        listenerSection.put(id, listener);
        this.allListeners.add(id);

        return id;
    }

    public synchronized int on(@NonNull E type, @NonNull Runnable listener) {
        return this.on(type, (aVoid) -> listener.run());
    }

    /* off */

    public synchronized void off(@NonNull Consumer<T> listener) {
        this.off(listener.hashCode());
    }

    public synchronized void off(@NonNull Runnable listener) {
        this.off(listener.hashCode());
    }

    public synchronized void off(int id) {
        this.allListeners.remove(id);

        for (Map<Integer, Consumer<T>> listenerSection : this.listenerSections.values()) {
            listenerSection.remove(id);
        }
    }

    /* Firing */

    public synchronized void fireEvent(@NonNull E type, @Nullable T data) {
        Map<Integer, Consumer<T>> listenerSection = this.listenerSections.get(type);

        if (listenerSection != null) {
            for (Consumer<T> listener : listenerSection.values()) {
                try {
                    listener.accept(data);
                } catch (Throwable t) {
                    System.err.println("An exception occurred whilst firing event:");
                    t.printStackTrace();
                }
            }
        }
    }

}
