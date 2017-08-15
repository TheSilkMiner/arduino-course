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
package net.thesilkminer.arduino.kloc.crash;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.thesilkminer.arduino.kloc.KloC;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.management.ManagementFactory;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * Represents a crash report.
 *
 * <p>A crash report is used to report all details in case of an error
 * that causes the entire program to fail.</p>
 *
 * @author TheSilkMiner
 * @since 1.0
 */
public final class CrashReport {

    private static final String DEFAULT_WITTY_COMMENT = "Witty comment unavailable :(";
    private static final List<String> WITTY_COMMENTS = ImmutableList.of(
            "That was supposed to happen, I swear!",
            "Uhm... everything was fine up to now...",
            "PLAN B!",
            "Time to use the screwdriver",
            "If I were a snake, things would be less brace-y",
            "Don't worry, I still love you <3",
            "What happened?",
            "Let the Hunger Games begin",
            "Did I do that?"
    );

    private final LocalDateTime generationTime = LocalDateTime.now();
    private final String comment = this.getWittyComment();
    private final String description;
    private final Thread thread;
    private final Throwable throwable;

    private final Collection<CrashReportCategory> categories;

    private CrashReport(@Nonnull final Throwable throwable, @Nullable final Thread thread, @Nullable final String description) {
        this.throwable = throwable;
        this.description = description == null? throwable.getMessage() : description;
        this.thread = thread == null? Thread.currentThread() : thread;
        this.categories = Lists.newLinkedList();
    }

    @Nonnull
    public static CrashReport from(@Nonnull final Throwable throwable, @Nullable final Thread thread) {
        return new CrashReport(Preconditions.checkNotNull(throwable), thread,
                throwable instanceof ReportedException? ((ReportedException) throwable).getDescription() : null);
    }

    @Contract(pure = true)
    @Nonnull
    private String getWittyComment() {
        try {
            return WITTY_COMMENTS.get(ThreadLocalRandom.current().nextInt(WITTY_COMMENTS.size()));
        } catch (final Throwable et) {
            return DEFAULT_WITTY_COMMENT;
        }
    }

    @Contract(pure = true)
    @Nonnull
    private String getFlags() {
        final StringBuilder b = new StringBuilder(40);
        ManagementFactory.getRuntimeMXBean().getInputArguments().forEach(it -> b.append(it).append(" "));
        return b.toString();
    }

    /**
     * Adds a custom category to the ones to print on the crash report.
     *
     * <p>The supplied {@code category}, must not be {@code null}.</p>
     *
     * @param category
     *      The category to add.
     * @return
     *      If the addition was successful.
     */
    @SuppressWarnings("unused")
    public final boolean registerCustomCategory(@Nonnull final CrashReportCategory category) {
        return this.categories.add(Preconditions.checkNotNull(category));
    }

    @Nonnull
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(5_000); // 5,000 characters should be enough... I hope
        builder.append("KloC has crashed due to an irrecoverable error.\n");
        builder.append("All the known details of the error are in the text that follows.\n");
        builder.append("Please send this report along with a detailed description and the steps to reproduce the error ");
        builder.append("to the developer.\nOr, if you know how to fix it, submit a PR! ;)\n\n");
        builder.append(" ---- KloC Crash Report ---- \n");
        builder.append("// ").append(this.comment).append("\n\n");
        builder.append("Description: ").append(this.description).append('\n');
        builder.append("Time: ").append(this.generationTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm:ss a")));
        builder.append("\nError message: ").append(this.throwable.getMessage());
        builder.append("\n\nStacktrace: \n");
        this.printStackTrace(this.throwable, builder);
        builder.append("\n\n --- The complete stacktrace, all known details and all the available information are in ");
        builder.append("the text that follows ---\n");
        builder.append("NOTE TO THE DEVELOPER: Objects may be in an illegal state when information has been gathered. ");
        //noinspection SpellCheckingInspection
        builder.append("The text below refers to the exact state in which the object was when KloC went kaputt.\n\n");
        builder.append(" -- Stacktrace --\n");
        this.printStackTrace(this.throwable instanceof ReportedException? this.throwable.getCause() : this.throwable, builder);
        builder.append("\n -- Thread statuses --\n");
        Thread.getAllStackTraces().forEach((k, v) -> {
            builder.append("    - ").append(k.getName()).append('\n');
            Arrays.stream(v).forEach(it -> builder.append("        ").append(it).append('\n'));
        });
        builder.append("\nException thread: ").append(this.thread);
        builder.append("\nCurrent thread: ").append(Thread.currentThread().toString()).append('\n');
        this.categories.forEach(it -> {
            builder.append("\n -- ").append(it.getName()).append(" --\n");
            it.getKeyValuePairs().forEach((k, v) -> {
                builder.append("    ").append(k).append(": ").append(new CrashReportCategory.SafeToStringObject(v));
                builder.append('\n');
            });
        });
        builder.append("\n -- Software details --\n");
        builder.append("Name: ").append(KloC.NAME).append("\nVersion: ").append(KloC.VERSION).append('\n');
        builder.append("Type: ").append(KloC.TYPE).append('\n');
        builder.append("\n -- System details --\n");
        builder.append("Operating system: ").append(System.getProperty("os.name")).append(' ');
        builder.append(System.getProperty("os.version")).append("\nOS architecture: ");
        builder.append(System.getProperty("os.arch")).append("\n");
        builder.append("Java version: ").append(System.getProperty("java.version")).append(" (by ");
        builder.append(System.getProperty("java.vendor")).append(")\nJava VM version: ");
        builder.append(System.getProperty("java.vm.version")).append(" (by ").append(System.getProperty("java.vm.vendor"));
        builder.append(")\nJVM Flags: ").append(this.getFlags()).append("\nJava App Classpath: ");
        builder.append(System.getProperty("java.class.path")).append("\nLaunch command: ");
        final String sun$java$command = System.getProperty("sun.java.command");
        builder.append(sun$java$command == null || sun$java$command.isEmpty()? "Not running on Oracle's VM" : sun$java$command);
        final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.ENGLISH);
        builder.append("\nMemory: ").append(numberFormat.format(Runtime.getRuntime().totalMemory() / 1_048_576L));
        builder.append("MB/").append(numberFormat.format(Runtime.getRuntime().maxMemory() / 1_048_576L)).append("MB");
        builder.append(" (").append(numberFormat.format(Runtime.getRuntime().freeMemory() / 1_048_576L));
        builder.append("MB free)\n");
        builder.append("\n\n-------- END OF CRASH REPORT --------\n");
        return builder.toString();
    }

    private void printStackTrace(final Throwable t, final StringBuilder b) {
        final Set<Throwable> s = Collections.newSetFromMap(new IdentityHashMap<>());
        s.add(t);
        b.append(t).append('\n');

        Arrays.stream(t.getStackTrace()).forEach(it -> b.append("    at ").append(it).append('\n'));
        Arrays.stream(t.getSuppressed()).forEach(it ->
                this.printEnclosedStackTrace(it, b, t.getStackTrace(), "Suppressed: ", "    ", s));

        if (Objects.nonNull(t.getCause())) {
            this.printEnclosedStackTrace(t.getCause(), b, t.getStackTrace(), "Caused by: ", "", s);
        }
    }

    private void printEnclosedStackTrace(final Throwable t, final StringBuilder b, final StackTraceElement[] e,
                                         final String c, final String p, final Set<Throwable> s) {
        if (s.contains(t)) {
            b.append("    ").append("[CIRCULAR REFERENCE: ").append(t).append("]\n");
            return;
        }

        s.add(t);

        final StackTraceElement[] r = t.getStackTrace();
        int m = r.length - 1;
        int n = e.length - 1;

        while (m >= 0 && n >= 0 && r[m].equals(e[n])) {
            --m;
            --n;
        }

        final int f = r.length - 1 - m;
        b.append(p).append(c).append(t).append("\n");

        IntStream.rangeClosed(0, m).forEach(i -> b.append(p).append("    at ").append(r[i]).append('\n'));

        if (f != 0) b.append(p).append("    ... ").append(f).append(" more\n");

        Arrays.stream(t.getSuppressed()).forEach(it ->
                this.printEnclosedStackTrace(t, b, r, "Suppressed: ", p + "    ", s));

        if (Objects.nonNull(t.getCause())) this.printEnclosedStackTrace(t.getCause(), b, r, "Caused by: ", p, s);
    }
}
