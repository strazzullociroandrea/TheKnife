package com.strazzullo_marocco_sibilla_marin.app.ui.map;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * The floating zoom in/out, locate-me and recenter buttons stacked in {@link MapView}'s corner.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
final class MapControls extends VBox {

    /**
     * @param onZoomIn called when the zoom-in button is pressed
     * @param onZoomOut called when the zoom-out button is pressed
     * @param onLocate called when the locate-me button is pressed
     * @param onRecenter called when the recenter button is pressed
     */
    MapControls(Runnable onZoomIn, Runnable onZoomOut, Runnable onLocate, Runnable onRecenter) {
        super(10);

        Button zoomIn = mapButton(Feather.PLUS, "tk-map-control-button");
        zoomIn.setOnAction(e -> onZoomIn.run());

        Button zoomOut = mapButton(Feather.MINUS, "tk-map-control-button");
        zoomOut.setOnAction(e -> onZoomOut.run());

        VBox zoomGroup = new VBox(zoomIn, new Separator(), zoomOut);
        zoomGroup.getStyleClass().add("tk-map-zoom-group");
        zoomGroup.setMaxWidth(Region.USE_PREF_SIZE);
        zoomGroup.setMaxHeight(Region.USE_PREF_SIZE);

        Button locate = mapButton(Feather.NAVIGATION, "tk-map-locate-button");
        locate.setOnAction(e -> onLocate.run());

        Button recenter = mapButton(Feather.MAXIMIZE, "tk-map-recenter-button");
        recenter.setOnAction(e -> onRecenter.run());

        setAlignment(Pos.CENTER);
        setMaxWidth(Region.USE_PREF_SIZE);
        setMaxHeight(Region.USE_PREF_SIZE);
        getChildren().addAll(zoomGroup, locate, recenter);
    }

    /**
     * Function to build a single floating, square map control button with an explicitly colored
     * icon, so its visibility doesn't depend on inherited text-fill from the surrounding theme.
     *
     * @param icon the icon to display
     * @param styleClass the CSS class providing the button's background/shape
     * @return the button
     */
    private Button mapButton(Feather icon, String styleClass) {
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.getStyleClass().add("tk-map-control-icon");
        Button button = new Button("", fontIcon);
        button.getStyleClass().add(styleClass);
        return button;
    }
}
