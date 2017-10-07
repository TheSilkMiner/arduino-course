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

package org.apache.log4j.custom;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.ErrorCode;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Appends log events to multiple files.
 *
 * @author TheSilkMiner
 * @since 1.0
 */
public class MultipleFilesAppender extends FileAppender {

    private static final int MAX_COPIES;

    static {
        int tmp;
        try {
            //noinspection SpellCheckingInspection
            tmp = Integer.parseInt(System.getProperty("kloc.logging.maxFileCopies"));
        } catch (final NumberFormatException e) {
            tmp = 4;
        }
        MAX_COPIES = tmp;
    }

    /**
     * The default constructor doesn't do anything.
     *
     * @since 1.0
     */
    @SuppressWarnings("unused")
    public MultipleFilesAppender() {
        super();
    }

    /**
     * Instantiate a {@code MultipleFilesAppender} and open the file
     * designated by {@code filename}.
     *
     * <p>The opened filename will become the output destination for this
     * appender.</p>
     *
     * <p>If the file already exists, a copy of the original is made before
     * continuing. The maximum amount of copies kept is stored in the
     * {@link #MAX_COPIES} constant.</p>
     *
     * <p>The file will be appended to.</p>
     *
     * @param layout
     *          The {@link Layout}.
     * @param filename
     *          The name of the file.
     * @throws IOException
     *          If an error occurs while opening the file.
     *
     * @since 1.0
     */
    @SuppressWarnings("unused")
    public MultipleFilesAppender(final Layout layout, final String filename) throws IOException {
        super(layout, filename);
    }

    /**
     *Instantiate a {@code MultipleFilesAppender} and open the file
     * designated by {@code filename}.
     *
     * <p>The opened filename will become the output destination for this
     * appender.</p>
     *
     * <p>If the file already exists, a copy of the original is made before
     * continuing. The maximum amount of copies kept is stored in the
     * {@link #MAX_COPIES} constant.</p>
     *
     * <p>If {@code append} is {@code true}, the file will be appended to,
     * otherwise the file will be truncated before being opened.</p>
     *
     * @param layout
     *          The {@link Layout}.
     * @param filename
     *          The name of the file.
     * @param append
     *          Whether the file should be appended ({@code true}) or truncated.
     * @throws IOException
     *          If an error occurs while opening the file.
     *
     * @since 1.0
     */
    @SuppressWarnings("unused")
    public MultipleFilesAppender(final Layout layout, final String filename, final boolean append) throws IOException {
        super(layout, filename, append);
    }

    /**
     *Instantiate a {@code MultipleFilesAppender} and open the file
     * designated by {@code filename}.
     *
     * <p>The opened filename will become the output destination for this
     * appender.</p>
     *
     * <p>If the file already exists, a copy of the original is made before
     * continuing. The maximum amount of copies kept is stored in the
     * {@link #MAX_COPIES} constant.</p>
     *
     * <p>If {@code append} is {@code true}, the file will be appended to,
     * otherwise the file will be truncated before being opened.</p>
     *
     * <p>If {@code bufferedIO} is {@code true}, then buffered IO will be
     * used to write the output file.</p>
     *
     * @param layout
     *          The {@link Layout}.
     * @param filename
     *          The name of the file.
     * @param append
     *          Whether the file should be appended ({@code true}) or truncated.
     * @param bufferedIO
     *          Whether buffered IO should be used to write the output file.
     * @param bufferSize
     *          The size of the buffer to use.
     * @throws IOException
     *          If an error occurs while opening the file.
     *
     * @since 1.0
     */
    @SuppressWarnings("unused")
    public MultipleFilesAppender(final Layout layout, final String filename, final boolean append,
                                 final boolean bufferedIO, final int bufferSize) throws IOException {
        super(layout, filename, append, bufferedIO, bufferSize);
    }

    @Override
    public void activateOptions() {
        if (this.fileName == null) return;
        try {
            this.fileName = this.copyFilesAndGetNewName(this.fileName);
            this.setFile(this.fileName, this.fileAppend, this.bufferedIO, this.bufferSize);
        } catch (final IOException e) {
            this.errorHandler.error("Error while activating log options", e, ErrorCode.FILE_OPEN_FAILURE);
        }
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    private String copyFilesAndGetNewName(@Nullable final String fileName) throws IOException {
        if (fileName == null) return null;

        final String extension = this.getExtensionIfPresent(fileName);
        final String name = this.stripExtensionFromName(fileName, extension);

        final String latestFileName = name + "-latest" + (extension.isEmpty()? "" : "." + extension);
        final File latestFile = new File(latestFileName);

        if (latestFile.exists()) this.move(name, extension, latestFile, 0);

        return latestFileName;
    }

    @Nonnull
    private String getExtensionIfPresent(@Nonnull final String fileName) {
        final String notHidden = fileName.indexOf('.') == 0? fileName.substring(1) : fileName;
        if (notHidden.indexOf('.') == -1) return "";
        return fileName.substring(fileName.indexOf('.') + 1);
    }

    @Nonnull
    private String stripExtensionFromName(@Nonnull final String name, @Nonnull final String extension) {
        if (extension.isEmpty()) return name;
        return StringUtils.removeEnd(StringUtils.removeEnd(name, extension), ".");
    }

    private void move(@Nonnull final String name, @Nonnull final String ext,
                      @Nonnull final File file, final int idx) throws IOException {
        final String newFileName = name + "-" + idx + (ext.isEmpty()? "" : "." + ext);
        final File newFile = new File(newFileName);
        if (newFile.exists()) {
            final int newIdx = idx + 1;

            if (newIdx == MAX_COPIES) this.deleteThisAndLeftovers(name, ext, newFile, idx);
            else this.move(name, ext, newFile, newIdx);
        }
        System.out.println("mfa4l: Moving file [" + file + "] to [" + newFile + "]");
        final File newFileAfterMove = Files.move(file.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING).toFile();
        if (newFileAfterMove.getAbsolutePath().equals(newFile.getAbsolutePath())) return;
        throw new RuntimeException("Unable to move file from " + file + " to " + newFile);
    }

    private void deleteThisAndLeftovers(@Nonnull final String name, @Nonnull final String ext,
                                        @Nonnull final File file, final int idx) throws IOException {
        if (!file.exists()) return;
        final File f = new File(name + "-" + (idx + 1) + (ext.isEmpty()? "" : "." + ext));
        this.deleteThisAndLeftovers(name, ext, f, idx + 1);
        System.out.println("mfa4l: Deleting file [" + file + "]");
        Files.delete(file.toPath());
    }
}
