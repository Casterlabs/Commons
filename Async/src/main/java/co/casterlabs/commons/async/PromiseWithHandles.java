/* 
Copyright 2022 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.async;

/**
 * A weird version of {@link Promise}
 */
public class PromiseWithHandles<T> extends Promise<T> {

    public PromiseWithHandles() {
        super();
    }

    /**
     * Resolves the promise with the specified result.
     */
    @Override
    public void resolve(T result) {
        super.resolve(result);
    }

    /**
     * Rejects the promise with the specified error.
     */
    @Override
    public void reject(Throwable err) {
        super.reject(err);
    }

}
