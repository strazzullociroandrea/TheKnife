package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.theme.Styles;
import com.strazzullo_marocco_sibilla_marin.app.geo.AddressAutocomplete;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * The search screen's "Distanza" row: an address reference and a search radius. Kept as a base
 * filter directly on the search screen (rather than buried in the advanced {@link FilterPanel})
 * since it also drives what's drawn on the map (the "you are here" pin and radius circle).
 * The leading nav icon doubles as a shortcut into the "your position" prompt, and the address
 * field grows to fill the row rather than sitting at a fixed width.
 *
 * @version 2.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class DistanceFilterRow extends HBox {

    private final TextField addressField = new TextField();
    private final Spinner<Double> radiusSpinner = new Spinner<>(1.0, 100.0, 10.0, 1.0);

    /**
     * DistanceFilterRow constructor.
     *
     * @param onChange callback invoked when the address is confirmed (Enter), cleared, or the
     *                  radius changes while an address is set
     * @param onLocateClick callback invoked when the leading nav icon is clicked, to open the
     *                       "your position" prompt
     */
    public DistanceFilterRow(Runnable onChange, Runnable onLocateClick) {
        setPadding(new Insets(0, 24, 16, 24));

        FontIcon navIcon = new FontIcon(Feather.NAVIGATION);
        navIcon.setCursor(Cursor.HAND);
        navIcon.setOnMouseClicked(e -> onLocateClick.run());

        addressField.setPromptText("Distanza da un indirizzo (opzionale)");
        AddressAutocomplete.attach(addressField);
        addressField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                onChange.run();
            }
        });
        HBox.setHgrow(addressField, Priority.ALWAYS);

        Button clearButton = new Button("", new FontIcon(Feather.X));
        clearButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.BUTTON_CIRCLE);
        clearButton.visibleProperty().bind(Bindings.isNotEmpty(addressField.textProperty()));
        clearButton.managedProperty().bind(clearButton.visibleProperty());
        clearButton.setOnAction(e -> {
            addressField.clear();
            onChange.run();
        });

        radiusSpinner.setEditable(true);
        radiusSpinner.setPrefWidth(90);
        radiusSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!addressField.getText().isBlank()) {
                onChange.run();
            }
        });

        HBox pill = new HBox(8,
                navIcon, addressField, clearButton,
                new Label("raggio"), radiusSpinner, new Label("km"));
        pill.getStyleClass().add("tk-search-pill");
        pill.setAlignment(Pos.CENTER_LEFT);
        pill.setPadding(new Insets(8, 16, 8, 16));
        pill.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(pill, Priority.ALWAYS);

        setFillHeight(true);
        getChildren().add(pill);
    }

    /**
     * @return the reference address, or blank if none was typed
     */
    public String getAddress() {
        return addressField.getText();
    }

    /**
     * Function to set the reference address, e.g. once resolved by the "your position" prompt.
     *
     * @param address the address to show
     */
    public void setAddress(String address) {
        addressField.setText(address);
    }

    /**
     * @return the configured search radius, in km
     */
    public double getRadiusKm() {
        return radiusSpinner.getValue();
    }
}
