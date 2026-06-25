package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the fixed list of 30-minute time slots used by {@link FilterPanel}'s opening time picker.
 *
 * @Author Marocco Stefano, 762192, VA - author of this file
 */
final class OpeningTimeSlots {

    private OpeningTimeSlots() {
    }

    /**
     * @return the list of "HH:mm" time slots covering a full day
     */
    static List<String> all() {
        List<String> slots = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            slots.add(String.format("%02d:00", hour));
            slots.add(String.format("%02d:30", hour));
        }
        return slots;
    }
}
