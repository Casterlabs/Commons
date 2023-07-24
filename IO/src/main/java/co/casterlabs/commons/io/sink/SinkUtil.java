package co.casterlabs.commons.io.sink;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;
import java.util.function.Supplier;

import lombok.NonNull;

public class SinkUtil {
    private static int num = 1;

    public static void drainSupplierToSink(@NonNull Supplier<byte[]> producer, @NonNull SinkBuffer sb) {
        Thread thread = new Thread(() -> {
            try {
                byte[] buf;
                while ((buf = producer.get()) != null) {
                    sb.insert(buf, 0, buf.length);
                }
            } catch (InterruptedException ignored) {}
        });
        thread.setDaemon(false);
        thread.setName("SinkUtil | Supplier -> Sink | #" + num++);
        thread.start();
    }

    public static void drainInputStreamToSink(@NonNull InputStream in, @NonNull SinkBuffer sb) {
        Thread thread = new Thread(() -> {
            byte[] buf = new byte[2048];
            int read;
            try {
                while ((read = in.read(buf)) != -1) {
                    sb.insert(buf, 0, read);
                }
            } catch (IOException | InterruptedException ignored) {
                //
            } finally {
                try {
                    in.close();
                } catch (IOException ignored) {}
            }
        });
        thread.setDaemon(false);
        thread.setName("SinkUtil | Stream -> Sink | #" + num++);
        thread.start();
    }

    public static void drainSinkToOutputStream(@NonNull OutputStream out, @NonNull SinkBuffer sb) {
        Thread thread = new Thread(() -> {
            byte[] buf = new byte[2048];
            try {
                int read;
                while (true) {
                    read = sb.extract(buf, 0, buf.length);
                    out.write(buf, 0, read);
                }
            } catch (IOException | InterruptedException ignored) {
                //
            } finally {
                try {
                    out.close();
                } catch (IOException ignored) {}
            }
        });
        thread.setDaemon(false);
        thread.setName("SinkUtil | Sink -> Stream | #" + num++);
        thread.start();
    }

    public static void drainSinkToConsumer(@NonNull Consumer<byte[]> consumer, @NonNull SinkBuffer sb) {
        Thread thread = new Thread(() -> {
            byte[] buf = new byte[2048];
            try {
                int read;
                while (true) {
                    read = sb.extract(buf, 0, buf.length);

                    byte[] data = new byte[read];
                    System.arraycopy(buf, 0, data, 0, read);
                    consumer.accept(data);
                }
            } catch (InterruptedException ignored) {}
        });
        thread.setDaemon(false);
        thread.setName("SinkUtil | Sink -> Consumer | #" + num++);
        thread.start();
    }

}
