package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

/**
 * Row of five hand-drawn star shapes, filled and amber up to the nearest whole star for a given
 * rating out of 5, outlined for the rest. Drawn as vector polygons rather than icon-font glyphs,
 * which proved unreliable to render consistently. Shared by {@link ResultCard} and {@link
 * ReviewCard} so every star rating in the app reads the same way; {@link StarRatingPicker} reuses
 * the same {@link #buildStar(double, boolean)} shape for its clickable rating filter.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class StarRating extends HBox {

    static final double DEFAULT_OUTER_RADIUS = 8;
    static final Color FILLED_COLOR = Color.web("#f5a623");
    static final Color EMPTY_COLOR = Color.web("#d0d5dd");

    /**
     * @param rating the rating to display, out of 5; values outside 0-5 are clamped
     */
    public StarRating(double rating) {
        super(4);
        setAlignment(Pos.CENTER_LEFT);
        int fullStars = Math.round((float) Math.max(0, Math.min(5, rating)));
        for (int i = 1; i <= 5; i++) {
            getChildren().add(buildStar(DEFAULT_OUTER_RADIUS, i <= fullStars));
        }
    }

    /**
     * Function to draw a single five-pointed star, filled and amber or outlined and gray.
     *
     * @param outerRadius the star's outer point radius; the inner radius follows at the
     *                     classic ~0.4 ratio
     * @param filled whether to draw it filled (amber) or outlined (gray)
     * @return the star shape
     */
    static Polygon buildStar(double outerRadius, boolean filled) {
        double innerRadius = outerRadius * 0.4;
        Polygon star = new Polygon();
        for (int i = 0; i < 10; i++) {
            double angle = Math.PI / 5 * i - Math.PI / 2;
            double radius = (i % 2 == 0) ? outerRadius : innerRadius;
            star.getPoints().addAll(radius * Math.cos(angle) + outerRadius, radius * Math.sin(angle) + outerRadius);
        }
        if (filled) {
            star.setFill(FILLED_COLOR);
        } else {
            star.setFill(Color.TRANSPARENT);
            star.setStroke(EMPTY_COLOR);
            star.setStrokeWidth(1.4);
        }
        return star;
    }
}
