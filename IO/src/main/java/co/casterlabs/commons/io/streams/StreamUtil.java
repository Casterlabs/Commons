package co.casterlabs.commons.io.streams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import lombok.NonNull;

public class StreamUtil {

    /**
     * @param    source      The data source.
     * @param    dest        The target destination.
     * @param    bufferSize  The read buffer size.
     * @param    limit       The expected length of source. This value will
     *                       constrain the amount of bytes read from the source!
     *                       (Use any negative value for unconstrained reading)
     * 
     * @throws   IOException If an IO error occurs.
     * 
     *                       This is designed to be an efficient IO transfer
     *                       allowing for fast block reads for high-bandwidth inputs
     *                       while still constraining memory usage for the transfer
     *                       buffer.
     * 
     * @implNote             The streams are NOT automatically closed for you, you
     *                       will need to close them yourself (e.g using a
     *                       try-with-resources block).
     */
    public static void streamTransfer(@NonNull InputStream source, @NonNull OutputStream dest, int bufferSize, long limit) throws IOException {
        if (limit < 0) {
            // Don't constrain.
            streamTransfer(source, dest, bufferSize);
            return;
        }

        byte[] buffer = new byte[bufferSize];
        long remaining = limit;
        int read = 0;
        while ((read = source.read(buffer)) != -1) {
            if (read >= remaining) {
                dest.write(buffer, 0, (int) remaining);
                dest.flush();
                break; // We're done!
            }

            remaining -= read;
            dest.write(buffer, 0, read);
            dest.flush();
        }
    }

    /**
     * @param    source      The data source.
     * @param    dest        The target destination.
     * @param    bufferSize  The read buffer size.
     * 
     * @throws   IOException If an IO error occurs.
     * 
     *                       Allows you to quickly write the source to the dest
     *                       using the specified buffer size.
     * 
     * @implNote             The streams are NOT automatically closed for you, you
     *                       will need to close them yourself (e.g using a
     *                       try-with-resources block).
     * 
     * @implNote             The destination is automatically flush()'d upon
     *                       success.
     */
    public static void streamTransfer(@NonNull InputStream source, @NonNull OutputStream dest, int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        int read = 0;

        while ((read = source.read(buffer)) != -1) {
            dest.write(buffer, 0, read);
        }

        dest.flush();
    }

    /**
     * @param    source The data source.
     * 
     * @return          The data in a byte array.
     * 
     *                  Reads an input stream into an array and then hands you the
     *                  result.
     * 
     * @see             #DEFAULT_BUFFER_SIZE
     * 
     * 
     * @implNote        The stream is NOT automatically closed for you, you will
     *                  need to close them yourself (e.g using a try-with-resources
     *                  block).
     */
    public static byte[] toBytes(@NonNull InputStream source) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        streamTransfer(source, out, 2048);
        return out.toByteArray();
    }

    /**
     * @param    source  The data source.
     * @param    charset The charset to use.
     * 
     * @return           The data as a String.
     * 
     *                   Reads an input stream into an String and then hands you the
     *                   result.
     * 
     * @see              #DEFAULT_BUFFER_SIZE
     * 
     * 
     * @implNote         The stream is NOT automatically closed for you, you will
     *                   need to close them yourself (e.g using a try-with-resources
     *                   block).
     */
    public static String toString(@NonNull InputStream source, @NonNull Charset sourceCharset) throws IOException {
        byte[] bytes = toBytes(source);
        return new String(bytes, sourceCharset);
    }

}
