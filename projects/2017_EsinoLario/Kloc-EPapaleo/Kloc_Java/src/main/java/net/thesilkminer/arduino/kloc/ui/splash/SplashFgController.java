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
package net.thesilkminer.arduino.kloc.ui.splash;

import com.jfoenix.controls.JFXProgressBar;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Controls the foreground of the splash screen.
 *
 * @author TheSilkMiner
 * @since 1.0
 */
public final class SplashFgController {

    @FXML
    private Text loadingText;

    @FXML
    private JFXProgressBar progressBar;

    /**
     * Initializes the controller.
     *
     * <p>Called automatically after controller load.</p>
     *
     * @since 1.0
     */
    @FXML
    @SuppressWarnings("unused")
    private void initialize() {
        this.loadingText.opacityProperty().setValue(0.0D);
        this.progressBar.opacityProperty().setValue(0.0D);
        this.setIndeterminate();
        this.loadingText.setText("");
    }

    void setProgress(final int progress) {
        this.progressBar.rotateProperty().setValue(0.0D);
        this.progressBar.progressProperty().setValue(progress / 100.0D);
    }

    void setIndeterminate() {
        this.progressBar.rotateProperty().setValue(180.0D);
        this.progressBar.progressProperty().setValue(-1.0D);
    }

    void setVisible() {
        if (this.loadingText.opacityProperty().getValue() == 1.0D) return;

        final Timeline timeline = new Timeline();
        final KeyFrame textKeyFrame = new KeyFrame(Duration.seconds(2),
                new KeyValue(this.loadingText.opacityProperty(), 1.0D));
        final KeyFrame barKeyFrame = new KeyFrame(Duration.seconds(2),
                new KeyValue(this.progressBar.opacityProperty(), 1.0D));
        timeline.getKeyFrames().addAll(textKeyFrame, barKeyFrame);
        timeline.play();
    }

    void setText(final String txt) {
        this.loadingText.setText(txt);
    }
}
