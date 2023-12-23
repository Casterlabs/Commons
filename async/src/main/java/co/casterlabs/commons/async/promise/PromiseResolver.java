/* 
Copyright 2023 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
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
