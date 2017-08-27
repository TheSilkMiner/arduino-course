/*
 * KloC - Java Companion App
 * Copyright (C) 2017  TheSilkMiner
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact information:
 * E-mail: thesilkminer <at> outlook <dot> com
 */
package net.thesilkminer.arduino.kloc.util;

import com.google.common.base.Preconditions;
import net.thesilkminer.arduino.kloc.crash.ReportedException;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Schedules automatic execution of various tasks.
 *
 * <p>The various tasks are executed on another thread.
 * That thread is <strong>single</strong>, so two scheduled tasks
 * cannot run at the same time.</p>
 *
 * @author TheSilkMiner
 * @since 1.0
 */
// TODO Add #scheduleTaskAfterPrevious(Runnable, long, TimeUnit)void
// TODO Add #scheduleFxTask(Runnable, long, TimeUnit)void
@ThreadSafe
public final class Scheduler {

    private static final class SchedulerHelper {
        static {
            LOGGER.trace("Scheduler first time loading: constructing thread-safe singleton instance");
        }

        private static final Scheduler SCHEDULER = new Scheduler();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);

    private final ScheduledExecutorService scheduler;
    private final ReentrantLock lock;

    private Scheduler() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.lock = new ReentrantLock();
    }

    /**
     * Gets the unique Scheduler instance.
     *
     * @return
     *      The unique Scheduler instance.
     *
     * @since 1.0
     */
    @Contract(pure = true)
    @Nonnull
    public static synchronized Scheduler getInstance() {
        return SchedulerHelper.SCHEDULER;
    }

    /**
     * Schedules the given task to be executed after the specified delay (in milliseconds).
     *
     * @param task
     *      The task to run. It must not be {@code null}.
     * @param millis
     *      The delay after which the task should be executed, in milliseconds.
     *
     * @since 1.0
     */
    public void scheduleTask(@Nonnull final Runnable task, final long millis) {
        this.scheduleTask(task, millis, TimeUnit.MILLISECONDS);
    }

    /**
     * Schedules the given task to be executed after the specified delay.
     *
     * @param task
     *      The task to run. It must not be {@code null}.
     * @param delay
     *      The delay after which the task should be executed.
     * @param timeUnit
     *      The measurement unit of the delay. It must not be {@code null}.
     *
     * @since 1.0
     */
    public void scheduleTask(@Nonnull final Runnable task, final long delay, @Nonnull final TimeUnit timeUnit) {
        Preconditions.checkNotNull(task, "How would you run a 'null' task exactly?");
        Preconditions.checkNotNull(timeUnit, "timeUnit must not be null: use #scheduleTask(Runnable, long) instead");
        this.lock.lock();
        try {
            /*
             * We don't schedule the task directly so that, if executions throws an
             * exception, then the entire application crashes away, instead of
             * continuing in an illegal state.
             */
            this.scheduler.schedule(() -> {
                try {
                    task.run();
                } catch (final Throwable t) {
                    WorkerThread.INSTANCE.offer(() -> {
                        throw new ReportedException("An error occurred while executing scheduled task " + task.toString(), t);
                    });
                }
            }, delay, timeUnit);
            LOGGER.trace("Scheduled task {} to be executed in {} {}", task, delay, timeUnit);
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * Shuts the current scheduler down.
     *
     * <p>Pending tasks are executed, but the scheduler does
     * not wait for completion. New tasks are rejected.</p>
     *
     * @since 1.0
     */
    public void shutdownScheduler() {
        this.shutdownScheduler(false);
    }

    /**
     * Shuts the current scheduler down immediately.
     *
     * <p>No new tasks are accepted and currently running
     * ones are interrupted at best (that is, if they allow
     * interruption). Currently pending tasks are returned.</p>
     *
     * @return
     *      A list of pending tasks at the moment of shutdown.
     *
     * @since 1.0
     */
    @Nonnull
    @SuppressWarnings("UnusedReturnValue")
    public List<Runnable> shutdownSchedulerImmediately() {
        return this.shutdownScheduler(true);
    }

    @Contract("true -> !null; false -> null")
    @Nullable
    private List<Runnable> shutdownScheduler(final boolean rightNow) {
        this.lock.lock();
        try {
            LOGGER.info("Attempting to shut down scheduler");
            LOGGER.trace("Request to be performed {}", rightNow? "now" : "as soon as possible");

            if (rightNow) {
                LOGGER.warn("Shutting down threads forcefully: this may lead to errors!");
                final List<Runnable> list = this.scheduler.shutdownNow();
                LOGGER.warn("Tasks still to execute: {}", list);
                return list;
            }

            this.scheduler.shutdown();
            return null;
        } finally {
            try {
                LOGGER.error("FATAL: Scheduler shut down");
            } finally {
                this.lock.unlock();
            }
        }
    }
}
