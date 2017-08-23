package net.thesilkminer.arduino.kloc;

import net.thesilkminer.arduino.kloc.ui.splash.SplashLoader;
import net.thesilkminer.arduino.kloc.util.Scheduler;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Handles the initialization phase.
 *
 * @author TheSilkMiner
 * @since 1.0
 */
public enum InitHandler {
    /**
     * The unique instance of this handler.
     *
     * @since 1.0
     */
    HANDLER;

    void createSplash() throws IOException {
        SplashLoader.IT.createSplash();
    }

    void initApp() {
        Scheduler.getInstance().scheduleTask(() -> {
            SplashLoader.IT.updateProgressBar(-1, "Preparing application...");
            Scheduler.getInstance().scheduleTask(() -> {
                // TODO
            }, 4500);
        }, 5, TimeUnit.SECONDS);
    }
}
