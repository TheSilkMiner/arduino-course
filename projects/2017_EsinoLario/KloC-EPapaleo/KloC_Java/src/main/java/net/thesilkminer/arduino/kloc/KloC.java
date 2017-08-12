package net.thesilkminer.arduino.kloc;

import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;

/**
 * Entry point for the application.
 *
 * @author TheSilkMiner
 * @since 1.0
 */
public class KloC extends Application {

    @SuppressWarnings("unused")
    private enum SoftwareType {
        /**
         * Software is currently in development and can't be accessed externally.
         */
        DEV,
        /**
         * Software is currently in beta testing, i.e. only a few selected people have access
         * to it.
         */
        BETA,
        /**
         * All the various developers who requested it have now access to the software in
         * order to start upgrading their products.
         */
        DEVELOPER_PREVIEW,
        /**
         * All the people who requested it have access to the software and are using it on
         * a daily basis to test out the new features and provide feedback.
         */
        RC,
        /**
         * The software has now been released fully and everybody has access to it, thus no more
         * changes can be performed on it and it's time to start working on a new version.
         */
        FINAL_RELEASE;

        // This represents the version that is currently developing: it may or may not be
        // the same as the one in KloC#VERSION.
        private static final String CURRENT_VERSION = "1.0";

        @Override
        public String toString() {
            final String super$ = super.toString();
            final StringBuilder b = new StringBuilder(super$.length());
            Arrays.stream(super$.split("_")).forEach(it -> {
                b.append(it.substring(0, 1).toUpperCase(Locale.ENGLISH));
                b.append(it.substring(1).toLowerCase(Locale.ENGLISH));
                b.append(' ');
            });
            return b.toString().trim();
        }
    }

    static {
        System.setProperty("slf4j.detectLoggerNameMismatch", "true");
        System.setProperty("log4j.debug", "true");
    }

    public static final String NAME = "KloC Java Companion App";
    public static final String VERSION = "1.0";
    public static final SoftwareType TYPE = SoftwareType.DEV;
    private static final Logger LOGGER = LoggerFactory.getLogger(KloC.class);

    @Override
    public void start(final Stage primaryStage) throws Exception {
        LOGGER.info("Pre-init: loading splash screen");
        net.thesilkminer.arduino.kloc.ui.splash.SplashLoader.IT.load();
        LOGGER.info("Pre-init loading completed. Moving to init phase");
        //javafx.application.Platform.exit();
    }

    public static void main(final String... args) {
        Thread.currentThread().setName("Launcher");
        LOGGER.info("KloC launched!");

        LOGGER.debug("Running on {} version {} ({}) with Java {} by {} on JVM version {} by {} in {}",
                System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"),
                System.getProperty("java.version"), System.getProperty("java.vendor"),
                System.getProperty("java.vm.version"), System.getProperty("java.vm.vendor"),
                System.getProperty("user.dir"));
        LOGGER.debug("PRESENT DAY, PRESENT TIME! HA HA HA HA! ({})",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm:ss a")));

        LOGGER.info("Pre-init: Setting crash handler");
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            final Thread h = new Thread(() -> {
                LOGGER.error(net.thesilkminer.arduino.kloc.crash.CrashReport.from(e, t).toString());
                javafx.application.Platform.exit();
                System.exit(1);
            });
            h.setName("Crash Handler Thread");
            h.start();
        }); // TODO REAL handler
        LOGGER.info("Pre-init: loading fonts");
        loadFonts();
        LOGGER.debug("Fonts loaded. Testing availability");
        LOGGER.debug("Roboto Regular: {}; Bold: {}; Thin: {}", Font.font("Roboto"),
                Font.font("Roboto", FontWeight.BOLD, 12), Font.font("Roboto", FontWeight.THIN, -1));
        LOGGER.debug("Material Icons: {}", Font.font("Material Icons"));
        LOGGER.debug("Roboto Slab Regular: {}; Thin: {}",
                Font.font("Roboto Slab"), Font.font("Roboto Slab", FontWeight.THIN, -1));

        final Thread startup = new Thread(() -> {
            LOGGER.info("Pre-init: Launching JavaFX app with arguments {}", (Object) args);
            launch(args);
        });
        startup.setName("JavaFX Application Startup Thread");
        startup.start();
    }

    private static void loadFonts() {
        final Class<KloC> clazz = KloC.class;
        final int size = -1;
        // FIXME Some fonts not loading (e.g. Thin versions)
        // Low priority: as long as Roboto Regular, Roboto Slab Regular and Material Icons Regular are loaded that's fine
        Font.loadFont(clazz.getResourceAsStream(toFontAsset("Material Icons/MaterialIcons-Regular.ttf")), size);
        Font.loadFont(clazz.getResourceAsStream(toFontAsset("Roboto/Roboto-Black.ttf")), size);
        Font.loadFont(clazz.getResourceAsStream(toFontAsset("Roboto/Roboto-BlackItalic.ttf")), size);
        Font.loadFont(clazz.getResourceAsStream(toFontAsset("Roboto/Roboto-Bold.ttf")), size);
        Font.loadFont(clazz.getResourceAsStream(toFontAsset("Roboto/Roboto-BoldItalic.ttf")), size);
        Font.loadFont(clazz.getResourceAsStream(toFontAsset("Roboto/Roboto-Italic.ttf")), size);
        Font.loadFont(clazz.getResourceAsStream(toFontAsset("Roboto/Roboto-Light.ttf")), size);
        Font.loadFont(clazz.getResourceAsStream(toFontAsset("Roboto/Roboto-LightItalic.ttf")), size);
        Font.loadFont(clazz.getResourceAsStream(toFontAsset("Roboto/Roboto-Medium.ttf")), size);
        Font.loadFont(clazz.getResourceAsStream(toFontAsset("Roboto/Roboto-MediumItalic.ttf")), size);
        Font.loadFont(clazz.getResourceAsStream(toFontAsset("Roboto/Roboto-Regular.ttf")), size);
        Font.loadFont(clazz.getResourceAsStream(toFontAsset("Roboto/Roboto-Thin.ttf")), size);
        Font.loadFont(clazz.getResourceAsStream(toFontAsset("Roboto/Roboto-ThinItalic.ttf")), size);
        Font.loadFont(clazz.getResourceAsStream(toFontAsset("Roboto Slab/RobotoSlab-Bold.ttf")), size);
        Font.loadFont(clazz.getResourceAsStream(toFontAsset("Roboto Slab/RobotoSlab-Light.ttf")), size);
        Font.loadFont(clazz.getResourceAsStream(toFontAsset("Roboto Slab/RobotoSlab-Thin.ttf")), size);
        Font.loadFont(clazz.getResourceAsStream(toFontAsset("Roboto Slab/RobotoSlab-Regular.ttf")), size);
    }

    @Contract(value = "_ -> !null", pure = true)
    @Nonnull
    @SuppressWarnings("SpellCheckingInspection")
    private static String toFontAsset(@Nullable final String str) {
        return "/assets/kloc/fonts/" + str;
    }
}
