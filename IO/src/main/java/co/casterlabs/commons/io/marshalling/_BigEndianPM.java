package co.casterlabs.commons.io.marshalling;

class _BigEndianPM implements PrimitiveMarshall {

    /* -------- */
    /* Long     */
    /* -------- */

    @Override
    public byte[] longToBytes(long v) {
        return new byte[] {
                (byte) (v >> 56),
                (byte) (v >> 48),
                (byte) (v >> 40),
                (byte) (v >> 32),
                (byte) (v >> 24),
                (byte) (v >> 16),
                (byte) (v >> 8),
                (byte) (v >> 0),
        };
    }

    @Override
    public long bytesToLong(byte[] b) {
        return (((long) b[0]) << 56) |
            ((b[1] & 0xFFl) << 48) |
            ((b[2] & 0xFFl) << 40) |
            ((b[3] & 0xFFl) << 32) |
            ((b[4] & 0xFFl) << 24) |
            ((b[5] & 0xFFl) << 16) |
            ((b[6] & 0xFFl) << 8) |
            ((b[7] & 0xFFl) << 0);
    }

    /* -------- */
    /* Int      */
    /* -------- */

    @Override
    public byte[] intToBytes(int v) {
        return new byte[] {
                (byte) (v >> 24),
                (byte) (v >> 16),
                (byte) (v >> 8),
                (byte) (v >> 0),
        };
    }

    @Override
    public int bytesToInt(byte[] b) {
        return ((b[0]) << 24) |
            ((b[1] & 0xFF) << 16) |
            ((b[2] & 0xFF) << 8) |
            ((b[3] & 0xFF) << 0);
    }

    /* -------- */
    /* Short    */
    /* -------- */

    @Override
    public byte[] shortToBytes(short v) {
        return new byte[] {
                (byte) (v >> 8),
                (byte) (v >> 0),
        };
    }

    @Override
    public short bytesToShort(byte[] b) {
        return (short) (((b[0]) << 8) |
            ((b[1] & 0xFF) << 0));
    }

}