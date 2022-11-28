package co.casterlabs.commons.ipc.impl.subprocess;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import co.casterlabs.commons.async.AsyncTask;
import lombok.NonNull;
import lombok.SneakyThrows;

public abstract class SubprocessIpcHostHandler extends SubprocessIpcConnection {
    private Process process;

    public final boolean isAlive() {
        return this.process.isAlive();
    }

    public abstract void onClose();

    @SneakyThrows
    @Override
    protected void close0() {
        if (this.process.isAlive()) {
            this.process.destroy();
        }
    }

    /* -------------------- */
    /* Creation */
    /* -------------------- */

    public SubprocessIpcHostHandler(@NonNull Class<? extends SubprocessIpcClientHandler> handlerClass) throws IOException {
        try {
            handlerClass.getConstructor();
        } catch (Exception e) {
            throw new IllegalArgumentException("Handler class must have a public no-args constructor.");
        }

        List<String> exec = getExec(SubprocessIpcClientEntryPoint.class, handlerClass.getTypeName());

        this.process = new ProcessBuilder()
            .command(exec)
            .redirectError(Redirect.INHERIT)
            .redirectInput(Redirect.PIPE)
            .redirectOutput(Redirect.PIPE)
            .start();

        InputStream proc_stdout = this.process.getInputStream();
        OutputStream proc_stdin = this.process.getOutputStream();

        this.init(proc_stdin, proc_stdout);

        // Wait for the process to die.
        AsyncTask.createNonDaemon(() -> {
            try {
                this.process.waitFor();
            } catch (InterruptedException e) {}

            this.onClose();
        });
    }

    private static List<String> getExec(Class<?> main, String... programArgs) throws IOException {
        List<String> jvmArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
        String classpath = System.getProperty("java.class.path");
        String javaHome = System.getProperty("java.home");

        String entry = System.getProperty("sun.java.command"); // Tested, present in OpenJDK and Oracle
        String[] launchArgs = entry.split(" ");
        File entryFile = new File(launchArgs[0]);
        if (entryFile.exists()) { // If the entry is a file, not a class.
            classpath += ":" + entryFile.getCanonicalPath();
        }

        List<String> result = new ArrayList<>();

        result.add(String.format("\"%s/bin/java\"", javaHome));
        result.addAll(jvmArgs);
        result.add("-cp");
        result.add('"' + classpath + '"');
        result.add(main.getTypeName());
        result.addAll(Arrays.asList(programArgs));

        return result;
    }

}
