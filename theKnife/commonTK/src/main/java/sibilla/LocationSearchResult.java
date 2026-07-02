package sibilla;

import java.io.Serial;
import java.io.Serializable;

/**
 * A single row of a location search result: the {@link Location} itself plus the
 * restaurant/rating data that only exists via the {@code view_location_rating} view
 * and the parent {@code restaurant} row, and is therefore not part of {@link Location}.
 *
 * @param location the matched location
 * @param averageRating the average review rating for this location, or null if it has no reviews
 * @param reviewCount the number of reviews for this location
 * @param restaurantName the name of the parent restaurant brand
 * @param restaurantCuisine the cuisine type of the parent restaurant
 * @param distanceKm the distance from the search's reference point in km, or null if no distance filter was used
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public record LocationSearchResult(
        Location location,
        Double averageRating,
        long reviewCount,
        String restaurantName,
        String restaurantCuisine,
        Double distanceKm
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
