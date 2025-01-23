package co.casterlabs.commons.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import co.casterlabs.commons.io.streams.OverzealousInputStream;
import lombok.AllArgsConstructor;
import lombok.ToString;

class _HttpUtil {
    private static final int MAX_REQUEST_LINE_LENGTH = 16 /*kb*/ * 1024;
    private static final int MAX_HEADER_LENGTH = 16 /*kb*/ * 1024;
    public static final Charset CHARSET = StandardCharsets.ISO_8859_1;

    /* ---------------- */
    /* Types            */
    /* ---------------- */

    @ToString
    @AllArgsConstructor
    static class ResponseLineInfo {
        public final String httpVersion;
        public final int statusCode;
        public final String statusMessage;
    }

    /* ---------------- */
    /* Data             */
    /* ---------------- */

    static ResponseLineInfo readResponseLine(OverzealousInputStream input, int guessedMtu) throws IOException {
        WorkBuffer buffer = new WorkBuffer(MAX_REQUEST_LINE_LENGTH);

        // Request line
        int requestLineEnd = readLine(input, buffer, guessedMtu);

        String version = _HttpUtil.readStringUntil(buffer, requestLineEnd, ' ');
        buffer.marker++; // Consume the ' '

        int statusCode = Integer.parseInt(_HttpUtil.readStringUntil(buffer, requestLineEnd, ' '));
        buffer.marker++; // Consume the ' '

        int statusLength = requestLineEnd - buffer.marker;
        String statusMessage = new String(buffer.raw, buffer.marker, statusLength, CHARSET);
        buffer.marker += statusLength; // Consume

        input.append(buffer.raw, buffer.marker, buffer.limit);

        // Discard remaining CRLF
        input.read();
        input.read();

        return new ResponseLineInfo(version, statusCode, statusMessage);
    }

    static Map<String, String> readHeaders(OverzealousInputStream input, int guessedMtu) throws IOException {
        Map<String, String> headers = new HashMap<>();
        WorkBuffer buffer = new WorkBuffer(MAX_HEADER_LENGTH);

        String currentKey = null;
        String currentValue = null;

        while (true) {
            int lineEnd = readLine(input, buffer, guessedMtu);

            if (lineEnd - buffer.marker == 0) {
                // End of headers
                if (currentKey != null) {
                    headers.put(currentKey.trim().toLowerCase(), currentValue.trim());
                }
                break;
            }

            // A header line that starts with a whitespace or tab is a continuation of the
            // previous header line. Example of what we're looking for:
            /* X-My-Header: some-value-1,\r\n  */
            /*              some-value-2\r\n   */
            if (currentKey != null) {
                if (buffer.raw[buffer.marker] == ' ' || buffer.raw[buffer.marker] == '\t') {
                    currentValue += readStringUntil(buffer, lineEnd, '\r');
                }
                headers.put(currentKey.trim().toLowerCase(), currentValue.trim());
            }

            currentKey = readStringUntil(buffer, lineEnd, ':');
            buffer.marker++; // Consume the ':'

            if (currentKey.length() == 0) {
                throw new IllegalArgumentException("Header key was blank");
            }

            currentValue = readStringUntil(buffer, lineEnd, '\r');
            buffer.marker += 2; // +2 to consume \r\n.

            if (currentValue.length() == 0) {
                throw new IllegalArgumentException("Header value was blank");
            }
        }

        // Discard 2 bytes to consume the \r\n at the end of the header block
        buffer.marker += 2;

        input.append(buffer.raw, buffer.marker, buffer.limit);

        return headers;
    }

    /* ---------------- */
    /* Helpers          */
    /* ---------------- */

    private static int readLine(InputStream in, WorkBuffer buffer, int guessedMtu) throws IOException {
        while (true) {
            for (int bufferIndex = buffer.marker; bufferIndex < buffer.limit; bufferIndex++) {
                if (buffer.raw[bufferIndex] == '\r' && buffer.raw[bufferIndex + 1] == '\n') {
                    return bufferIndex; // End of line, break!
                }
            }

//            buffer.marker = buffer.limit;

            if (buffer.available() == 0) {
                throw new IllegalArgumentException("Request line or header line too long");
            }

            int amountToRead = Math.min(
                Math.max(guessedMtu, in.available()), // We might already have > mtu waiting on the wire
                buffer.available() // Limit by available space.
            );

            int read = in.read(buffer.raw, buffer.limit, amountToRead);
            if (read == -1) {
                throw new IOException("Reached end of stream before line was fully read.");
            } else {
                buffer.limit += read;
            }
        }
    }

    private static String readStringUntil(WorkBuffer buffer, int limit, char target) throws IOException {
        int start = buffer.marker;
        int end = start;
        for (; end < limit; end++) {
            int readCharacter = buffer.raw[end];

            if (readCharacter == target) {
                break; // End of string, break!
            }
        }

        buffer.marker = end; // +1 to consume the target.
        return new String(buffer.raw, start, end - start, CHARSET);
    }

    private static class WorkBuffer {
        public final byte[] raw;
        public int marker;
        public int limit = 0;

        public WorkBuffer(int bufferSize) {
            this.raw = new byte[bufferSize];
        }

        public int available() {
            return this.raw.length - this.limit;
        }

    }

}
