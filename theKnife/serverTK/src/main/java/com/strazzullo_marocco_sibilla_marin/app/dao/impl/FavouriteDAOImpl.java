package com.strazzullo_marocco_sibilla_marin.app.dao.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.strazzullo_marocco_sibilla_marin.app.DBConnectionPool;
import com.strazzullo_marocco_sibilla_marin.app.dao.FavouriteDAO;
import sibilla.Day;
import sibilla.Location;
import sibilla.LocationSearchResult;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * Concrete JDBC implementation of the {@link FavouriteDAO} interface.
 * Connects to the PostgreSQL database via {@link DBConnectionPool}.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class FavouriteDAOImpl implements FavouriteDAO {

    private final Gson gson = new Gson();

    /**
     * Adds a location to a user's favourites. A no-op if it is already favourited, relying on the
     * {@code favourite} table's composite primary key on {@code (user_id, location_id)}.
     *
     * @param userId the id of the user
     * @param locationId the id of the location to favourite
     * @throws SQLException if a database operation error occurs
     */
    @Override
    public void add(String userId, String locationId) throws SQLException {
        String query = "INSERT INTO favourite(user_id, location_id) VALUES (?, ?) " +
                "ON CONFLICT (user_id, location_id) DO NOTHING";

        try (Connection conn = DBConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, userId);
            stmt.setString(2, locationId);
            stmt.executeUpdate();
        }
    }

    /**
     * Removes a location from a user's favourites. A no-op if it was not favourited.
     *
     * @param userId the id of the user
     * @param locationId the id of the location to remove
     * @throws SQLException if a database operation error occurs
     */
    @Override
    public void remove(String userId, String locationId) throws SQLException {
        String query = "DELETE FROM favourite WHERE user_id = ? AND location_id = ?";

        try (Connection conn = DBConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, userId);
            stmt.setString(2, locationId);
            stmt.executeUpdate();
        }
    }

    /**
     * Retrieves the ids of every location a user has favourited.
     *
     * @param userId the id of the user
     * @return the set of favourited location ids, possibly empty
     * @throws SQLException if a database query error occurs
     */
    @Override
    public Set<String> listLocationIdsByUser(String userId) throws SQLException {
        String query = "SELECT location_id FROM favourite WHERE user_id = ?";

        try (Connection conn = DBConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, userId);

            Set<String> locationIds = new LinkedHashSet<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    locationIds.add(rs.getString("location_id"));
                }
            }
            return locationIds;
        }
    }

    /**
     * Retrieves a page of a user's favourite locations, most recently added first, joining in the
     * same rating and restaurant info {@link LocationDAOImpl#search} exposes.
     *
     * @param userId the id of the user
     * @param limit the maximum number of favourites to return
     * @param offset the number of favourites to skip, for pagination
     * @return the page's favourites, in most-recently-added-first order
     * @throws SQLException if a database query error occurs
     */
    @Override
    public List<LocationSearchResult> listByUser(String userId, int limit, int offset) throws SQLException {
        String query = "SELECT l.*, r.name AS restaurant_name, r.cuisine_type AS restaurant_cuisine, " +
                "v.avg_rating, v.review_count " +
                "FROM favourite f " +
                "JOIN location l ON f.location_id = l.location_id " +
                "JOIN restaurant r ON l.restaurant_id = r.restaurant_id " +
                "LEFT JOIN view_location_rating v ON l.location_id = v.location_id " +
                "WHERE f.user_id = ? " +
                "ORDER BY f.added_at DESC " +
                "LIMIT ? OFFSET ?";

        try (Connection conn = DBConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, userId);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);

            List<LocationSearchResult> results = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRowToSearchResult(rs));
                }
            }
            return results;
        }
    }

    /**
     * Function to map a row of ResultSet from the {@link #listByUser} query into a {@link
     * LocationSearchResult}, mirroring {@code LocationDAOImpl.mapRowToSearchResult}.
     *
     * @param rs the ResultSet positioned on a favourite row
     * @return the constructed search result
     * @throws SQLException if a database mapping error occurs
     */
    private LocationSearchResult mapRowToSearchResult(ResultSet rs) throws SQLException {
        Location location = mapRowToLocation(rs);
        Double averageRating = rs.getObject("avg_rating") != null ? rs.getDouble("avg_rating") : null;
        long reviewCount = rs.getLong("review_count");
        String restaurantName = rs.getString("restaurant_name");
        String restaurantCuisine = rs.getString("restaurant_cuisine");
        return new LocationSearchResult(location, averageRating, reviewCount, restaurantName, restaurantCuisine, null);
    }

    /**
     * Function to map a row of ResultSet to a {@link Location} entity object, mirroring {@code
     * LocationDAOImpl.mapRowToLocation}.
     *
     * @param rs the ResultSet containing location table rows
     * @return the constructed Location entity
     * @throws SQLException if a database mapping error occurs
     */
    private Location mapRowToLocation(ResultSet rs) throws SQLException {
        String id = rs.getString("location_id");
        String name = rs.getString("name");
        String country = rs.getString("country");
        String city = rs.getString("city");
        String address = rs.getString("address");
        float latitude = rs.getFloat("latitude");
        float longitude = rs.getFloat("longitude");
        int priceRange = rs.getInt("price_range");
        boolean delivery = rs.getBoolean("delivery");
        boolean takeaway = rs.getBoolean("takeaway");
        int maxCapacity = rs.getInt("max_capacity");
        boolean vegetarianMenu = rs.getBoolean("vegetarian_menu");
        boolean veganMenu = rs.getBoolean("vegan_menu");
        boolean glutenFreeMenu = rs.getBoolean("gluten_free_menu");

        Map<Day, String> openingTimes = new HashMap<>();
        String jsonHours = rs.getString("opening_hours");
        if (jsonHours != null && !jsonHours.trim().isEmpty()) {
            try {
                Type type = new TypeToken<Map<String, String>>(){}.getType();
                Map<String, String> rawMap = gson.fromJson(jsonHours, type);
                if (rawMap != null) {
                    for (Map.Entry<String, String> entry : rawMap.entrySet()) {
                        try {
                            Day day = Day.valueOf(entry.getKey().toLowerCase());
                            openingTimes.put(day, entry.getValue());
                        } catch (IllegalArgumentException e) {
                        }
                    }
                }
            } catch (Exception e) {
            }
        }

        return new Location(name, id, country, city, address, latitude, longitude,
                priceRange, delivery, takeaway, maxCapacity, vegetarianMenu, veganMenu, glutenFreeMenu, openingTimes);
    }
}
