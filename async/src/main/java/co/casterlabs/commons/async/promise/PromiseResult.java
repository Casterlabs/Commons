package co.casterlabs.commons.async.promise;

import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
class PromiseResult {
    public final boolean rejected;
    public final Object result; // Either a throwable or T, depending on #rejected.

    static PromiseResult resolve(@Nullable Object v) {
        return new PromiseResult(false, v);
    }

    static PromiseResult reject(@NonNull Throwable t) {
        return new PromiseResult(true, t);
    }
}
