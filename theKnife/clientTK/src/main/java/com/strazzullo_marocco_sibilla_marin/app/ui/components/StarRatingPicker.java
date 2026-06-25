package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Polygon;

/**
 * A row of five clickable hand-drawn stars used to pick a minimum rating filter, sharing {@link
 * StarRating}'s star shape so the filter panel's rating picker reads exactly like every other
 * star rating in the app. Clicking a star sets the rating to its position (1-5); clicking the
 * already-selected star clears it back to 0, meaning "any rating".
 *
 * @version 2.0
 * @Author Marocco Stefano, 762192, VA - author of this file
 */
public class StarRatingPicker extends HBox {

    private static final double OUTER_RADIUS = 11;

    private final IntegerProperty value = new SimpleIntegerProperty(0);
    private final Polygon[] stars = new Polygon[5];

    /**
     * StarRatingPicker constructor.
     */
    public StarRatingPicker() {
        setSpacing(6);
        setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < stars.length; i++) {
            int position = i + 1;
            Polygon star = StarRating.buildStar(OUTER_RADIUS, false);
            star.setCursor(Cursor.HAND);
            star.setOnMouseClicked(e -> setValue(getValue() == position ? 0 : position));
            stars[i] = star;
            getChildren().add(star);
        }
        value.addListener((obs, oldValue, newValue) -> refresh());
        refresh();
    }

    private void refresh() {
        int selected = getValue();
        for (int i = 0; i < stars.length; i++) {
            boolean filled = i < selected;
            stars[i].setFill(filled ? StarRating.FILLED_COLOR : javafx.scene.paint.Color.TRANSPARENT);
            stars[i].setStroke(filled ? null : StarRating.EMPTY_COLOR);
        }
    }

    /**
     * @return the currently selected minimum rating, from 0 (any rating) to 5
     */
    public int getValue() {
        return value.get();
    }

    /**
     * @param newValue the minimum rating to select, from 0 (any rating) to 5
     */
    public void setValue(int newValue) {
        value.set(Math.max(0, Math.min(5, newValue)));
    }

    /**
     * @return the rating property, for read-only observation
     */
    public IntegerProperty valueProperty() {
        return value;
    }
}
