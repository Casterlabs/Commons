package co.casterlabs.commons.io.marshalling;

public interface PrimitiveMarshall {
    public static final PrimitiveMarshall BIG_ENDIAN = new _BigEndianPM();
    public static final PrimitiveMarshall LITTLE_ENDIAN = new _LittleEndianPM();

    /* -------- */
    /* Long     */
    /* -------- */

    /**
     * @param  v The long to convert
     * 
     * @return   The long, as an array of bytes.
     */
    public byte[] longToBytes(long v);

    /**
     * @param  b The long, as an array of bytes
     * 
     * @return   The converted value.
     */
    public long bytesToLong(byte[] b);

    /**
     * @param  v The double to convert
     * 
     * @return   The double, as an array of bytes.
     */
    default byte[] doubleToBytes(double v) {
        return this.longToBytes(Double.doubleToRawLongBits(v));
    }

    /**
     * @param  b The double, as an array of bytes
     * 
     * @return   The converted value.
     */
    default double bytesToDouble(byte[] b) {
        return Double.longBitsToDouble(this.bytesToLong(b));
    }

    /* -------- */
    /* Int      */
    /* -------- */

    /**
     * @param  v The int to convert
     * 
     * @return   The int, as an array of bytes.
     */
    public byte[] intToBytes(int v);

    /**
     * @param  b The int, as an array of bytes
     * 
     * @return   The converted value.
     */
    public int bytesToInt(byte[] b);

    /**
     * @param  v The float to convert
     * 
     * @return   The float, as an array of bytes.
     */
    default byte[] floatToBytes(float v) {
        return this.longToBytes(Float.floatToRawIntBits(v));
    }

    /**
     * @param  b The float, as an array of bytes
     * 
     * @return   The converted value.
     */
    default float bytesToFloat(byte[] b) {
        return Float.intBitsToFloat(this.bytesToInt(b));
    }

    /* -------- */
    /* Short    */
    /* -------- */

    /**
     * @param  v The short to convert
     * 
     * @return   The short, as an array of bytes.
     */
    public byte[] shortToBytes(short v);

    /**
     * @param  b The short, as an array of bytes
     * 
     * @return   The converted value.
     */
    public short bytesToShort(byte[] b);

    /**
     * @param  v The char to convert
     * 
     * @return   The char, as an array of bytes.
     */
    default byte[] charToBytes(char v) {
        return this.shortToBytes((short) v);
    }

    /**
     * @param  b The char, as an array of bytes
     * 
     * @return   The converted value.
     */
    default char bytesToChar(byte[] b) {
        return (char) this.bytesToShort(b);
    }

    /* -------- */
    /* Boolean  */
    /* -------- */

    /**
     * @param  v The boolean to convert
     * 
     * @return   The boolean, as an array of bytes.
     */
    default byte[] booleanToBytes(boolean v) {
        return new byte[] {
                (byte) (v ? 1 : 0)
        };
    }

    /**
     * @param  b The boolean, as an array of bytes
     * 
     * @return   The converted value.
     */
    default boolean bytesToBoolean(byte[] b) {
        return b[0] != 0;
    }

}
