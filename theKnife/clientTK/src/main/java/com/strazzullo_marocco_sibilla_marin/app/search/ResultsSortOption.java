package com.strazzullo_marocco_sibilla_marin.app.search;

import sibilla.LocationSearchResult;

import java.util.Comparator;
import java.util.Locale;

/**
 * Ways the search screen can order its results, each pairing an Italian display label with the
 * {@link Comparator} that implements it. Kept out of {@link
 * com.strazzullo_marocco_sibilla_marin.app.ui.SearchView} so the sorting rules themselves are
 * independent of how they're picked in the UI.
 *
 * @Author Marocco Stefano, 762192, VA - author of this file
 */
public enum ResultsSortOption {

    /** The order results were returned in by the search itself. */
    RECOMMENDED("Consigliati", null),
    /** Highest average rating first; results with no rating yet sort last. */
    HIGHEST_RATED("Valutazione più alta", Comparator.comparing(
            (LocationSearchResult r) -> r.averageRating() == null ? 0.0 : r.averageRating()).reversed()),
    /** Restaurant name, alphabetically, case- and locale-insensitive. */
    NAME_ASCENDING("Nome (A-Z)", Comparator.comparing(
            (LocationSearchResult r) -> r.restaurantName() == null ? "" : r.restaurantName().toLowerCase(Locale.ITALIAN)));

    private final String label;
    private final Comparator<LocationSearchResult> comparator;

    ResultsSortOption(String label, Comparator<LocationSearchResult> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    /**
     * @return the Italian display label shown in the sort dropdown
     */
    public String label() {
        return label;
    }

    /**
     * @return the comparator implementing this order, or null if results should be left as
     *         returned by the search (i.e. {@link #RECOMMENDED})
     */
    public Comparator<LocationSearchResult> comparator() {
        return comparator;
    }

    /**
     * Function to resolve the option matching a display label.
     *
     * @param label the display label, as shown in the sort dropdown
     * @return the matching option, or {@link #RECOMMENDED} if no option has that label
     */
    public static ResultsSortOption fromLabel(String label) {
        for (ResultsSortOption option : values()) {
            if (option.label.equals(label)) {
                return option;
            }
        }
        return RECOMMENDED;
    }
}
