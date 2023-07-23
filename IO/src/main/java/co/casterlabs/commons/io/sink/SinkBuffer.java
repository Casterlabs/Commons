/* 
Copyright 2023 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.io.sink;

import lombok.Getter;
import lombok.NonNull;

/**
 * A SinkBuffer allows you to insert data at a reliable rate and read back the
 * data at a reliable rate. You can mix and match the strategies and set the
 * internal buffer size to any value. <br />
 * <br />
 * 
 * There are three insertion strategies:
 * <ul>
 * <li>{@link InsertionStrategy#DROP_ON_OVERFLOW}</li>
 * <li>{@link InsertionStrategy#BLOCK_ON_OVERFLOW}</li>
 * <li>{@link InsertionStrategy#THROW_ON_OVERFLOW}</li>
 * </ul>
 * 
 * There are three extraction strategies:
 * <ul>
 * <li>{@link ExtractionStrategy#NULL_ON_UNDERRUN}</li>
 * <li>{@link ExtractionStrategy#BLOCK_ON_UNDERRUN}</li>
 * <li>{@link ExtractionStrategy#THROW_ON_UNDERRUN}</li>
 * </ul>
 */
public class SinkBuffer {
    private final InsertionStrategy insertionStrategy;
    private final ExtractionStrategy extractionStrategy;
    private final byte[] buffer;

    public SinkBuffer(int bufferSize, @NonNull InsertionStrategy insertionStrategy, ExtractionStrategy extractionStrategy) {
        this.buffer = new byte[bufferSize];
        this.insertionStrategy = insertionStrategy;
        this.extractionStrategy = extractionStrategy;
    }

    public void insert(byte[] b, int off, int len) throws InterruptedException {
        // TODO
    }

    public void extract(byte[] b, int off, int len) throws InterruptedException {
        // TODO

    }

}
