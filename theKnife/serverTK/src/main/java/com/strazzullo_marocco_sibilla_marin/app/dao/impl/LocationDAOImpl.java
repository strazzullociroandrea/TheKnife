package com.strazzullo_marocco_sibilla_marin.app.dao.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.strazzullo_marocco_sibilla_marin.app.DBConnectionPool;
import com.strazzullo_marocco_sibilla_marin.app.dao.LocationDAO;
import com.strazzullo_marocco_sibilla_marin.app.geo.GeoPoint;
import com.strazzullo_marocco_sibilla_marin.app.geo.GeocodingException;
import com.strazzullo_marocco_sibilla_marin.app.geo.GeocodingService;
import com.strazzullo_marocco_sibilla_marin.app.geo.NominatimGeocodingService;
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
     * The geocoding service used to resolve an address into coordinates for distance search.
     */
    private final GeocodingService geocodingService;

    /**
     * LocationDAOImpl constructor to initialize the DAO.
     */
    public LocationDAOImpl() {
        this(new NominatimGeocodingService());
    }

    /**
     * LocationDAOImpl constructor with a custom geocoding service injected.
     *
     * @param geocodingService the geocoding service to use for address-based distance search
     */
    public LocationDAOImpl(GeocodingService geocodingService) {
        this.gson = new Gson();
        this.geocodingService = geocodingService;
    }

    /**
     * Function to dynamically search restaurant locations using criteria defined in the {@link SearchFilter}.
     * If the filter carries an address instead of coordinates, it is resolved via {@link GeocodingService}
     * before running the distance calculation.
     *
     * @param filter the filter criteria
     * @return a list of locations matching the filter criteria
     * @throws SQLException if a database query error occurs or the address cannot be geocoded
     */
    @Override
    public List<Location> search(SearchFilter filter) throws SQLException {
        List<Location> results = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionPool.getInstance().getConnection();

            Double latRef = filter.getLatRef();
            Double lonRef = filter.getLonRef();
            if (filter.hasAddressDistanceFilter() && !filter.hasCoordinatesDistanceFilter()) {
                try {
                    GeoPoint point = geocodingService.geocode(filter.getAddressRef());
                    latRef = point.getLatitude();
                    lonRef = point.getLongitude();
                } catch (GeocodingException e) {
                    throw new SQLException("Unable to resolve address for distance search: " + e.getMessage(), e);
                }
            }

            StringBuilder query = new StringBuilder();
            query.append("SELECT l.*, r.name AS restaurant_name, r.cuisine_type AS restaurant_cuisine, ")
                 .append("v.avg_rating, v.review_count ");

            boolean hasDistance = filter.hasDistanceFilter();
            if (hasDistance) {
                query.append(", (6371 * 2 * ASIN(SQRT(POWER(SIN((l.latitude - ?) * pi()/180 / 2), 2) + ")
                     .append("COS(? * pi()/180) * COS(l.latitude * pi()/180) * ")
                     .append("POWER(SIN((l.longitude - ?) * pi()/180 / 2), 2)))) AS distance ");
            }

            query.append("FROM location l ")
                 .append("JOIN restaurant r ON l.restaurant_id = r.restaurant_id ")
                 .append("LEFT JOIN view_location_rating v ON l.location_id = v.location_id ")
                 .append("WHERE 1=1 ");

            List<Object> params = new ArrayList<>();

            if (hasDistance) {
                params.add(latRef);
                params.add(latRef);
                params.add(lonRef);
            }

            if (filter.getRestaurantName() != null && !filter.getRestaurantName().trim().isEmpty()) {
                query.append("AND LOWER(r.name) LIKE ? ");
                params.add("%" + filter.getRestaurantName().toLowerCase() + "%");
            }

            if (filter.getLocationName() != null && !filter.getLocationName().trim().isEmpty()) {
                query.append("AND LOWER(l.name) LIKE ? ");
                params.add("%" + filter.getLocationName().toLowerCase() + "%");
            }

            if (filter.getCountry() != null && !filter.getCountry().trim().isEmpty()) {
                query.append("AND LOWER(l.country) = ? ");
                params.add(filter.getCountry().toLowerCase().trim());
            }

            if (filter.getCity() != null && !filter.getCity().trim().isEmpty()) {
                query.append("AND LOWER(l.city) = ? ");
                params.add(filter.getCity().toLowerCase().trim());
            }

            if (filter.getAddress() != null && !filter.getAddress().trim().isEmpty()) {
                query.append("AND LOWER(l.address) LIKE ? ");
                params.add("%" + filter.getAddress().toLowerCase() + "%");
            }

            if (filter.getCuisineType() != null) {
                query.append("AND r.cuisine_type = ? ");
                params.add(filter.getCuisineType().name());
            }

            if (filter.getPriceRange() != null) {
                query.append("AND l.price_range = ? ");
                params.add(filter.getPriceRange());
            }

            if (filter.getDelivery() != null) {
                query.append("AND l.delivery = ? ");
                params.add(filter.getDelivery());
            }

            if (filter.getTakeaway() != null) {
                query.append("AND l.takeaway = ? ");
                params.add(filter.getTakeaway());
            }

            if (filter.getMaxCapacity() != null) {
                query.append("AND l.max_capacity >= ? ");
                params.add(filter.getMaxCapacity());
            }

            if (filter.getVegetarianMenu() != null) {
                query.append("AND l.vegetarian_menu = ? ");
                params.add(filter.getVegetarianMenu());
            }

            if (filter.getVeganMenu() != null) {
                query.append("AND l.vegan_menu = ? ");
                params.add(filter.getVeganMenu());
            }

            if (filter.getGlutenFreeMenu() != null) {
                query.append("AND l.gluten_free_menu = ? ");
                params.add(filter.getGlutenFreeMenu());
            }

            if (filter.getOpenDay() != null) {
                query.append("AND jsonb_exists(l.opening_hours, ?) ");
                params.add(filter.getOpenDay().name().toLowerCase());
            }

            if (filter.getMinRating() != null) {
                query.append("AND COALESCE(v.avg_rating, 0.0) >= ? ");
                params.add(filter.getMinRating());
            }

            if (hasDistance) {
                query.append("AND (6371 * 2 * ASIN(SQRT(POWER(SIN((l.latitude - ?) * pi()/180 / 2), 2) + ")
                     .append("COS(? * pi()/180) * COS(l.latitude * pi()/180) * ")
                     .append("POWER(SIN((l.longitude - ?) * pi()/180 / 2), 2)))) <= ? ");
                params.add(latRef);
                params.add(latRef);
                params.add(lonRef);
                params.add(filter.getRadiusKm());
            }

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
                try { rs.close(); } catch (SQLException e) { }
            }
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) { }
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
                            Day day = Day.valueOf(entry.getKey().toUpperCase());
                            openingTimes.put(day, entry.getValue());
                        } catch (IllegalArgumentException e) {
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to parse opening hours JSON: " + jsonHours);
            }
        }

        return new Location(name, id, country, city, address, latitude, longitude,
                priceRange, delivery, takeaway, maxCapacity, vegetarianMenu, veganMenu, glutenFreeMenu, openingTimes);
    }
}
