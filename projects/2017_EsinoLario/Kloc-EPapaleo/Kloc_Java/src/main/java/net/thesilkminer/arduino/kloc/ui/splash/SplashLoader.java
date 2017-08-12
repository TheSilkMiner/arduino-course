package net.thesilkminer.arduino.kloc.ui.splash;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
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

    public void load() throws IOException {
        final Stage stage = new Stage();
        final AnchorPane bg = GenericLoader.<AnchorPane>ofUnchecked("splash/splash_bg.fxml").load();
        final BorderPane fg = GenericLoader.<BorderPane>ofUnchecked("splash/splash_fg.fxml").load();
        final StackPane root = GenericLoader.<StackPane>ofUnchecked("splash/splash.fxml").load();
        root.getChildren().add(bg);
        root.getChildren().add(fg);
        root.opacityProperty().setValue(0);
        stage.setScene(new Scene(root));
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.getScene().setFill(Color.TRANSPARENT);
        stage.setResizable(false);
        stage.show();

        final Timeline timeline = new Timeline();
        final KeyFrame key = new KeyFrame(Duration.seconds(2), new KeyValue(root.opacityProperty(), 1));
        timeline.getKeyFrames().add(key);
        timeline.play();
    }
}
