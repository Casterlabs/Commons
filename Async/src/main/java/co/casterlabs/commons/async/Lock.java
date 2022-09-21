package co.casterlabs.commons.async;

import java.util.function.Supplier;

public class Lock {
    private Object lock = new Object();
    private int executing = 0;

    public void execute(Runnable run) throws InterruptedException {
        this.execute(() -> {
            run.run();
            return null;
        });
    }

    public synchronized <T> T execute(Supplier<T> task) throws InterruptedException {
        boolean shouldWait = this.executing > 0;
        this.executing++;

        if (shouldWait) {
            synchronized (this.lock) {
                this.lock.wait();
            }
        }

        try {
            return task.get();
        } finally {
            this.executing--;

            synchronized (this.lock) {
                this.lock.notify();
            }
        }
    }

}
