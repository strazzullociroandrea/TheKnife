package com.strazzullo_marocco_sibilla_marin.app.ui;

import sibilla.Cuisine;
import sibilla.Day;

/**
 * Holds the advanced search criteria edited in {@link com.strazzullo_marocco_sibilla_marin.app.ui.components.FilterDialog},
 * kept separate from the city/query top bar fields so the two can be merged into a single
 * {@link marocco.SearchFilter} only when a search actually runs.
 *
 * @param cuisineType the required cuisine type, or null for any
 * @param maxPriceRange the maximum price the customer is willing to pay, or null for any
 * @param delivery whether delivery must be available, or null to not filter on it
 * @param takeaway whether takeaway must be available, or null to not filter on it
 * @param minCapacity the minimum seating capacity required, or null for any
 * @param vegetarianMenu whether a vegetarian menu is required, or null to not filter on it
 * @param veganMenu whether a vegan menu is required, or null to not filter on it
 * @param glutenFreeMenu whether a gluten-free menu is required, or null to not filter on it
 * @param openDay the day the location must be open on, or null for any
 * @param openTime the time of day (HH:mm) the location must be open at, or null for any
 * @param minRating the minimum average rating required, or null for any
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 * @version 2.0
 */
public record AdvancedFilters(
        Cuisine cuisineType,
        Double maxPriceRange,
        Boolean delivery,
        Boolean takeaway,
        Integer minCapacity,
        Boolean vegetarianMenu,
        Boolean veganMenu,
        Boolean glutenFreeMenu,
        Day openDay,
        String openTime,
        Double minRating
) {

    /**
     * Function to build an empty set of advanced filters, with every criterion unset.
     *
     * @return the empty filter set
     */
    public static AdvancedFilters empty() {
        return new AdvancedFilters(null, null, null, null, null, null, null, null, null, null, null);
    }

    /**
     * Function to check whether any criterion is actually set.
     *
     * @return true if at least one filter field is non-null
     */
    public boolean isEmpty() {
        return equals(empty());
    }

    /**
     * Function to derive a copy of these filters with a different cuisine, keeping every
     * other criterion unchanged. Used to keep the quick-filter chips and the advanced dialog
     * writing to the same single source of truth.
     *
     * @param cuisine the cuisine to set, or null to clear it
     * @return the derived filters
     */
    public AdvancedFilters withCuisineType(Cuisine cuisine) {
        return new AdvancedFilters(cuisine, maxPriceRange, delivery, takeaway, minCapacity,
                vegetarianMenu, veganMenu, glutenFreeMenu, openDay, openTime, minRating);
    }
}
