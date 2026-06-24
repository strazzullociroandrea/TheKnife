package com.strazzullo_marocco_sibilla_marin.app.ui.components;

/**
 * Renders a location's numeric price range as a euro-sign scale, shared by {@link ResultCard}
 * and the location detail screen.
 *
 * @Author Marocco Stefano, 762192, VA - author of this file
 */
public final class PriceLabels {

    private PriceLabels() {
    }

    /**
     * Function to render a price range as a euro-sign scale.
     *
     * @param priceRange the location's price range
     * @return a string of one to three euro signs
     */
    public static String of(int priceRange) {
        if (priceRange <= 15) return "€";
        if (priceRange <= 35) return "€€";
        return "€€€";
    }
}
