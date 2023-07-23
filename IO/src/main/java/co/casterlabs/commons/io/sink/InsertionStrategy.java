/* 
Copyright 2023 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.io.sink;

/**
 * @see {@link SinkBuffer}
 */
public enum InsertionStrategy {

    /**
     * If there is not enough space in the buffer then any additional data will be
     * discarded. This allows you to read a stream as fast as possible to prevent
     * congestion.
     * 
     * @see {@link SinkBuffer}
     */
    DROP_ON_OVERFLOW,

    /**
     * If there is not enough space in the buffer then insert() will block until
     * enough data is extracted to fit the remaining data.
     * 
     * @see {@link SinkBuffer}
     */
    BLOCK_ON_OVERFLOW,

    /**
     * If there is not enough space in the buffer then insert() will throw an
     * exception.
     * 
     * @see {@link SinkBuffer}
     * @see {@link BufferUnderrunError}
     */
    THROW_ON_OVERFLOW,

}
