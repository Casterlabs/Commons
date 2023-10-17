/* 
Copyright 2023 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.io.marshalling;

class _LittleEndianPM implements PrimitiveMarshall {

    /* -------- */
    /* Long     */
    /* -------- */

    @Override
    public byte[] longToBytes(long v) {
        return new byte[] {
                (byte) (v >> 0),
                (byte) (v >> 8),
                (byte) (v >> 16),
                (byte) (v >> 24),
                (byte) (v >> 32),
                (byte) (v >> 40),
                (byte) (v >> 48),
                (byte) (v >> 56),
        };
    }

    @Override
    public long bytesToLong(byte[] b) {
        return (((long) b[7]) << 56) |
            ((b[6] & 0xFFl) << 48) |
            ((b[5] & 0xFFl) << 40) |
            ((b[4] & 0xFFl) << 32) |
            ((b[3] & 0xFFl) << 24) |
            ((b[2] & 0xFFl) << 16) |
            ((b[1] & 0xFFl) << 8) |
            ((b[0] & 0xFFl) << 0);
    }

    /* -------- */
    /* Int      */
    /* -------- */

    @Override
    public byte[] intToBytes(int v) {
        return new byte[] {
                (byte) (v >> 0),
                (byte) (v >> 8),
                (byte) (v >> 16),
                (byte) (v >> 24),
        };
    }

    @Override
    public int bytesToInt(byte[] b) {
        return ((b[3]) << 24) |
            ((b[2] & 0xFF) << 16) |
            ((b[1] & 0xFF) << 8) |
            ((b[0] & 0xFF) << 0);
    }

    /* -------- */
    /* Short    */
    /* -------- */

    @Override
    public byte[] shortToBytes(short v) {
        return new byte[] {
                (byte) (v >> 0),
                (byte) (v >> 8),
        };
    }

    @Override
    public short bytesToShort(byte[] b) {
        return (short) (((b[1]) << 8) |
            ((b[0] & 0xFF) << 0));
    }

}