/* 
Copyright 2025 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.io.bytes;

/**
 * Used to calculate the size of a marshalled object. All methods are chainable.
 * Use {@link #result()} to get the result.
 */
public class ByteSizer {
    private int result = 0;

    public int result() {
        return this.result;
    }

    public ByteSizer bytes(int len) {
        this.result += len;
        return this;
    }

    public ByteSizer b8() {
        this.result += 1;
        return this;
    }

    public ByteSizer b16() {
        this.result += 2;
        return this;
    }

    public ByteSizer b24() {
        this.result += 3;
        return this;
    }

    public ByteSizer b32() {
        this.result += 4;
        return this;
    }

    public ByteSizer b64() {
        this.result += 8;
        return this;
    }

    public ByteSizer flt() {
        this.result += 4;
        return this;

    }

    public ByteSizer dbl() {
        this.result += 8;
        return this;
    }

}
