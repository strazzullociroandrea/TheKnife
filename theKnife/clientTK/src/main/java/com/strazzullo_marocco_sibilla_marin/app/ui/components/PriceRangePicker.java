package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.theme.Styles;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.List;

/**
 * Three selectable "how much do you want to spend" chips (€ / €€ / €€€), replacing a raw euro
 * amount spinner with the same price-tier language {@link PriceLabels} already shows on every
 * result card, so customers pick a budget bracket rather than typing a number that doesn't mean
 * anything to them at a glance. Clicking the already-selected chip clears it back to "any price".
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class PriceRangePicker extends HBox {

    private static final double CHEAP_MAX = 15.0;
    private static final double MID_MAX = 35.0;
    private static final double EXPENSIVE_MAX = 200.0;

    private final ToggleButton cheap = new ToggleButton("€");
    private final ToggleButton mid = new ToggleButton("€€");
    private final ToggleButton expensive = new ToggleButton("€€€");
    private final List<ToggleButton> chips = List.of(cheap, mid, expensive);

    /**
     * PriceRangePicker constructor.
     */
    public PriceRangePicker() {
        super(8);
        for (ToggleButton chip : chips) {
            chip.getStyleClass().addAll(Styles.BUTTON_OUTLINED, "tk-price-chip");
            chip.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(chip, Priority.ALWAYS);
            chip.setOnAction(e -> select(chip));
        }
        getChildren().addAll(chips);
    }

    /**
     * Function to enforce single-selection among the three chips: selecting one deselects the
     * other two, and clicking the already-selected chip clears it back to "any price".
     *
     * @param clicked the chip that was just clicked
     */
    private void select(ToggleButton clicked) {
        boolean nowSelected = clicked.isSelected();
        for (ToggleButton chip : chips) {
            chip.setSelected(chip == clicked && nowSelected);
        }
    }

    /**
     * @return the upper bound of the selected price bracket, or null if none is selected ("any price")
     */
    public Double getMaxPrice() {
        if (cheap.isSelected()) return CHEAP_MAX;
        if (mid.isSelected()) return MID_MAX;
        if (expensive.isSelected()) return EXPENSIVE_MAX;
        return null;
    }

    /**
     * @param maxPrice the upper bound to select the matching bracket for, or null to clear
     */
    public void setMaxPrice(Double maxPrice) {
        if (maxPrice == null) {
            chips.forEach(chip -> chip.setSelected(false));
            return;
        }
        cheap.setSelected(maxPrice <= CHEAP_MAX);
        mid.setSelected(maxPrice > CHEAP_MAX && maxPrice <= MID_MAX);
        expensive.setSelected(maxPrice > MID_MAX);
    }
}
