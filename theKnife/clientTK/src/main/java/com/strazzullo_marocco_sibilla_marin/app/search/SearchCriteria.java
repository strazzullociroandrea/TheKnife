package com.strazzullo_marocco_sibilla_marin.app.search;

import com.strazzullo_marocco_sibilla_marin.app.ui.AdvancedFilters;

/**
 * Everything the search screen lets a customer specify, kept free of any JavaFX type so it can
 * be assembled into a {@link marocco.SearchFilter} (via {@link SearchFilterAssembler}) without
 * the assembling code depending on the UI controls it was read from.
 *
 * @param query the free-text query matched against restaurant name, location name, city, and
 *              address at once, or null/blank for any
 * @param advancedFilters the cuisine, price, dietary, opening-hours, and rating criteria
 * @param distanceAddress the address to search around, or null/blank to not filter by distance
 * @param distanceRadiusKm the search radius in km, only meaningful together with {@link #distanceAddress}
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public record SearchCriteria(
        String query,
        AdvancedFilters advancedFilters,
        String distanceAddress,
        double distanceRadiusKm) {
}
