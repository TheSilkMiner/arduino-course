package net.thesilkminer.arduino.kloc.ui.splash;

import com.jfoenix.controls.JFXProgressBar;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

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
        this.setIndeterminate();
        this.loadingText.setText("");
    }

    private void setDeterminate(final int progress) {
        this.progressBar.rotateProperty().setValue(0.0D);
        this.progressBar.progressProperty().setValue(progress / 100.0D);
    }

    private void setIndeterminate() {
        this.progressBar.rotateProperty().setValue(180.0D);
        this.progressBar.progressProperty().setValue(-1.0D);
    }
}
