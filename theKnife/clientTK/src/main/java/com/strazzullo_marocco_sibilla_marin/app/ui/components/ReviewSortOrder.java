package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import marin.Review;

import java.util.Comparator;

/**
 * Sort orders offered by {@link ReviewsSection} for a location's review list.
 *
 * @Author Marocco Stefano, 762192, VA - author of this file
 */
public enum ReviewSortOrder {

    MOST_RECENT("Più recenti", Comparator.comparing(Review::getReviewDate).reversed()),
    MOST_LIKED("Più utili", Comparator.comparingInt(Review::getReviewLikes).reversed()),
    BEST("Le migliori", Comparator.comparingInt(Review::getGlobalStars).reversed()),
    MOST_CRITICAL("Le più critiche", Comparator.comparingInt(Review::getGlobalStars));

    private final String label;
    private final Comparator<Review> comparator;

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

    @Override
    public String toString() {
        return label;
    }
}
