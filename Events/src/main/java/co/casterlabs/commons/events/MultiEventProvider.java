package co.casterlabs.commons.events;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import lombok.NonNull;

public class MultiEventProvider<E extends Enum<?>, T> {
    private Map<E, Map<Integer, Consumer<T>>> listenerSections = new HashMap<>();
    private Set<Integer> allListeners = new HashSet<>();

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
