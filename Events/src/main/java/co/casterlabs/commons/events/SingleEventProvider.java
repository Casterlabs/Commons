/* 
Copyright 2022 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.events;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import lombok.NonNull;

/**
 * A helper class for creating event-driven architecture.
 */
public class SingleEventProvider<D> {
    private Map<Integer, Consumer<D>> listeners = new ConcurrentHashMap<>();

    /* ---------------- */
    /* On               */
    /* ---------------- */

    /**
     * Registers a {@link Consumer} which accepts an event when the provider fires.
     * 
     * @param  listener the listener to register
     * 
     * @return          the registration id, to be used with {@link #off(int)}.
     */
    public int on(@NonNull Consumer<D> listener) {
        int id = ThreadLocalRandom.current().nextInt();
        this.listeners.put(id, listener);

        return id;
    }

    /**
     * Registers a {@link Runnable} which gets executed when the provider fires.
     * 
     * @param  handler the handler to register
     * 
     * @return         the registration id, to be used with {@link #off(int)}.
     */
    public int on(@NonNull Runnable handler) {
        // Secretly, this just wraps #on(Consumer).
        return this.on((aVoid) -> handler.run());
    }

    /* ---------------- */
    /* Off              */
    /* ---------------- */

    /**
     * Unregisters a previously registered event handler.
     * 
     * @param id The id given to you after calling #on().
     */
    public void off(int id) {
        this.listeners.remove(id);
    }

    /* ---------------- */
    /* Firing           */
    /* ---------------- */

    /**
     * Fires an event, which can be null, to all registered listeners. Any error
     * generated during fire is printed to stderr and swallowed.
     */
    public synchronized void fireEvent(@Nullable D data) {
        for (Consumer<D> listener : this.listeners.values()) {
            try {
                listener.accept(data);
            } catch (Throwable ex) {
                System.err.println("An exception occurred whilst firing event:");
                ex.printStackTrace();
            }
        }
    }

}
