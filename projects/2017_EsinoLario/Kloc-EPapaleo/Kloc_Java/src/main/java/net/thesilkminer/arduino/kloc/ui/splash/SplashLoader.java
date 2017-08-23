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

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import net.thesilkminer.arduino.kloc.ui.GenericLoader;

import java.io.IOException;

public enum SplashLoader {
    IT;

    private SplashFgController controller;

    public void createSplash() throws IOException {
        final Stage stage = new Stage();

        final AnchorPane bg = GenericLoader.ofUnchecked("splash/splash_bg.fxml").getContainer();

        final GenericLoader fgLoader = GenericLoader.ofUnchecked("splash/splash_fg.fxml");
        final BorderPane fg = fgLoader.getContainer();
        this.controller = fgLoader.<SplashFgController>getController().orElseThrow(RuntimeException::new);

        final StackPane root = GenericLoader.ofUnchecked("splash/splash.fxml").getContainer();

        root.getChildren().add(bg);
        root.getChildren().add(fg);
        root.opacityProperty().setValue(0);

        stage.setScene(new Scene(root));
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.getScene().setFill(Color.TRANSPARENT);
        stage.setResizable(false);
        stage.show();

        final Timeline timeline = new Timeline();
        final KeyFrame key = new KeyFrame(Duration.seconds(2), new KeyValue(root.opacityProperty(), 1.0D));
        timeline.getKeyFrames().add(key);
        timeline.play();
    }

    public void updateProgressBar(final int progress, final String text) {
        if (com.sun.javafx.tk.Toolkit.getToolkit().isFxUserThread()) this.updateProgressBar0(progress, text);
        else Platform.runLater(() -> this.updateProgressBar0(progress, text));
    }

    private void updateProgressBar0(final int progress, final String text) {
        this.controller.setVisible();
        if (progress < 0) this.controller.setIndeterminate();
        else this.controller.setProgress(progress);
        this.controller.setText(text);
    }
}
