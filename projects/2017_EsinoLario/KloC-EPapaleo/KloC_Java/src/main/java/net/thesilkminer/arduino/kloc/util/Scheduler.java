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
import org.jetbrains.annotations.Contract;

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
// TODO Support concurrent scheduled tasks (will it work?)
// TODO Add #scheduleTaskAfterPrevious(Runnable, long, TimeUnit)void
@ThreadSafe
public final class Scheduler {

    private static final class SchedulerHelper {
        private static final Scheduler SCHEDULER = new Scheduler();
    }

    private final ScheduledExecutorService scheduler;
    private final ReentrantLock lock;

    private Scheduler() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.lock = new ReentrantLock();
    }

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
            this.scheduler.schedule(task, delay, timeUnit);
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
    public List<Runnable> shutdownSchedulerImmediately() {
        return this.shutdownScheduler(true);
    }

    @Contract("true -> !null; false -> null")
    @Nullable
    private List<Runnable> shutdownScheduler(final boolean rightNow) {
        this.lock.lock();
        try {
            if (rightNow) return this.scheduler.shutdownNow();
            else this.scheduler.shutdown();
            return null;
        } finally {
            this.lock.unlock();
        }
    }
}
