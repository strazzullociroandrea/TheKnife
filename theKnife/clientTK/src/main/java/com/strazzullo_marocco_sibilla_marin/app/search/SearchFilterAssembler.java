package com.strazzullo_marocco_sibilla_marin.app.search;

import marocco.SearchFilter;

/**
 * Translates a {@link SearchCriteria} snapshot of the search screen into the {@link SearchFilter}
 * sent over RMI. Kept separate from {@link com.strazzullo_marocco_sibilla_marin.app.ui.SearchView}
 * so this mapping can be reasoned about (and changed) without touching any JavaFX wiring.
 *
 * @Author Marocco Stefano, 762192, VA - author of this file
 */
public final class SearchFilterAssembler {

    private SearchFilterAssembler() {
    }

    /**
     * Function to assemble a {@link SearchFilter}, leaving out any criterion left blank/unset
     * in the given {@link SearchCriteria}.
     *
     * @param criteria the search screen's current criteria
     * @return the assembled filter
     */
    public static SearchFilter assemble(SearchCriteria criteria) {
        SearchFilter.Builder builder = new SearchFilter.Builder();
        var filters = criteria.advancedFilters();

        if (criteria.city() != null && !criteria.city().isBlank()) {
            builder.city(criteria.city().trim());
        }
        if (criteria.restaurantQuery() != null && !criteria.restaurantQuery().isBlank()) {
            builder.restaurantName(criteria.restaurantQuery().trim());
        }
        if (filters.cuisineType() != null) {
            builder.cuisineType(filters.cuisineType());
        }
        if (filters.maxPriceRange() != null) {
            builder.maxPriceRange(filters.maxPriceRange());
        }
        if (filters.delivery() != null) {
            builder.delivery(filters.delivery());
        }
        if (filters.takeaway() != null) {
            builder.takeaway(filters.takeaway());
        }
        if (filters.minCapacity() != null) {
            builder.maxCapacity(filters.minCapacity());
        }
        if (filters.vegetarianMenu() != null) {
            builder.vegetarianMenu(filters.vegetarianMenu());
        }
        if (filters.veganMenu() != null) {
            builder.veganMenu(filters.veganMenu());
        }
        if (filters.glutenFreeMenu() != null) {
            builder.glutenFreeMenu(filters.glutenFreeMenu());
        }
        if (filters.openDay() != null) {
            builder.openDay(filters.openDay());
            if (filters.openTime() != null) {
                builder.openTime(filters.openTime());
            }
        }
        if (criteria.distanceAddress() != null && !criteria.distanceAddress().isBlank()) {
            builder.distanceFromAddress(criteria.distanceAddress().trim(), criteria.distanceRadiusKm());
        }
        if (filters.minRating() != null) {
            builder.minRating(filters.minRating());
        }
        return builder.build();
    }
}
