package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.scene.control.Label;

/**
 * Small rounded pill label used for cuisine, price, and dietary tags, shared by {@link
 * ResultCard} and the location detail screen so every tag in the app reads as the same shape.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class TagLabel extends Label {

    /**
     * @param text the tag text, capitalized if not already
     */
    public TagLabel(String text) {
        super(text == null ? "" : Capitalization.capitalize(text));
        getStyleClass().addAll(Styles.TEXT_SMALL, Styles.TEXT_BOLD, "tk-tag");
        setPadding(new Insets(3, 10, 3, 10));
    }
}
