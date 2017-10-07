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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An exception which is being reported by the system.
 *
 * <p>Usually an exception like this means an expected exception has been detected
 * at runtime and it is being reported with the maximum amount of details available.</p>
 *
 * @author TheSilkMiner
 * @since 1.0
 */
@SuppressWarnings("unused")
public class ReportedException extends RuntimeException {

    private final String description;

    /**
     * Constructs a new {@code ReportedException} with {@code null} as its
     * detail message and the specified description.
     *
     * <p>The cause is not specified and can be initialized subsequently via
     * a call to {@link #initCause(Throwable)}.</p>
     *
     * @param description
     *      The description of the error.
     *
     * @since 1.0
     */
    public ReportedException(@Nonnull final String description) {
        super();
        this.description = Preconditions.checkNotNull(description);
    }

    /**
     * Constructs a new {@code ReportedException} with the given {@code message}
     * as its detail message and the specified description.
     *
     * <p>The cause is not specified and can be initialized subsequently via
     * a call to {@link #initCause(Throwable)}.</p>
     *
     * @param description
     *      The description of the error.
     * @param message
     *      The detail message.
     *
     * @since 1.0
     */
    public ReportedException(@Nonnull final String description, @Nullable final String message) {
        super(message);
        this.description = Preconditions.checkNotNull(description);
    }

    /**
     * Constructs a new {@code ReportedException} with the given {@code message}
     * as its detail message, the specified description and the given {@code
     * cause}.
     *
     * <p>The detail message of {@code cause} is <strong>not</strong> automatically
     * incorporated in this exception's detail message.</p>
     *
     * @param description
     *      The description of the error.
     * @param message
     *      The detail message.
     * @param cause
     *      The cause. A {@code null} value means it is unavailable or unknown.
     *
     * @since 1.0
     */
    public ReportedException(@Nonnull final String description, @Nullable final String message, @Nullable final Throwable cause) {
        super(message, cause);
        this.description = Preconditions.checkNotNull(description);
    }

    /**
     * Constructs a new {@code ReportedException} with the specified cause, the given
     * description and a detail message of {@code cause == null? null : cause.toString()}
     * (which typically contains contains the class and detail message of {@code cause}).
     *
     * @param description
     *      The description of the error.
     * @param cause
     *      The cause. A {@code null} value means it is unavailable or unknown.
     *
     * @since 1.0
     */
    public ReportedException(@Nonnull final String description, @Nullable final Throwable cause) {
        super(cause);
        this.description = Preconditions.checkNotNull(description);
    }

    /**
     * Gets the description associated with this {@code ReportedException}.
     *
     * @return
     *      The description.
     *
     * @since 1.0
     */
    @Nonnull
    String getDescription() {
        return this.description;
    }
}
