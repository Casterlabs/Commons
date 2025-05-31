/* 
Copyright 2025 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.io.bytes.reading;

import java.io.IOException;

import co.casterlabs.commons.io.bytes.EndOfStreamException;

public abstract class ByteReader implements AutoCloseable {
    public final Endian le = new LittleEndian();
    public final Endian be = new BigEndian();

    /**
     * Skips len bytes from the source.
     * 
     * @throws IOException          if an I/O error occurs
     * @throws EndOfStreamException if the end of the stream is reached
     */
    public abstract void skip(int len) throws IOException;

    /**
     * Reads len bytes from the source.
     * 
     * @throws IOException          if an I/O error occurs
     * @throws EndOfStreamException if the end of the stream is reached
     */
    public abstract byte[] read(int len) throws IOException;

    /**
     * Reads len bytes from the source and puts them into b at offset off.
     * 
     * @throws IOException          if an I/O error occurs
     * @throws EndOfStreamException if the end of the stream is reached
     */
    public abstract void read(byte[] b, int off, int len) throws IOException;

    /**
     * @return an unsigned 8 bit value, between 0-255
     */
    protected abstract int read() throws IOException;

    /**
     * @return a reader that can only read up to len bytes. Once the limit is
     *         reached, the reader will throw an IOException. Closing the reader
     *         will not close the source, but will instead consume all remaining
     *         bytes.
     */
    public final ByteReader limited(int len) {
        if (len < 1) throw new IllegalArgumentException("len must be > 0");
        return new LimitedByteReader(this, len);
    }

    public abstract class Endian {

        /* ---------------- */
        /*     Unsigned     */
        /* ---------------- */

        /**
         * @return                      an unsigned 8 bit value, between 0-255
         * 
         * @throws IOException          if an I/O error occurs
         * @throws EndOfStreamException if the end of the stream is reached
         */
        public int u8() throws IOException {
            return read();
        }

        /**
         * @return                      an unsigned 16 bit value, between 0-65535
         * 
         * @throws IOException          if an I/O error occurs
         * @throws EndOfStreamException if the end of the stream is reached
         */
        public abstract int u16() throws IOException;

        /**
         * @return                      an unsigned 24 bit value, between 0-16777215
         * 
         * @throws IOException          if an I/O error occurs
         * @throws EndOfStreamException if the end of the stream is reached
         */
        public abstract int u24() throws IOException;

        /**
         * @return                      an unsigned 32 bit value, between 0-4294967295
         * 
         * @throws IOException          if an I/O error occurs
         * @throws EndOfStreamException if the end of the stream is reached
         */
        public abstract long u32() throws IOException;

        /**
         * @return                      an unsigned long. You will have to do unsigned
         *                              operations via {@link Long}!
         * 
         * @throws IOException          if an I/O error occurs
         * @throws EndOfStreamException if the end of the stream is reached
         */
        public abstract long u64() throws IOException;

        /* ---------------- */
        /*  Floating-Point  */
        /* ---------------- */

        /**
         * @throws IOException          if an I/O error occurs
         * @throws EndOfStreamException if the end of the stream is reached
         */
        public float flt() throws IOException {
            int bits = (int) this.u32();
            return Float.intBitsToFloat(bits);
        }

        /**
         * @throws IOException          if an I/O error occurs
         * @throws EndOfStreamException if the end of the stream is reached
         */
        public double dbl() throws IOException {
            long bits = this.u64();
            return Double.longBitsToDouble(bits);
        }

        /* ---------------- */
        /*      Signed      */
        /* ---------------- */

        /**
         * @return                      an signed byte
         * 
         * @throws IOException          if an I/O error occurs
         * @throws EndOfStreamException if the end of the stream is reached
         */
        public byte s8() throws IOException {
            return (byte) this.u8();
        }

        /**
         * @return                      an signed short
         * 
         * @throws IOException          if an I/O error occurs
         * @throws EndOfStreamException if the end of the stream is reached
         */
        public short s16() throws IOException {
            return (short) this.u16();
        }

        /**
         * @return                      an signed int
         * 
         * @throws IOException          if an I/O error occurs
         * @throws EndOfStreamException if the end of the stream is reached
         */
        public int s32() throws IOException {
            return (int) this.u32();
        }

        /**
         * @return                      an signed long
         * 
         * @throws IOException          if an I/O error occurs
         * @throws EndOfStreamException if the end of the stream is reached
         */
        public long s64() throws IOException {
            return this.u64();
        }

    }

    private class BigEndian extends Endian {

        @Override
        public int u16() throws IOException {
            return u8() << 8
                | u8() << 0;
        }

        @Override
        public int u24() throws IOException {
            return u8() << 16
                | u8() << 8
                | u8() << 0;
        }

        @Override
        public long u32() throws IOException {
            return (long) u8() << 24
                | (long) u8() << 16
                | (long) u8() << 8
                | (long) u8() << 0;
        }

        @Override
        public long u64() throws IOException {
            return (long) u8() << 56
                | (long) u8() << 48
                | (long) u8() << 40
                | (long) u8() << 32
                | (long) u8() << 24
                | (long) u8() << 16
                | (long) u8() << 8
                | (long) u8() << 0;
        }

    }

    private class LittleEndian extends Endian {

        @Override
        public int u16() throws IOException {
            return u8() << 0
                | u8() << 8;
        }

        @Override
        public int u24() throws IOException {
            return u8() << 0
                | u8() << 8
                | u8() << 16;
        }

        @Override
        public long u32() throws IOException {
            return (long) u8() << 0
                | (long) u8() << 8
                | (long) u8() << 16
                | (long) u8() << 24;
        }

        @Override
        public long u64() throws IOException {
            return (long) u8() << 0
                | (long) u8() << 8
                | (long) u8() << 16
                | (long) u8() << 24
                | (long) u8() << 32
                | (long) u8() << 40
                | (long) u8() << 48
                | (long) u8() << 56;
        }

    }

}
