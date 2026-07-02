package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import javafx.util.StringConverter;
import sibilla.Day;

/**
 * Converts between {@link Day} values and their Italian display label, for {@link
 * FilterPanel}'s "Giorno" combo box, where a null value means "any day".
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
final class DayConverter extends StringConverter<Day> {

    /**
     * @param day the day to describe, or null for "any day"
     * @return the Italian display label
     */
    @Override
    public String toString(Day day) {
        return day == null ? "Qualsiasi giorno" : DayLabels.of(day);
    }

    /**
     * Function unused: the "Giorno" combo box is not editable, so the reverse direction is never
     * called.
     *
     * @param string unused
     * @return always null
     */
    @Override
    public Day fromString(String string) {
        return null;
    }
}
