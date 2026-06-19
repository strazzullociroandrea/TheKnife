package com.strazzullo_marocco_sibilla_marin.app.dao.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.strazzullo_marocco_sibilla_marin.app.DBConnectionPool;
import com.strazzullo_marocco_sibilla_marin.app.dao.LocationDAO;
import marocco.SearchFilter;
import sibilla.Cuisine;
import sibilla.Day;
import sibilla.Location;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Concrete JDBC implementation of the {@link LocationDAO} interface.
 * Implements database search querying using dynamic SQL mapping from a {@link SearchFilter}.
 * Matches criteria against every attribute of the location (sede) and the restaurant name.
 * Connects to the PostgreSQL database via {@link DBConnectionPool}.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA - author of this file
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class LocationDAOImpl implements LocationDAO {
    /**
     * The Gson instance for parsing JSONB content.
     */
    private final Gson gson;

    /**
     * LocationDAOImpl constructor to initialize the DAO.
     */
    public LocationDAOImpl() {
        this.gson = new Gson();
    }

    /**
     * Function to dynamically search restaurant locations using criteria defined in the {@link SearchFilter}.
     *
     * @param filter the filter criteria
     * @return a list of locations matching the filter criteria
     * @throws SQLException if a database query error occurs
     */
    @Override
    public List<Location> search(SearchFilter filter) throws SQLException {
        List<Location> results = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Retrieve DB connection from the singleton pool
            conn = DBConnectionPool.getInstance().getConnection();

            StringBuilder query = new StringBuilder();
            query.append("SELECT l.*, r.name AS restaurant_name, r.cuisine_type AS restaurant_cuisine, ")
                 .append("v.avg_rating, v.review_count ");

            boolean hasDistance = filter.hasDistanceFilter();
            if (hasDistance) {
                // Haversine formula for calculating distance in kilometers:
                // distance = 6371 * 2 * ASIN(SQRT(sin^2(dLat/2) + cos(lat1) * cos(lat2) * sin^2(dLon/2)))
                query.append(", (6371 * 2 * ASIN(SQRT(POWER(SIN((l.latitude - ?) * pi()/180 / 2), 2) + ")
                     .append("COS(? * pi()/180) * COS(l.latitude * pi()/180) * ")
                     .append("POWER(SIN((l.longitude - ?) * pi()/180 / 2), 2)))) AS distance ");
            }

            query.append("FROM location l ")
                 .append("JOIN restaurant r ON l.restaurant_id = r.restaurant_id ")
                 .append("LEFT JOIN view_location_rating v ON l.location_id = v.location_id ")
                 .append("WHERE 1=1 ");

            List<Object> params = new ArrayList<>();

            // 1. Add select parameters for distance if present
            if (hasDistance) {
                params.add(filter.getLatRef());
                params.add(filter.getLatRef());
                params.add(filter.getLonRef());
            }

            // 2. Restaurant Name filter
            if (filter.getRestaurantName() != null && !filter.getRestaurantName().trim().isEmpty()) {
                query.append("AND LOWER(r.name) LIKE ? ");
                params.add("%" + filter.getRestaurantName().toLowerCase() + "%");
            }

            // 3. Country filter
            if (filter.getCountry() != null && !filter.getCountry().trim().isEmpty()) {
                query.append("AND LOWER(l.country) = ? ");
                params.add(filter.getCountry().toLowerCase().trim());
            }

            // 4. City filter
            if (filter.getCity() != null && !filter.getCity().trim().isEmpty()) {
                query.append("AND LOWER(l.city) = ? ");
                params.add(filter.getCity().toLowerCase().trim());
            }

            // 5. Address filter
            if (filter.getAddress() != null && !filter.getAddress().trim().isEmpty()) {
                query.append("AND LOWER(l.address) LIKE ? ");
                params.add("%" + filter.getAddress().toLowerCase() + "%");
            }

            // 6. Cuisine filter
            if (filter.getCuisineType() != null) {
                query.append("AND r.cuisine_type = ? ");
                params.add(filter.getCuisineType().name());
            }

            // 7. Price range filter
            if (filter.getPriceRange() != null) {
                query.append("AND l.price_range = ? ");
                params.add(filter.getPriceRange());
            }

            // 8. Delivery filter
            if (filter.getDelivery() != null) {
                query.append("AND l.delivery = ? ");
                params.add(filter.getDelivery());
            }

            // 9. Takeaway filter
            if (filter.getTakeaway() != null) {
                query.append("AND l.takeaway = ? ");
                params.add(filter.getTakeaway());
            }

            // 10. Max Capacity filter (minimum capacity required to fit a reservation request)
            if (filter.getMaxCapacity() != null) {
                query.append("AND l.max_capacity >= ? ");
                params.add(filter.getMaxCapacity());
            }

            // 11. Vegetarian Menu filter
            if (filter.getVegetarianMenu() != null) {
                query.append("AND l.vegetarian_menu = ? ");
                params.add(filter.getVegetarianMenu());
            }

            // 12. Vegan Menu filter
            if (filter.getVeganMenu() != null) {
                query.append("AND l.vegan_menu = ? ");
                params.add(filter.getVeganMenu());
            }

            // 13. Gluten Free Menu filter
            if (filter.getGlutenFreeMenu() != null) {
                query.append("AND l.gluten_free_menu = ? ");
                params.add(filter.getGlutenFreeMenu());
            }

            // 14. Open Day filter (check key existence in JSONB using jsonb_exists to avoid JDBC placeholder conflicts)
            if (filter.getOpenDay() != null) {
                query.append("AND jsonb_exists(l.opening_hours, ?) ");
                params.add(filter.getOpenDay().name().toLowerCase());
            }

            // 15. Rating filter
            if (filter.getMinRating() != null) {
                query.append("AND COALESCE(v.avg_rating, 0.0) >= ? ");
                params.add(filter.getMinRating());
            }

            // 16. Distance constraint (WHERE clause)
            if (hasDistance) {
                query.append("AND (6371 * 2 * ASIN(SQRT(POWER(SIN((l.latitude - ?) * pi()/180 / 2), 2) + ")
                     .append("COS(? * pi()/180) * COS(l.latitude * pi()/180) * ")
                     .append("POWER(SIN((l.longitude - ?) * pi()/180 / 2), 2)))) <= ? ");
                params.add(filter.getLatRef());
                params.add(filter.getLatRef());
                params.add(filter.getLonRef());
                params.add(filter.getRadiusKm());
            }

            // Order clause
            if (hasDistance) {
                query.append(" ORDER BY distance ASC");
            } else {
                query.append(" ORDER BY COALESCE(v.avg_rating, 0.0) DESC");
            }

            stmt = conn.prepareStatement(query.toString());
            int idx = 1;
            for (Object param : params) {
                stmt.setObject(idx++, param);
            }

            rs = stmt.executeQuery();
            while (rs.next()) {
                Location location = mapRowToLocation(rs);
                results.add(location);
            }

        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { /* ignored */ }
            }
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) { /* ignored */ }
            }
        }

        return results;
    }

    /**
     * Function to map a row of ResultSet to a {@link Location} entity object.
     * Deserializes the opening_hours JSONB field into a Day-based Map.
     *
     * @param rs the ResultSet containing location table rows
     * @return the constructed Location entity
     * @throws SQLException if a database mapping error occurs
     */
    private Location mapRowToLocation(ResultSet rs) throws SQLException {
        String id = rs.getString("location_id");
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

        // Parse JSONB opening hours
        Map<Day, String> openingTimes = new HashMap<>();
        String jsonHours = rs.getString("opening_hours");
        if (jsonHours != null && !jsonHours.trim().isEmpty()) {
            try {
                Type type = new TypeToken<Map<String, String>>(){}.getType();
                Map<String, String> rawMap = gson.fromJson(jsonHours, type);
                if (rawMap != null) {
                    for (Map.Entry<String, String> entry : rawMap.entrySet()) {
                        try {
                            Day day = Day.valueOf(entry.getKey().toUpperCase());
                            openingTimes.put(day, entry.getValue());
                        } catch (IllegalArgumentException e) {
                            // Ignore unrecognized days in enum mapping
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to parse opening hours JSON: " + jsonHours);
            }
        }

        return new Location(id, country, city, address, latitude, longitude,
                priceRange, delivery, takeaway, maxCapacity, vegetarianMenu, veganMenu, glutenFreeMenu, openingTimes);
    }
}
