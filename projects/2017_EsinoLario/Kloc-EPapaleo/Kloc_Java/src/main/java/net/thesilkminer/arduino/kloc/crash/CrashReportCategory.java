package net.thesilkminer.arduino.kloc.crash;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Represents a category of information used in crash reports.
 *
 * <p>Categories are used only for optional information: everything else
 * is bundled directly into {@link CrashReport}.</p>
 *
 * @author TheSilkMiner
 * @since 0.1
 */
public interface CrashReportCategory {

    /**
     * Gets the name of the category, for display on the crash report.
     *
     * @return
     *      The name of the category.
     *
     * @since 1.0
     */
    @Nonnull
    String getName();

    /**
     * Gets a map of key, value pairs that must be shown on the crash
     * report.
     *
     * <p>Every pair will be printed separately. You can have a custom
     * object as the value, which will be printed according to its
     * {@code toString()} implementation. The key will be printed as it
     * is instead.</p>
     *
     * <p>If your object throws when implementing {@code toString()}, then
     * the crash report will default to a custom implementation.</p>
     *
     * @return
     *      A map of key, value pairs to display on the report.
     *
     * @since 1.0
     */
    @Nonnull
    Map<String, Object> getKeyValuePairs();

    /**
     * Class wrapper to account for faulty {@code toString()} implementation.
     *
     * @author TheSilkMiner
     * @since 1.0
     */
    final class SafeToStringObject {

        private final Object wrapped;

        SafeToStringObject(final Object wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public String toString() {
            try {
                return wrapped.toString();
            } catch (final Throwable t) {
                return "~~ERROR: " + t.getClass().getCanonicalName() + "~~";
            }
        }
    }
}
