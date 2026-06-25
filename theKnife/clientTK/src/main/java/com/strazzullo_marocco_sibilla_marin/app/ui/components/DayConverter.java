package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import javafx.util.StringConverter;
import sibilla.Day;

/**
 * Converts between {@link Day} values and their Italian display label, for {@link
 * FilterPanel}'s "Giorno" combo box, where a null value means "any day".
 *
 * @Author Marocco Stefano, 762192, VA - author of this file
 */
final class DayConverter extends StringConverter<Day> {

    @Override
    public String toString(Day day) {
        return day == null ? "Qualsiasi giorno" : DayLabels.of(day);
    }

    @Override
    public Day fromString(String string) {
        return null;
    }
}
