package net.thesilkminer.arduino.kloc.ui;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * General purpose class used to load an FXML file.
 *
 * <p>Localization must be applied manually.</p>
 *
 * @param <T>
 *     The type of the parent container.
 *
 * @author TheSilkMiner
 * @since 1.0
 */
public final class GenericLoader<T extends Parent> {

    private final String fxmlName;
    private final URL fxmlUrl;
    private final boolean needsMaterialIcons;

    private Object controller;

    private GenericLoader(@Nonnull final String fxmlName, final boolean needsMaterialIcons) throws MalformedURLException {
        this.fxmlName = fxmlName;
        this.needsMaterialIcons = needsMaterialIcons;
        //noinspection SpellCheckingInspection
        this.fxmlUrl = this.getClass().getResource("/assets/kloc/fxml/" + this.fxmlName);
        this.controller = null;
    }

    /**
     * Constructs a new loader instance that points to the given location.
     *
     * <p>Material design icon codes are resolved.</p>
     *
     * @param fxmlName
     *      The location of the FXML file. It must not be {@code null}.
     * @param <T>
     *      The type of the parent container.
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
    public static <T extends Parent> GenericLoader<T> of(@Nonnull final String fxmlName) throws MalformedURLException {
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
     * @param <T>
     *      The type of the parent container.
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
    public static <T extends Parent> GenericLoader<T> of(@Nonnull final String fxmlName, final boolean materialIcons)
            throws MalformedURLException {
        Preconditions.checkNotNull(fxmlName);
        return new GenericLoader<>(fxmlName, materialIcons);
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
     * @param <T>
     *      The type of the parent container.
     * @return
     *      A new instance of this loader that points to the given location.
     *
     * @see URL
     * @since 1.0
     */
    @Nonnull
    public static <T extends Parent> GenericLoader<T> ofUnchecked(@Nonnull final String fxmlName) {
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
     * @param <T>
     *      The type of the parent container.
     * @return
     *      A new instance of this loader that points to the given location.
     *
     * @see URL
     * @since 1.0
     */
    @Nonnull
    public static <T extends Parent> GenericLoader<T> ofUnchecked(@Nonnull final String fxmlName,
                                                                  final boolean materialIcons) {
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
        return this.fxmlName;
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
        return this.needsMaterialIcons;
    }

    /**
     * Loads the specified FXML file and returns the container.
     *
     * <p>If specified during construction, Material design icon codes are
     * resolved.</p>
     *
     * <p>Also, the background of the container is set to {@link Background#EMPTY
     * empty} if it is an instance of {@link Pane} or one of its subclasses, such as
     * {@code AnchorPane}.</p>
     *
     * @return
     *      The current container.
     * @throws IOException
     *      If the file could not be loaded.
     *
     * @see FXMLLoader#load()
     * @since 1.0
     */
    @Nonnull
    public final T load() throws IOException {
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.fxmlUrl);
        if (this.needsMaterialIcons) {
            //noinspection SpellCheckingInspection
            loader.setResources(ResourceBundle.getBundle("assets.kloc.fonts.Material Icons.Material_Icons"));
        }
        final T container = loader.load();
        if (container instanceof Pane) ((Pane) container).setBackground(Background.EMPTY);
        this.controller = loader.getController();
        return container;
    }

    /**
     * Gets the controller associated to this FXML instance.
     *
     * <p>The controller is wrapped in an {@link Optional}, to account
     * for FXML files that don't have any controller associated to them.</p>
     *
     * @param <C>
     *     The type of the controller.
     * @return
     *     An {code Optional} with the controller if available.
     *     {@link Optional#empty()} instead.
     *
     * @see FXMLLoader#getController()
     * @since 1.0
     */
    @Contract(pure = true)
    @Nonnull
    @SuppressWarnings("unchecked")
    public final <C> Optional<C> getAssociatedController() {
        return Optional.ofNullable((C) this.controller);
    }
}
