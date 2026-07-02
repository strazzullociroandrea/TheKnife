package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import sibilla.Cuisine;

/**
 * Resolves the Italian display label for a {@link Cuisine}, shared by {@link CuisineFilterRow}'s
 * quick-filter chips and the home screen's category rows so every cuisine name in the app reads
 * the same way.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public final class CuisineLabels {

    private CuisineLabels() {
    }

    /**
     * @param cuisine the cuisine
     * @return the Italian display label
     */
    public static String of(Cuisine cuisine) {
        return switch (cuisine) {
            case italian -> "Italiana";
            case chinese -> "Cinese";
            case thai -> "Tailandese";
            case mexican -> "Messicana";
            case indian -> "Indiana";
            case healthy -> "Sana";
            case japanese -> "Giapponese";
        };
    }
}
