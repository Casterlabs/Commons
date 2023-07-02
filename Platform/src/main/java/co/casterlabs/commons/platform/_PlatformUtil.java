package co.casterlabs.commons.platform;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import lombok.NonNull;

class _PlatformUtil {

    static void writeInputStreamToOutputStream(@NonNull InputStream source, @NonNull OutputStream dest) throws IOException {
        byte[] buffer = new byte[1024];
        int read = 0;

        while ((read = source.read(buffer)) != -1) {
            dest.write(buffer, 0, read);
        }

        dest.flush();

        source.close();
        dest.close();
    }

    static byte[] readInputStreamBytes(@NonNull InputStream source) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        writeInputStreamToOutputStream(source, out);

        source.close();

        return out.toByteArray();
    }

    static String readInputStreamString(@NonNull InputStream source, @NonNull Charset sourceCharset) throws IOException {
        byte[] bytes = readInputStreamBytes(source);

        return new String(bytes, sourceCharset);
    }

    static int getWordSize() {
        // Sources:
        // https://www.oracle.com/java/technologies/hotspotfaq.html#64bit_detection:~:text=When%20writing%20Java%20code%2C%20how%20do%20I%20distinguish%20between%2032%20and%2064%2Dbit%20operation%3F
        // https://stackoverflow.com/a/808314
        // https://www.ibm.com/docs/en/sdk-java-technology/8?topic=dja-determining-whether-your-application-is-running-32-bit-31-bit-z-64-bit-jvm

        String SADM = System.getProperty("sun.arch.data.model");
        if ((SADM != null) && !SADM.equals("unknown")) {
            return Integer.parseInt(SADM);
        }

        String CIVBM = System.getProperty("com.ibm.vm.bitmode");
        if (CIVBM != null) {
            return Integer.parseInt(CIVBM);
        }

        String vmName = System.getProperty("java.vm.name");
        if (vmName.contains("64-bit")) {
            return 64;
        } else if (vmName.contains("32-bit")) {
            return 32;
        }

        return -1;
    }

    static String getArchTarget() {
        // https://github.com/llvm/llvm-project/blob/main/llvm/include/llvm/TargetParser/Triple.h
        switch (Platform.archFamily) {
            case ARM:
                return Platform.wordSize == 64 ? //
                    (Platform.isBigEndian ? "arm" : "aarch64") : //
                    (Platform.isBigEndian ? "armeb" : "arm");

            case MIPS:
                return Platform.wordSize == 64 ? //
                    (Platform.isBigEndian ? "mips64" : "mips64el") : //
                    (Platform.isBigEndian ? "mips" : "mipsel");

            case PPC:
                return Platform.wordSize == 64 ? //
                    (Platform.isBigEndian ? "ppc64" : "ppc64le") : //
                    (Platform.isBigEndian ? "ppc" : "ppcle");

            case RISCV:
                return Platform.wordSize == 64 ? "riscv64" : "riscv32";

            case S390:
                return "systemz"; // TODO LLVM appears to not have a s390x variant?

            case SPARC:
                return Platform.wordSize == 64 ? //
                    ("sparcv9") : //
                    (Platform.isBigEndian ? "sparc" : "sparcel");

            case X86:
                return Platform.wordSize == 64 ? "x86_64" : "x86";
        }

        throw new RuntimeException("Unable to figure out LLVM for arch: " + Platform.archFamily);
    }

}
