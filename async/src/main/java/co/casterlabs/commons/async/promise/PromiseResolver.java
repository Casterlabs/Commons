package co.casterlabs.commons.async.promise;

import org.jetbrains.annotations.Nullable;

import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public abstract class PromiseResolver<T> {
    public final Promise<T> promise;

    protected abstract void handle(@NonNull PromiseResult result);

    public final void resolve(@Nullable T value) {
        this.handle(PromiseResult.resolve(value));
    }

    public final void resolve() {
        this.handle(PromiseResult.resolve(null));
    }

    public final void reject(@NonNull Throwable error) {
        this.handle(PromiseResult.reject(error));
    }

    public final void reject() {
        this.handle(PromiseResult.reject(new Exception("Rejected")));
    }

}
