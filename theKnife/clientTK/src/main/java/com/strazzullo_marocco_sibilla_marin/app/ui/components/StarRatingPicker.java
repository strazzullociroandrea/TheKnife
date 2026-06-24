package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * A row of five clickable star icons used to pick a minimum rating filter, replacing the
 * generic, plain-looking {@code Slider} previously used for the same purpose. Clicking a star
 * sets the rating to its position (1-5); clicking the already-selected star clears it back to 0,
 * meaning "any rating".
 *
 * @version 1.0
 * @Author Marocco Stefano, 762192, VA - author of this file
 */
public class StarRatingPicker extends HBox {

    private final IntegerProperty value = new SimpleIntegerProperty(0);
    private final FontIcon[] stars = new FontIcon[5];

    /**
     * StarRatingPicker constructor.
     */
    public StarRatingPicker() {
        setSpacing(4);
        for (int i = 0; i < stars.length; i++) {
            int position = i + 1;
            FontIcon star = new FontIcon(Feather.STAR);
            star.getStyleClass().add("tk-star-picker-icon");
            star.setCursor(javafx.scene.Cursor.HAND);
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
            stars[i].pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("selected"), i < selected);
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
