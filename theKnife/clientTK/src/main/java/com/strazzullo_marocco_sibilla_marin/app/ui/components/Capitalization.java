package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import java.util.Locale;

/**
 * Capitalizes the first letter of a string, shared by every label that turns a lower-case domain
 * value (a cuisine name, an Italian month name from {@link java.time.format.TextStyle}, ...) into
 * a display label.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public final class Capitalization {

    private Capitalization() {
    }

    /**
     * Function to capitalize the first letter of a string.
     *
     * @param text the text to capitalize
     * @return the capitalized text
     */
    public static String capitalize(String text) {
        return text == null || text.isEmpty()
                ? text : text.substring(0, 1).toUpperCase(Locale.ITALIAN) + text.substring(1);
    }
}
