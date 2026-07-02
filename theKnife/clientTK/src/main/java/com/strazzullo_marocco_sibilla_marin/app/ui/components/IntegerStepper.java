package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.theme.Styles;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * A "minus value plus" row for picking a bounded integer, shared by {@link BookingDialog}'s
 * people count and {@link FilterPanel}'s "In quanti siete" capacity filter, rather than each
 * screen rolling its own copy of the same three controls.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class IntegerStepper extends HBox {

    private final IntegerProperty value = new SimpleIntegerProperty();
    private final Label valueLabel = new Label();
    private final int min;
    private final int max;

    /**
     * @param min the smallest value this stepper can reach
     * @param max the largest value this stepper can reach
     * @param initial the starting value, clamped to [min, max]
     */
    public IntegerStepper(int min, int max, int initial) {
        super(16);
        this.min = min;
        this.max = max;
        setAlignment(Pos.CENTER);
        setMaxWidth(Double.MAX_VALUE);

        valueLabel.getStyleClass().add(Styles.TITLE_2);
        valueLabel.setMinWidth(40);
        valueLabel.setAlignment(Pos.CENTER);

        Button minusButton = new Button("", new FontIcon(Feather.MINUS));
        minusButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.BUTTON_CIRCLE, Styles.FLAT);
        Button plusButton = new Button("", new FontIcon(Feather.PLUS));
        plusButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.BUTTON_CIRCLE, Styles.FLAT);
        minusButton.setOnAction(e -> setValue(getValue() - 1));
        plusButton.setOnAction(e -> setValue(getValue() + 1));

        value.addListener((obs, oldValue, newValue) -> valueLabel.setText(String.valueOf(newValue)));
        setValue(initial);

        getChildren().addAll(minusButton, valueLabel, plusButton);
    }

    /**
     * @return the currently selected value
     */
    public int getValue() {
        return value.get();
    }

    /**
     * @param newValue the value to select, clamped to this stepper's [min, max] range
     */
    public void setValue(int newValue) {
        value.set(Math.max(min, Math.min(max, newValue)));
    }

    /**
     * @return the value property, for read-only observation
     */
    public IntegerProperty valueProperty() {
        return value;
    }
}
