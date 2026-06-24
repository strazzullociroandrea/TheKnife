package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.theme.Styles;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import sibilla.Day;
import sibilla.Location;
import sibilla.OpeningHours;

/**
 * Two-column grid of a {@link Location}'s opening hours for every day of the week, in calendar
 * order, with closed days shown in red ("Chiuso") rather than left blank. Used by the location
 * detail screen, and reusable wherever else a location's full week of hours needs showing.
 *
 * @Author Marocco Stefano, 762192, VA - author of this file
 */
public class OpeningHoursGrid extends GridPane {

    /**
     * @param location the location to show opening hours for
     */
    public OpeningHoursGrid(Location location) {
        setHgap(32);
        setVgap(10);
        ColumnConstraints halfWidth = new ColumnConstraints();
        halfWidth.setPercentWidth(50);
        getColumnConstraints().addAll(halfWidth, halfWidth);

        int index = 0;
        for (Day day : DayLabels.orderedDays()) {
            add(buildRow(location, day), index % 2, index / 2);
            index++;
        }
    }

    private HBox buildRow(Location location, Day day) {
        Label dayLabel = new Label(DayLabels.of(day));
        dayLabel.getStyleClass().add(Styles.TEXT_BOLD);
        dayLabel.setMinWidth(100);

        String hours = location.getOpeningTimes() == null ? null : location.getOpeningTimes().get(day);
        var range = OpeningHours.parseRange(hours);
        Label hoursLabel = new Label(range == null ? "Chiuso" : range[0] + " – " + range[1]);
        if (range == null) {
            hoursLabel.getStyleClass().add(Styles.DANGER);
        }

        HBox row = new HBox(16, dayLabel, hoursLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }
}
