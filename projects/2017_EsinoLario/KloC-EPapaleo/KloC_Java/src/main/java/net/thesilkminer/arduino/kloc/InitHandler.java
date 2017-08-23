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
