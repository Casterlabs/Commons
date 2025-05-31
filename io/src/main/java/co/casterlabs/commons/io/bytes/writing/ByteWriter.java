/* 
Copyright 2025 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.io.bytes.writing;

import java.io.IOException;

public abstract class ByteWriter implements AutoCloseable {
    public final Endian le = new LittleEndian();
    public final Endian be = new BigEndian();

    /**
     * @throws IOException if an I/O error occurs
     */
    public final void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    /**
     * @throws IOException if an I/O error occurs
     */
    public abstract void write(byte[] b, int off, int len) throws IOException;

    protected abstract void write(int value) throws IOException;

    /**
     * @return a writer limited to len bytes. Once the limit is reached, the writer
     *         will throw an IOException. Closing the reader will not close the
     *         destination, but will instead write 0s in place of all remaining
     *         bytes.
     */
    public final ByteWriter limited(int len) {
        if (len < 1) throw new IllegalArgumentException("len must be > 0");
        return new LimitedByteWriter(this, len);
    }

    public abstract class Endian {
        /* ---------------- */
        /*     Unsigned     */
        /* ---------------- */

        /**
         * @param  value       between 0-255
         * 
         * @throws IOException if an I/O error occurs
         */
        public void u8(int value) throws IOException {
            if (value < 0 || value > 0xFF) throw new IllegalArgumentException("value must be between 0 and 255");
            write(value);
        }

        /**
         * @param  value       between 0-65535
         * 
         * @throws IOException if an I/O error occurs
         */
        public abstract void u16(int value) throws IOException;

        /**
         * @param  value       between 0-16777215
         * 
         * @throws IOException if an I/O error occurs
         */
        public abstract void u24(int value) throws IOException;

        /**
         * @param  value       between 0-4294967295
         * 
         * @throws IOException if an I/O error occurs
         */
        public abstract void u32(long value) throws IOException;

        /**
         * @param  value       an unsigned long.
         * 
         * @throws IOException if an I/O error occurs
         */
        public abstract void u64(long value) throws IOException;

        /* ---------------- */
        /*  Floating-Point  */
        /* ---------------- */

        /**
         * @throws IOException if an I/O error occurs
         */
        public void flt(float value) throws IOException {
            int bits = Float.floatToRawIntBits(value);
            this.u32(bits);
        }

        /**
         * @throws IOException if an I/O error occurs
         */
        public void dbl(double value) throws IOException {
            long bits = Double.doubleToRawLongBits(value);
            this.u64(bits);
        }

        /* ---------------- */
        /*      Signed      */
        /* ---------------- */

        /**
         * @throws IOException if an I/O error occurs
         */
        public void s8(byte value) throws IOException {
            this.u8(value & 0xFF); // upcast, unsigned.
        }

        /**
         * @throws IOException if an I/O error occurs
         */
        public void s16(short value) throws IOException {
            this.u16(value & 0xFFFF); // upcast, unsigned.
        }

        /**
         * @throws IOException if an I/O error occurs
         */
        public void s32(int value) throws IOException {
            this.u32(value & 0xFFFFFFFFL); // upcast, unsigned.
        }

        /**
         * @throws IOException if an I/O error occurs
         */
        public void s64(long value) throws IOException {
            this.u64(value);
        }
    }

    private class BigEndian extends Endian {

        @Override
        public void u16(int value) throws IOException {
            if (value < 0 || value > 0xFFFF) throw new IllegalArgumentException("value must be between 0 and 65535");
            u8(value >> 8 & 0xFF);
            u8(value & 0xFF);
        }

        @Override
        public void u24(int value) throws IOException {
            if (value < 0 || value > 0xFFFFFF) throw new IllegalArgumentException("value must be between 0 and 16777215");
            u8(value >> 16 & 0xFF);
            u8(value >> 8 & 0xFF);
            u8(value & 0xFF);
        }

        @Override
        public void u32(long value) throws IOException {
            if (value < 0 || value > 0xFFFFFFFFL) throw new IllegalArgumentException("value must be between 0 and 4294967295");
            u8((int) (value >> 24 & 0xFF));
            u8((int) (value >> 16 & 0xFF));
            u8((int) (value >> 8 & 0xFF));
            u8((int) (value & 0xFF));
        }

        @Override
        public void u64(long value) throws IOException {
            u8((int) (value >> 56 & 0xFF));
            u8((int) (value >> 48 & 0xFF));
            u8((int) (value >> 40 & 0xFF));
            u8((int) (value >> 32 & 0xFF));
            u8((int) (value >> 24 & 0xFF));
            u8((int) (value >> 16 & 0xFF));
            u8((int) (value >> 8 & 0xFF));
            u8((int) (value & 0xFF));
        }

    }

    private class LittleEndian extends Endian {

        @Override
        public void u16(int value) throws IOException {
            if (value < 0 || value > 0xFFFF) throw new IllegalArgumentException("value must be between 0 and 65535");
            u8(value & 0xFF);
            u8(value >> 8 & 0xFF);
        }

        @Override
        public void u24(int value) throws IOException {
            if (value < 0 || value > 0xFFFFFF) throw new IllegalArgumentException("value must be between 0 and 16777215");
            u8(value & 0xFF);
            u8(value >> 8 & 0xFF);
            u8(value >> 16 & 0xFF);
        }

        @Override
        public void u32(long value) throws IOException {
            if (value < 0 || value > 0xFFFFFFFFL) throw new IllegalArgumentException("value must be between 0 and 4294967295");
            u8((int) (value & 0xFF));
            u8((int) (value >> 8 & 0xFF));
            u8((int) (value >> 16 & 0xFF));
            u8((int) (value >> 24 & 0xFF));
        }

        @Override
        public void u64(long value) throws IOException {
            u8((int) (value & 0xFF));
            u8((int) (value >> 8 & 0xFF));
            u8((int) (value >> 16 & 0xFF));
            u8((int) (value >> 24 & 0xFF));
            u8((int) (value >> 32 & 0xFF));
            u8((int) (value >> 40 & 0xFF));
            u8((int) (value >> 48 & 0xFF));
            u8((int) (value >> 56 & 0xFF));
        }

    }

}
