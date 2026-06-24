package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.theme.Styles;
import com.strazzullo_marocco_sibilla_marin.app.geo.AddressGeocoder;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.function.Consumer;

/**
 * Small in-app panel that resolves the "you are here" point shown on the {@link
 * com.strazzullo_marocco_sibilla_marin.app.ui.map.MapView}, via {@link
 * AddressGeocoder#locateAutomatically()} (preferring a Wi-Fi scan over the coarser public-IP
 * lookup). Typing an address by hand is deliberately not offered here: the host screen's own
 * "Distanza" address field already does that job, and this panel's only reason to exist is the
 * one thing that field can't do for itself. The resolved address is fed back through that same
 * field, so the rest of the existing geocoding/search plumbing handles it unchanged.
 * Designed to be hosted inside an {@code atlantafx.base.controls.ModalPane}, like {@link FilterPanel}.
 *
 * @version 2.0
 * @Author Marocco Stefano, 762192, VA - author of this revision
 */
public class LocationPromptPanel extends VBox {

    private final AddressGeocoder geocoder;
    private final Label statusLabel = new Label();
    private final ProgressIndicator spinner = new ProgressIndicator();

    /**
     * LocationPromptPanel constructor.
     *
     * @param geocoder the geocoder used to resolve an automatic position guess
     * @param onResolved callback invoked with the resolved address once one is available
     * @param onClose callback invoked when the panel should be dismissed without resolving anything
     */
    public LocationPromptPanel(AddressGeocoder geocoder, Consumer<String> onResolved, Runnable onClose) {
        this.geocoder = geocoder;
        setPrefWidth(420);
        setMinWidth(420);
        setMaxWidth(420);
        setMaxHeight(Region.USE_PREF_SIZE);
        getStyleClass().addAll(Styles.BG_DEFAULT, "tk-modal-card");

        Label title = new Label("La tua posizione");
        title.getStyleClass().add(Styles.TITLE_3);
        Button closeButton = new Button("", new FontIcon(Feather.X));
        closeButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.BUTTON_CIRCLE);
        closeButton.setOnAction(e -> onClose.run());
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        HBox header = new HBox(title, headerSpacer, closeButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 20, 16, 24));

        Label explanation = new Label(
                "Mostra un riferimento sulla mappa per il filtro \"Distanza\", individuando"
                        + " automaticamente la tua posizione approssimativa.");
        explanation.setWrapText(true);
        explanation.getStyleClass().add(Styles.TEXT_MUTED);

        Button autoLocateButton = new Button("Usa la mia posizione approssimativa", new FontIcon(Feather.NAVIGATION));
        autoLocateButton.getStyleClass().add(Styles.ACCENT);
        autoLocateButton.setMaxWidth(Double.MAX_VALUE);
        autoLocateButton.setOnAction(e -> autoLocate(onResolved));

        spinner.setMaxSize(18, 18);
        spinner.setVisible(false);
        statusLabel.getStyleClass().add(Styles.TEXT_MUTED);
        statusLabel.setWrapText(true);
        HBox statusRow = new HBox(8, spinner, statusLabel);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        VBox body = new VBox(16, explanation, autoLocateButton, statusRow);
        body.setPadding(new Insets(4, 24, 24, 24));

        getChildren().addAll(header, new Separator(), body);
    }

    /**
     * Function to resolve the user's approximate position on a background thread — preferring a
     * Wi-Fi scan, falling back to the coarser IP-based guess — then reverse-geocode it into a
     * human-readable address.
     *
     * @param onResolved callback invoked with the resolved address
     */
    private void autoLocate(Consumer<String> onResolved) {
        setBusy(true, "Individuazione della posizione in corso...");
        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return geocoder.locateAutomatically()
                        .flatMap(coords -> geocoder.reverseGeocode(coords.lat(), coords.lng()))
                        .orElse(null);
            }
        };
        task.setOnSucceeded(e -> {
            setBusy(false, null);
            String address = task.getValue();
            if (address == null) {
                statusLabel.setText("Non siamo riusciti a determinare la tua posizione.");
            } else {
                onResolved.accept(address);
            }
        });
        task.setOnFailed(e -> setBusy(false, "Non siamo riusciti a determinare la tua posizione."));
        Thread thread = new Thread(task, "auto-locate-worker");
        thread.setDaemon(true);
        thread.start();
    }

    private void setBusy(boolean busy, String message) {
        Platform.runLater(() -> {
            spinner.setVisible(busy);
            statusLabel.setText(message == null ? "" : message);
        });
    }
}
