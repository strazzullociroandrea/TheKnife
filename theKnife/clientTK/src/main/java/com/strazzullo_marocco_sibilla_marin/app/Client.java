package com.strazzullo_marocco_sibilla_marin.app;

import atlantafx.base.theme.PrimerLight;
import com.strazzullo_marocco_sibilla_marin.app.config.ClientConfig;
import com.strazzullo_marocco_sibilla_marin.app.rmi.ServiceLocator;
import com.strazzullo_marocco_sibilla_marin.app.ui.AppShell;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point for the TheKnife JavaFX client. Connects to the RMI services exposed by the
 * server, applies the AtlantaFX Primer Light theme, and shows the home screen.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class Client extends Application {

    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    /**
     * Function to launch the JavaFX application.
     *
     * @param args unused
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Function to connect to the server and show the home screen.
     *
     * @param stage the primary stage
     */
    @Override
    public void start(Stage stage) {
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        try {
            ServiceLocator.connect(ClientConfig.rmiHost(), ClientConfig.rmiPort());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to the TheKnife RMI services", e);
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Impossibile connettersi al server TheKnife (" + ClientConfig.rmiHost() + ":"
                            + ClientConfig.rmiPort() + "): " + e.getMessage());
            alert.showAndWait();
            Platform.exit();
            return;
        }

        Scene scene = new Scene(new AppShell(), 1280, 800);
        scene.getStylesheets().add(getClass().getResource("/theme/theknife-theme.css").toExternalForm());
        stage.setTitle("TheKnife");
        stage.setScene(scene);
        stage.setMinWidth(1024);
        stage.setMinHeight(680);
        stage.show();
    }
}
