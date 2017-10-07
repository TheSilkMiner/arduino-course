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
package net.thesilkminer.arduino.kloc.ui;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * General purpose class used to load an FXML file.
 *
 * <p>Localization must be applied manually.</p>
 *
 * <p>The created instances are cached to account for performance:
 * every time the same FXML file is requested, a cached instance is
 * loaded. This allows to cache returned containers and preserve
 * CPU cycles, avoiding I/O all together where possible.</p>
 *
 * <p>Resolution of Material Icon Codes is optional and the user
 * can opt out of it: the choice is remember in caching. I.e., if
 * a user wants to load the same FXML twice, one with resolution and
 * the other without, it is possible to do so.</p>
 *
 * @author TheSilkMiner
 * @since 1.0
 */
@SuppressWarnings("unused")
@ThreadSafe
public final class GenericLoader {

    private static final Map<Pair<String, Boolean>, GenericLoader> CACHE = Maps.newConcurrentMap();
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericLoader.class);
    private static final Lock LOCK = new ReentrantLock();

    private final String fxmlName;
    private final URL fxmlUrl;
    private final boolean needsMaterialIcons;

    private Object node;
    private Object controller;

    private GenericLoader(@Nonnull final String fxmlName, final boolean needsMaterialIcons) throws MalformedURLException {
        LOCK.lock();
        try {
            this.fxmlName = fxmlName;
            this.needsMaterialIcons = needsMaterialIcons;
            //noinspection SpellCheckingInspection
            this.fxmlUrl = this.getClass().getResource("/assets/kloc/fxml/" + this.fxmlName);

            LOGGER.info("Constructed new loader pointing to '{}'", this.fxmlUrl.toString());
            LOGGER.trace("Given name: {}; should resolve MDI? {}", fxmlName, needsMaterialIcons);

            this.node = null;
            this.controller = null;
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Constructs a new loader instance that points to the given location.
     *
     * <p>Material design icon codes are resolved.</p>
     *
     * @param fxmlName
     *      The location of the FXML file. It must not be {@code null}.
     * @return
     *      A new instance of this loader that points to the given location.
     * @throws MalformedURLException
     *      If the given {@code fxmlName} is not a valid location.
     *
     * @see MalformedURLException
     * @see URL
     * @since 1.0
     */
    @Nonnull
    public static GenericLoader of(@Nonnull final String fxmlName) throws MalformedURLException {
        return of(fxmlName, true);
    }

    /**
     * Constructs a new loader instance that points to the given location.
     *
     * <p>If {@code materialIcons} is {@code true}, then Material design icon codes
     * are resolved, otherwise are left unescaped.</p>
     *
     * @param fxmlName
     *      The location of the FXML file. It must not be {@code null}.
     * @param materialIcons
     *      Whether Material design icon codes should be resolved.
     * @return
     *      An instance of this loader that points to the given location.
     *      The returned instance is taken from the cache if possible, otherwise
     *      a new one is constructed.
     * @throws MalformedURLException
     *      If the given {@code fxmlName} is not a valid location.
     *
     * @see MalformedURLException
     * @see URL
     * @since 1.0
     */
    @Nonnull
    public static GenericLoader of(@Nonnull final String fxmlName, final boolean materialIcons) throws MalformedURLException {
        Preconditions.checkNotNull(fxmlName);
        LOCK.lock();
        try {
            final Pair<String, Boolean> key = ImmutablePair.of(fxmlName, materialIcons);
            if (CACHE.get(key) != null) return CACHE.get(key);
            final GenericLoader value = new GenericLoader(fxmlName, materialIcons);
            CACHE.put(key, value);
            return value;
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Constructs a new loader instance that points to the given location.
     *
     * <p>Material design icon codes are resolved.</p>
     *
     * <p>Using this method, the eventual {@link MalformedURLException} is
     * re-thrown as an unchecked {@link RuntimeException}.</p>
     *
     * @param fxmlName
     *      The location of the FXML file. It must not be {@code null}.
     * @return
     *      A new instance of this loader that points to the given location.
     *
     * @see URL
     * @since 1.0
     */
    @Nonnull
    public static GenericLoader ofUnchecked(@Nonnull final String fxmlName) {
        return ofUnchecked(fxmlName, true);
    }

    /**
     * Constructs a new loader instance that points to the given location.
     *
     * <p>If {@code materialIcons} is {@code true}, then Material design icon codes
     * are resolved, otherwise are left unescaped.</p>
     *
     * <p>Using this method, the eventual {@link MalformedURLException} is
     * re-thrown as an unchecked {@link RuntimeException}.</p>
     *
     * @param fxmlName
     *      The location of the FXML file. It must not be {@code null}.
     * @param materialIcons
     *      Whether Material design icon codes should be resolved.
     * @return
     *      A new instance of this loader that points to the given location.
     *
     * @see URL
     * @since 1.0
     */
    @Nonnull
    @SuppressWarnings("WeakerAccess")
    public static GenericLoader ofUnchecked(@Nonnull final String fxmlName, final boolean materialIcons) {
        try {
            return of(fxmlName, materialIcons);
        } catch (final MalformedURLException e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Gets the given FXML name.
     *
     * @return
     *      The given FXML name.
     * @since 1.0
     */
    @Contract(pure = true)
    @Nonnull
    protected String getFxmlName() {
        LOCK.lock();
        try {
            return this.fxmlName;
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Returns whether Material design icon codes should be resolved or not.
     *
     * @return
     *      If Material design icon codes should be resolved.
     * @since 1.0
     */
    @Contract(pure = true)
    protected boolean needsMaterialIcons() {
        LOCK.lock();
        try {
            return this.needsMaterialIcons;
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Gets the main container of the FXML file.
     *
     * <p>If needed, the contents of the FXML file are loaded from the URL passed
     * in during construction. In this occasion, Material design icon codes are
     * resolved (if specified during construction) and the background of the container
     * is set to {@link Background#EMPTY} if it is an instance of {@link Pane}.</p>
     *
     * @param <T>
     *     The type of the parent container.
     * @return
     *      The current container.
     * @throws IOException
     *      If the file could not be loaded.
     *
     * @see FXMLLoader#load()
     * @since 1.0
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public final <T> T getContainer() throws IOException {
        LOCK.lock();
        try {
            if (this.node == null) this.load0();
            return Preconditions.checkNotNull((T) this.node); // An FXML without main container? THROW immediately!
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Gets the controller associated to this FXML instance.
     *
     * <p>The controller is wrapped in an {@link Optional}, to account
     * for FXML files that don't have any controller associated to them.</p>
     *
     * <p>If needed, the contents of the FXML file are loaded from the URL passed
     * in during construction. For more information for what happens during loading,
     * please refer to the {@link #getContainer()} documentation.</p>
     *
     * @param <C>
     *     The type of the controller.
     * @return
     *     An {code Optional} with the controller if available.
     *     {@link Optional#empty()} instead.
     * @throws IOException
     *      If the file could not be loaded.
     *
     * @see FXMLLoader#getController()
     * @since 1.0
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public final <C> Optional<C> getController() throws IOException {
        LOCK.lock();
        try {
            if (this.node == null && this.controller == null) this.load0();
            return Optional.ofNullable((C) this.controller);
        } finally {
            LOCK.unlock();
        }
    }

    private void load0() throws IOException {
        LOCK.lock();
        try {
            LOGGER.info("Loading FXML file '{}'", this.fxmlName);
            final FXMLLoader loader = new FXMLLoader();
            loader.setLocation(this.fxmlUrl);
            if (this.needsMaterialIcons) {
                //noinspection SpellCheckingInspection
                loader.setResources(ResourceBundle.getBundle("assets.kloc.fonts.Material Icons.Material_Icons"));
                LOGGER.trace("Allow MDI resolution");
            }
            final Object container = loader.load();
            if (container instanceof Pane) ((Pane) container).setBackground(Background.EMPTY);
            this.node = container;
            this.controller = loader.getController();
            LOGGER.debug("Obtained node: {} ({}); obtained controller: {} ({})", container, container.getClass().getCanonicalName(),
                    this.controller, this.controller == null? null : this.controller.getClass().getCanonicalName());
        } finally {
            LOCK.unlock();
        }
    }
}
