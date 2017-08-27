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
package net.thesilkminer.arduino.kloc;

import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import net.thesilkminer.arduino.kloc.crash.CrashHandler;
import net.thesilkminer.arduino.kloc.crash.ReportedException;
import net.thesilkminer.arduino.kloc.util.WorkerThread;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
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

    private static final boolean MOCK_JAVA_9_FOR_TESTING_PURPOSES_MUST_BE_FALSE = false;

    static {
        System.setProperty("slf4j.detectLoggerNameMismatch", "true");
        System.setProperty("log4j.debug", "true");

        if (MOCK_JAVA_9_FOR_TESTING_PURPOSES_MUST_BE_FALSE) System.setProperty("java.version", "9");
    }

    public static final String NAME = "KloC Java Companion App";
    public static final String VERSION = "1.0";
    public static final SoftwareType TYPE = SoftwareType.DEV;
    private static final Logger LOGGER = LoggerFactory.getLogger(KloC.class);

    @Override
    public void start(final Stage primaryStage) throws Exception {
        LOGGER.info("Pre-init: loading splash screen");
        InitHandler.HANDLER.createSplash();
        LOGGER.info("Pre-init loading completed. Moving to init phase");

        LOGGER.info("App Initialization starting");
        LOGGER.trace("Deferring control to {}", InitHandler.class.getCanonicalName());
        InitHandler.HANDLER.initApp();
    }

    @Override
    public void stop() throws Exception {
        LOGGER.info("Application exit requested: performing cleanup");
        // TODO Cleanup
        // TODO Save preferences
        LOGGER.error("FATAL: Application terminated");
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

        LOGGER.info("Pre-init: setting crash handler");
        Thread.setDefaultUncaughtExceptionHandler(KloC::uncaughtExceptionHandler);

        LOGGER.info("Pre-init: initializing Java Swing layer");
        LOGGER.trace("Applying Black-Nimbus theme");

        try {
            final String className = Arrays.stream(UIManager.getInstalledLookAndFeels())
                    .filter(it -> "Nimbus".equalsIgnoreCase(it.getName()))
                    .findAny()
                    .map(UIManager.LookAndFeelInfo::getClassName)
                    .orElseThrow(() -> new ReportedException("Unable to apply Swing Look and Feel"));
            UIManager.setLookAndFeel(className);

            final Color base = new Color(40, 40, 40);
            UIManager.put("control", base);
            UIManager.put("text", base.brighter().brighter().brighter().brighter().brighter());
            UIManager.put("nimbusBase", new Color(0, 0, 0));
            UIManager.put("nimbusFocus", base);
            UIManager.put("nimbusBorder", base);
            UIManager.put("nimbusLightBackground", base);
            UIManager.put("info", base.brighter().brighter());
            UIManager.put("nimbusSelectionBackground", base.brighter().brighter());
        } catch (final ReflectiveOperationException | UnsupportedLookAndFeelException | ReportedException e) {
            // Force creation of a crash report
            uncaughtExceptionHandler(Thread.currentThread(), e);
            return;
        }

        LOGGER.info("Pre-init: checking Java version");
        try {
            checkForJava9();
        } catch (final Throwable t) {
            // Force creation of a crash report
            uncaughtExceptionHandler(Thread.currentThread(), t);
            return;
        }

        LOGGER.info("Pre-init: loading fonts");
        try {
            loadFonts();
        } catch (final Throwable t) {
            // Force creation of a crash report
            uncaughtExceptionHandler(Thread.currentThread(), new ReportedException("Unable to load fonts needed", t));
            return;
        }
        LOGGER.debug("Fonts loaded. Testing availability");
        LOGGER.debug("Roboto Regular: {}; Bold: {}; Thin: {}", Font.font("Roboto"),
                Font.font("Roboto", FontWeight.BOLD, 12), Font.font("Roboto Thin", FontWeight.THIN, -1));
        LOGGER.debug("Material Icons: {}", Font.font("Material Icons"));
        LOGGER.debug("Roboto Slab Regular: {}; Thin: {}",
                Font.font("Roboto Slab"), Font.font("Roboto Slab Thin", FontWeight.THIN, -1));

        try {
            net.thesilkminer.arduino.kloc.crash.CrashFrame.hasNimbus = true;
        } catch (final Throwable t) {
            // Force creation of a crash report
            uncaughtExceptionHandler(Thread.currentThread(), new ReportedException("Unable to configure Nimbus Look and Feel", t));
            return;
        }

        final Thread startup = new Thread(() -> {
            LOGGER.info("Pre-init: Launching JavaFX app with arguments {}", (Object) args);
            launch(args);
        });
        startup.setName("JavaFX Application Startup Thread");
        startup.start();

        final Thread worker = new Thread(WorkerThread.INSTANCE);
        worker.setName("Worker Thread");
        worker.start();
    }

    private static void loadFonts() throws Throwable {
        final Class<KloC> clazz = KloC.class;
        final int size = -1;

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

    private static void checkForJava9() {
        final boolean isJava9 = System.getProperty("java.version").startsWith("9");
        if (isJava9) {
            LOGGER.error("FATAL: Java 9 identified. This is unsupported!");
            throw new UnsupportedClassVersionError("Java 9 is not currently supported");
        }
        LOGGER.info("Currently not running under Java 9: that's fine");
    }

    private static void uncaughtExceptionHandler(final Thread t, final Throwable e) {
        final Thread h = new Thread(() -> CrashHandler.handle(t, e));
        h.setName("Crash Handler Thread");
        h.start();
    }

    @Contract(value = "_ -> !null", pure = true)
    @Nonnull
    @SuppressWarnings("SpellCheckingInspection")
    private static String toFontAsset(@Nullable final String str) {
        return "/assets/kloc/fonts/" + str;
    }
}
