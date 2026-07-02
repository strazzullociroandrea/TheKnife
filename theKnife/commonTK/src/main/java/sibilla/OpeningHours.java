package sibilla;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/**
 * Helper to evaluate a {@link Location}'s opening hours against a point in time.
 * Opening hours are stored as {@code "HH:mm-HH:mm"} strings keyed by {@link Day}, as written by
 * the location persistence layer.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 * @version 1.0
 */
public final class OpeningHours {

    private OpeningHours() {}

    /**
     * Function to check whether a location is open right now.
     *
     * @param location the location to check
     * @return true if the location has an opening hours entry for today that covers the current time
     */
    public static boolean isOpenNow(Location location) {
        return isOpenAt(location, LocalDateTime.now());
    }

    /**
     * Function to check whether a location is open at a given point in time.
     *
     * @param location the location to check
     * @param when the point in time to check
     * @return true if the location has an opening hours entry for that day that covers that time
     */
    public static boolean isOpenAt(Location location, LocalDateTime when) {
        if (location == null || when == null || location.getOpeningTimes() == null) {
            return false;
        }
        Day day = Day.valueOf(when.getDayOfWeek().name().toLowerCase());
        String hours = location.getOpeningTimes().get(day);
        LocalTime[] range = parseRange(hours);
        if (range == null) {
            return false;
        }
        LocalTime now = when.toLocalTime();
        return !now.isBefore(range[0]) && now.isBefore(range[1]);
    }

    /**
     * Function to parse an {@code "HH:mm-HH:mm"} opening hours string.
     *
     * @param hours the opening hours string for a single day
     * @return a two-element array with the opening and closing time, or null if malformed
     */
    public static LocalTime[] parseRange(String hours) {
        if (hours == null || hours.isBlank()) {
            return null;
        }
        String[] parts = hours.split("-");
        if (parts.length != 2) {
            return null;
        }
        try {
            return new LocalTime[] { LocalTime.parse(parts[0].trim()), LocalTime.parse(parts[1].trim()) };
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
