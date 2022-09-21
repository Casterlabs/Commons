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
