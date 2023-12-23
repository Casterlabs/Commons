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

public class PromiseFunctionalInterface {

    @FunctionalInterface
    public static interface PromiseRunnableWithHandle<T> {
        public void run(PromiseResolver<T> resolver) throws Throwable;
    }

    @FunctionalInterface
    public static interface PromiseRunnable {
        public void run() throws Throwable;
    }

    @FunctionalInterface
    public interface PromiseSupplier<T> {
        public T get() throws Throwable;
    }

    @FunctionalInterface
    public static interface PromiseConsumer<T> {
        public void accept(@Nullable T result) throws Throwable;
    }

    @FunctionalInterface
    public static interface PromiseFunction<T, R> {
        public @Nullable R apply(@Nullable T result) throws Throwable;
    }

}
