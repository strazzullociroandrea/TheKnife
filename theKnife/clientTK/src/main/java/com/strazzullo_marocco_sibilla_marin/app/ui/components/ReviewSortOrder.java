package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import marin.Review;

import java.util.Comparator;

/**
 * Sort orders offered by {@link ReviewsSection} for a location's review list.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public enum ReviewSortOrder {

    MOST_RECENT("Più recenti", Comparator.comparing(Review::getReviewDate).reversed()),
    MOST_LIKED("Più utili", Comparator.comparingInt(Review::getReviewLikes).reversed()),
    BEST("Le migliori", Comparator.comparingInt(Review::getGlobalStars).reversed()),
    MOST_CRITICAL("Le più critiche", Comparator.comparingInt(Review::getGlobalStars));

    private final String label;
    private final Comparator<Review> comparator;

    /**
     * @param label the Italian display label shown in the sort picker
     * @param comparator the comparator implementing this sort order
     */
    ReviewSortOrder(String label, Comparator<Review> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    /**
     * @return the comparator implementing this sort order
     */
    public Comparator<Review> comparator() {
        return comparator;
    }

    /**
     * @return the Italian display label, shown directly by the sort picker's combo box
     */
    @Override
    public String toString() {
        return label;
    }
}
