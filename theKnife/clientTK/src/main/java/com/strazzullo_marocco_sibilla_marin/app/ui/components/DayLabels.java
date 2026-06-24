package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import sibilla.Day;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Italian display labels for {@link Day}, shared by every screen that lists or picks an opening
 * day ({@link FilterPanel}'s day picker, the location detail screen's opening hours table),
 * keyed in calendar order.
 *
 * @Author Marocco Stefano, 762192, VA - author of this file
 */
public final class DayLabels {

    private static final Map<Day, String> LABELS = new LinkedHashMap<>();

    static {
        LABELS.put(Day.monday, "Lunedì");
        LABELS.put(Day.tuesday, "Martedì");
        LABELS.put(Day.wednesday, "Mercoledì");
        LABELS.put(Day.thursday, "Giovedì");
        LABELS.put(Day.friday, "Venerdì");
        LABELS.put(Day.saturday, "Sabato");
        LABELS.put(Day.sunday, "Domenica");
    }

    private DayLabels() {
    }

    /**
     * Function to resolve a day's Italian display label.
     *
     * @param day the day
     * @return the Italian label
     */
    public static String of(Day day) {
        return LABELS.get(day);
    }

    /**
     * @return every day, in calendar order (Monday through Sunday)
     */
    public static Iterable<Day> orderedDays() {
        return LABELS.keySet();
    }
}
