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
public enum ExtractionStrategy {

    /**
     * If there is not enough data in the buffer then 0s will fill the remaining
     * data. This allows you to supply a stream as fast as possible to prevent
     * underruns or droughts in other streams.
     * 
     * @see {@link SinkBuffer}
     */
    NULL_ON_UNDERRUN,

    /**
     * If there is not enough data in the buffer then extract() will block until
     * enough data is available.
     * 
     * @see {@link SinkBuffer}
     */
    BLOCK_ON_UNDERRUN,

    /**
     * If there is not enough data in the buffer then extract() will throw an
     * exception and leave the buffer unconsumed.
     * 
     * @see {@link SinkBuffer}
     * @see {@link BufferUnderrunError}
     */
    THROW_ON_UNDERRUN,

    /**
     * If there is not enough data in the buffer then extract() will continue
     * looping over the buffer until the correct amount of data is read.
     * 
     * @see {@link SinkBuffer}
     */
    LOOP_ON_UNDERRUN,

    /**
     * If there is not enough data in the buffer then extract() will only retrieve
     * the total amount of data available.
     * 
     * @see {@link SinkBuffer}
     */
    SHRINK_ON_UNDERRUN,

}
