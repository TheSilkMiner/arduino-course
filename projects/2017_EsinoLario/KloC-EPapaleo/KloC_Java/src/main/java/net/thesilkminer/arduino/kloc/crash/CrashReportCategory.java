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
