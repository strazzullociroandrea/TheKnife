package com.strazzullo_marocco_sibilla_marin.app.ui.map;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Owns {@link MapView}'s location pin markers and their shared popup bubble: which pins are
 * shown, which one (if any) is focused with its popup open, and the popup's "Dettagli" button.
 * Screen positions depend on {@link MapView}'s camera, so every pin/popup is projected via the
 * {@code projector} callback rather than this class knowing about the camera itself.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
final class MapPinOverlay {

    private final Pane overlay;
    private final BiFunction<Double, Double, double[]> projector;
    private final Consumer<List<double[]>> onPinsChanged;

    private final Map<String, MapPin> pins = new HashMap<>();
    private final Map<String, Circle> pinNodes = new HashMap<>();

    private final Label popupText = new Label();
    private final Button popupDetailsButton = new Button("Dettagli", new FontIcon(Feather.CHEVRON_RIGHT));
    private final VBox popupBox = new VBox(8, popupText, popupDetailsButton);

    private Consumer<String> onPinDetails = id -> {};
    private String focusedPinId;
    private boolean popupsEnabled = true;

    /**
     * @param overlay the pane pins and the popup are added to
     * @param projector converts a (lat, lng) pair into its current on-screen (x, y) position
     * @param onPinsChanged called with every currently shown pin's (lat, lng), whenever the pin
     *                       set changes, so the host can refit the camera to them
     */
    MapPinOverlay(Pane overlay, BiFunction<Double, Double, double[]> projector, Consumer<List<double[]>> onPinsChanged) {
        this.overlay = overlay;
        this.projector = projector;
        this.onPinsChanged = onPinsChanged;

        popupBox.getStyleClass().add("tk-map-popup");
        popupBox.setVisible(false);
        popupBox.setAlignment(Pos.CENTER_LEFT);
        popupDetailsButton.getStyleClass().add("tk-map-popup-button");
        popupDetailsButton.setContentDisplay(ContentDisplay.RIGHT);
        popupDetailsButton.setMaxWidth(Double.MAX_VALUE);
        popupDetailsButton.setOnAction(e -> {
            if (focusedPinId != null) {
                onPinDetails.accept(focusedPinId);
            }
        });
        overlay.getChildren().add(popupBox);
    }

    /**
     * @param handler the callback to invoke with the focused pin's id when "Dettagli" is pressed
     */
    void setOnPinDetails(Consumer<String> handler) {
        this.onPinDetails = handler == null ? id -> {} : handler;
    }

    /**
     * @param enabled whether clicking a pin opens its popup; disabled on read-only map previews
     */
    void setPopupsEnabled(boolean enabled) {
        this.popupsEnabled = enabled;
    }

    /**
     * @return every currently shown pin
     */
    Collection<MapPin> pins() {
        return pins.values();
    }

    /**
     * @param pinId the pin id to look up
     * @return the pin, or null if no pin with that id is currently shown
     */
    MapPin get(String pinId) {
        return pins.get(pinId);
    }

    /**
     * @return whether the popup is currently visible
     */
    boolean isPopupVisible() {
        return popupBox.isVisible();
    }

    /**
     * Function to hide the popup and clear the focused pin, if one is focused.
     */
    void hidePopup() {
        popupBox.setVisible(false);
        focusedPinId = null;
        refreshPinStyles();
    }

    /**
     * Function to replace every pin currently shown, notifying {@link #onPinsChanged} with their
     * coordinates so the host can refit the camera.
     *
     * @param newPins the pins to display
     */
    void setPins(List<MapPin> newPins) {
        overlay.getChildren().removeAll(pinNodes.values());
        pinNodes.clear();
        pins.clear();
        focusedPinId = null;
        popupBox.setVisible(false);

        List<double[]> points = new ArrayList<>();
        for (MapPin pin : newPins) {
            pins.put(pin.id(), pin);
            points.add(new double[]{pin.lat(), pin.lng()});
            overlay.getChildren().add(buildPinNode(pin));
        }
        onPinsChanged.accept(points);
    }

    /**
     * Function to build a pin's circle node, wiring its click to toggle its popup.
     *
     * @param pin the pin to build a node for
     * @return the pin's circle node
     */
    private Circle buildPinNode(MapPin pin) {
        Circle node = new Circle(7);
        node.getStyleClass().add("tk-map-pin");
        node.setOnMouseClicked(event -> {
            if (popupsEnabled) {
                if (pin.id().equals(focusedPinId) && popupBox.isVisible()) {
                    hidePopup();
                } else {
                    showPopupFor(pin.id());
                }
            }
            event.consume();
        });
        pinNodes.put(pin.id(), node);
        return node;
    }

    /**
     * Function to focus a pin and open its popup, without moving the camera (the caller is
     * responsible for that).
     *
     * @param pinId the id of the pin to focus
     */
    void focusPin(String pinId) {
        showPopupFor(pinId);
    }

    /**
     * Function to focus a pin, populate and show its popup, and reposition everything.
     *
     * @param pinId the id of the pin to show the popup for; a no-op if unknown
     */
    private void showPopupFor(String pinId) {
        MapPin pin = pins.get(pinId);
        if (pin == null) {
            return;
        }
        focusedPinId = pinId;
        popupText.setText(pin.title() + "\n" + pin.subtitle());
        popupBox.setVisible(true);
        popupBox.toFront();
        refreshPinStyles();
        reposition();
    }

    /**
     * Function to restyle every pin node, highlighting the currently focused one.
     */
    private void refreshPinStyles() {
        pinNodes.forEach((id, node) -> {
            node.getStyleClass().setAll(id.equals(focusedPinId) ? "tk-map-pin-focused" : "tk-map-pin");
        });
    }

    /**
     * Function to re-project every pin and the popup (if open) to their current screen position.
     * Called by {@link MapView} on every camera change.
     */
    void reposition() {
        for (Map.Entry<String, MapPin> entry : pins.entrySet()) {
            Circle node = pinNodes.get(entry.getKey());
            if (node == null) {
                continue;
            }
            double[] screen = projector.apply(entry.getValue().lat(), entry.getValue().lng());
            node.setCenterX(screen[0]);
            node.setCenterY(screen[1]);
        }
        if (focusedPinId != null && popupBox.isVisible()) {
            MapPin pin = pins.get(focusedPinId);
            double[] screen = projector.apply(pin.lat(), pin.lng());
            Platform.runLater(() -> {
                popupBox.setLayoutX(screen[0] - popupBox.getWidth() / 2);
                popupBox.setLayoutY(screen[1] - popupBox.getHeight() - 18);
            });
        }
    }
}
