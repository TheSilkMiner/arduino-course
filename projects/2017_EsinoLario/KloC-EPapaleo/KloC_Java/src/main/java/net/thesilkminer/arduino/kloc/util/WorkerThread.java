package net.thesilkminer.arduino.kloc.util;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Identifies a thread whose only purpose is to execute immediately
 * the given tasks.
 *
 * @author TheSilkMiner
 * @since 1.0
 */
public final class WorkerThread implements Runnable {

    /**
     * The unique instance of this runnable.
     *
     * @since 1.0
     */
    public static final WorkerThread INSTANCE = new WorkerThread();

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerThread.class);

    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

    private WorkerThread() {}

    @Override
    public void run() {
        LOGGER.trace("WorkerThread started");
        while (!Thread.interrupted()) {
            try {
                LOGGER.trace("Waiting for a job to run...");
                final Runnable runnable = this.queue.take();
                LOGGER.trace("Gotten task: executing {}", runnable);
                runnable.run();
                LOGGER.trace("Correctly executed");
            } catch (final InterruptedException e) {
                LOGGER.warn("WorkerThread interrupted while waiting");
                LOGGER.warn("Unless the program is shutting down/crashing, this is a serious programming error");
                LOGGER.debug("Current queue status: {} (size: {})", this.queue.toString(), this.queue.size());
            }
        }
        LOGGER.trace("Terminating WorkerThread");
    }

    /**
     * Adds a new runnable to the queue for the worker thread.
     *
     * @param r
     *      The runnable to add. It cannot be {@code null}.
     */
    public void offer(@Nonnull final Runnable r) {
        this.queue.add(Preconditions.checkNotNull(r));
    }
}
