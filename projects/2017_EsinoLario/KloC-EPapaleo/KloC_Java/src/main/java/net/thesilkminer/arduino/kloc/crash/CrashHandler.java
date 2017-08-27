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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;

/**
 * Handles the various errors thrown by the program,
 * creating a crash report and displaying it to the
 * user.
 *
 * @author TheSilkMiner
 * @since 1.0
 */
public final class CrashHandler {

    private static final class FileSavingReportCategory implements CrashReportCategory {

        private static final class StackTracePrinter {
            private final Throwable t;

            private StackTracePrinter(@Nonnull final Throwable t) {
                this.t = t;
                final String newMessage = this.t.getMessage() == null?
                        "Stacktrace dump. THIS IS NOT AN ERROR!!" : this.t.getMessage();
                try {
                    final Field detailMessage = Throwable.class.getDeclaredField("detailMessage");
                    detailMessage.setAccessible(true);
                    detailMessage.set(this.t, newMessage);
                } catch (final ReflectiveOperationException e) {
                    // Whatever, it's not that important
                }
            }

            @Override
            public String toString() {
                final StringWriter wrapped = new StringWriter();
                final PrintWriter wrapping = new PrintWriter(wrapped);
                this.t.printStackTrace(wrapping);
                wrapping.flush();
                return String.format("%n%s", wrapped.toString());
            }
        }

        private final CrashHandler handler;

        private FileSavingReportCategory(@Nonnull final CrashHandler handler) {
            this.handler = handler;
        }

        @Nonnull
        @Override
        public String getName() {
            return "Crash Report Saving";
        }

        @Nonnull
        @Override
        public Map<String, Object> getKeyValuePairs() {
            final Map<String, Object> map = Maps.newLinkedHashMap();
            map.put("File saving result", this.handler.fileSavedSuccessfully);
            map.put("Stacktrace", new StackTracePrinter(this.handler.savingStacktrace));
            if (!this.handler.fileSavedSuccessfully)
                map.put("File saving error", new StackTracePrinter(this.handler.fileSaveError));
            return map;
        }
    }

    private static final Collection<CrashReportCategory> CUSTOM_CATEGORIES = Lists.newArrayList();
    private static final Logger LOGGER = LoggerFactory.getLogger(CrashHandler.class);

    private boolean fileSavedSuccessfully;
    private Throwable fileSaveError;
    private Throwable savingStacktrace;

    private CrashHandler() {}

    @SuppressWarnings("unused")
    public static boolean registerCustomCrashReportCategory(@Nonnull final CrashReportCategory category) {
        Preconditions.checkNotNull(category);
        return CUSTOM_CATEGORIES.add(category);
    }

    public static void handle(@Nullable final Thread t, @Nonnull final Throwable e) {
        LOGGER.warn("Identified exception, constructing crash report");
        new CrashHandler().handleImpl(t, e);
    }

    private void handleImpl(@Nullable final Thread t, @Nonnull final Throwable e) {
        this.handle0(t == null? Thread.currentThread() : t, e);
    }

    /*
     * I don't know why I even wrote this entire messy code, like, really
     * what the fish was I thinking??
     * Oh well, if it works... don't touch it
     */

    private void handle0(@Nonnull final Thread t, @Nonnull final Throwable e) {
        LOGGER.trace("Constructing report");
        final CrashReport report = CrashReport.from(e, t);
        CUSTOM_CATEGORIES.forEach(report::registerCustomCategory);
        report.registerCustomCategory(new FileSavingReportCategory(this));

        LOGGER.info("Attempting to create file");
        final File file = this.createCrashReportFile();
        LOGGER.info("Writing crash report to file ({})", file);
        if (file != null) this.writeRealReport(report, file);
        LOGGER.trace("Save successful");

        // TODO Finish up implementation
        LOGGER.info("Displaying crash to the user");
        net.thesilkminer.arduino.kloc.crash.CrashFrame.obtain(report).setVisible(true);

        LOGGER.info("Terminating application");
        javafx.application.Platform.exit();
        net.thesilkminer.arduino.kloc.util.Scheduler.getInstance().shutdownSchedulerImmediately();
    }

    @Nullable
    private File createCrashReportFile() {
        try {
            LOGGER.trace("createCrashReportFile()");
            final File crashReportsDirectory = new File(System.getProperty("user.dir"), "crash-reports");
            if (!crashReportsDirectory.exists() && !crashReportsDirectory.mkdirs()) {
                this.fileSavedSuccessfully = false;
                this.fileSaveError = new RuntimeException("Unable to create directory");
                return null;
            }
            return this.createFileAndWriteDummy(crashReportsDirectory);
        } finally {
            if (this.savingStacktrace == null) this.savingStacktrace = new Throwable().fillInStackTrace();
        }
    }

    @Nullable
    private File createFileAndWriteDummy(final File dir) {
        try {
            LOGGER.trace("createFileAndWriteDummy");
            final String name = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-hh-mm-ss-a"));
            final File file = new File(dir, String.format("crash_%s.txt", name));
            if (!file.createNewFile()) throw new IOException("Unable to create given file");
            this.writeDummy(file);
            return file;
        } catch (final IOException e) {
            this.fileSavedSuccessfully = false;
            this.fileSaveError = e;
            return null;
        } finally {
            if (this.savingStacktrace == null) this.savingStacktrace = new Throwable().fillInStackTrace();
        }
    }

    private void writeRealReport(final CrashReport report, final File file) {
        try {
            LOGGER.trace("writeRealReport()");
            this.write(file, report.toString());
        } catch (final IOException ignored) {
            // This can't happen now
        }
    }

    private void writeDummy(final File file) {
        try {
            LOGGER.trace("writeDummy()");
            this.write(file, "DUMMY CONTENT");
            this.fileSavedSuccessfully = true;
        } catch (final IOException e) {
            this.fileSaveError = e;
            this.fileSavedSuccessfully = false;
        } finally {
            if (this.savingStacktrace == null) this.savingStacktrace = new Throwable().fillInStackTrace();
        }
    }

    private void write(final File file, final String content) throws IOException {
        try {
            LOGGER.trace("write()");
            final Charset UTF_8 = StandardCharsets.UTF_8;
            try (final PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), UTF_8))) {
                this.write0(w, content);
            }
        } finally {
            if (this.savingStacktrace == null) this.savingStacktrace = new Throwable().fillInStackTrace();
        }
    }

    private void write0(final PrintWriter writer, final String content) throws IOException {
        try {
            LOGGER.trace("write0()");
            writer.write(content);
        } finally {
            if (this.savingStacktrace == null) this.savingStacktrace = new Throwable().fillInStackTrace();
        }
    }
}
