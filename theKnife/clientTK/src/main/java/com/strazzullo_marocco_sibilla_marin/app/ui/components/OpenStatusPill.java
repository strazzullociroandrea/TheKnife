package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.theme.Styles;
import javafx.scene.control.Label;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import sibilla.Location;
import sibilla.OpeningHours;

/**
 * Small colored-dot-plus-label pill showing whether a {@link Location} is open right now,
 * shared by {@link ResultCard} and the location detail screen so both read as the same design.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class OpenStatusPill extends Label {

    /**
     * @param location the location whose opening hours determine the status
     */
    public OpenStatusPill(Location location) {
        boolean open = OpeningHours.isOpenNow(location);
        FontIcon dot = new FontIcon(Feather.CIRCLE);
        dot.getStyleClass().add(open ? Styles.SUCCESS : Styles.DANGER);
        setText(open ? "Aperto ora" : "Chiuso");
        setGraphic(dot);
        getStyleClass().addAll(Styles.TEXT_SMALL, Styles.TEXT_BOLD, "tk-status-pill",
                open ? "tk-status-open" : "tk-status-closed");
    }
}
