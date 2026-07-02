package com.strazzullo_marocco_sibilla_marin.app.dao.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.strazzullo_marocco_sibilla_marin.app.DBConnectionPool;
import com.strazzullo_marocco_sibilla_marin.app.config.GeoConfig;
import com.strazzullo_marocco_sibilla_marin.app.dao.LocationDAO;
import com.strazzullo_marocco_sibilla_marin.app.geo.GeoPoint;
import com.strazzullo_marocco_sibilla_marin.app.geo.GeocodingException;
import com.strazzullo_marocco_sibilla_marin.app.geo.GeocodingService;
import com.strazzullo_marocco_sibilla_marin.app.geo.GoogleGeocodingService;
import com.strazzullo_marocco_sibilla_marin.app.geo.NominatimGeocodingService;
import marocco.SearchFilter;
import sibilla.Cuisine;
import sibilla.Day;
import sibilla.Location;
import sibilla.LocationSearchResult;
import sibilla.Restaurant;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Concrete JDBC implementation of the {@link LocationDAO} interface.
 * Implements database search querying using dynamic SQL mapping from a {@link SearchFilter}.
 * Matches criteria against every attribute of the location (sede) and the restaurant name.
 * Connects to the PostgreSQL database via {@link DBConnectionPool}.
 *
 * @version 2.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
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
     * LocationDAOImpl constructor to initialize the DAO. Uses {@link GoogleGeocodingService}
     * when a {@code GOOGLE_MAPS_API_KEY} is configured, falling back to the free, key-less
     * {@link NominatimGeocodingService} otherwise.
     */
    public LocationDAOImpl() {
        this(defaultGeocodingService());
    }

    /**
     * Function to resolve the default geocoding service: Google when an API key is configured,
     * Nominatim otherwise.
     *
     * @return the resolved geocoding service
     */
    private static GeocodingService defaultGeocodingService() {
        String apiKey = GeoConfig.googleMapsApiKey();
        return apiKey != null ? new GoogleGeocodingService(apiKey) : new NominatimGeocodingService();
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
     * before running the distance calculation. The Haversine distance is computed once in a {@code search_base}
     * CTE so it can be reused in both the radius filter and the {@code ORDER BY} clause without duplicating the formula.
     * Borrows a connection from {@link DBConnectionPool} and returns it via try-with-resources.
     *
     * @param filter the filter criteria
     * @return a list of locations matching the filter criteria
     * @throws SQLException if a database query error occurs or the address cannot be geocoded
     */
    @Override
    public List<LocationSearchResult> search(SearchFilter filter) throws SQLException {
        try (Connection conn = DBConnectionPool.getInstance().getConnection()) {
            Double latRef = filter.getLatRef();
            Double lonRef = filter.getLonRef();
            boolean hasDistance = filter.hasDistanceFilter();
            if (filter.hasAddressDistanceFilter() && !filter.hasCoordinatesDistanceFilter()) {
                try {
                    GeoPoint point = geocodingService.geocode(filter.getAddressRef());
                    latRef = point.latitude();
                    lonRef = point.longitude();
                } catch (GeocodingException e) {
                    throw new SQLException("Unable to resolve address for distance search: " + e.getMessage(), e);
                }
            }

            StringBuilder query = new StringBuilder("WITH search_base AS (SELECT l.*, ")
                    .append("r.name AS restaurant_name, r.cuisine_type AS restaurant_cuisine, ")
                    .append("v.avg_rating, v.review_count");

            List<Object> params = new ArrayList<>();

            if (hasDistance) {
                query.append(", (6371 * 2 * ASIN(SQRT(POWER(SIN((l.latitude - ?) * pi()/180 / 2), 2) + ")
                     .append("COS(? * pi()/180) * COS(l.latitude * pi()/180) * ")
                     .append("POWER(SIN((l.longitude - ?) * pi()/180 / 2), 2)))) AS distance");
                params.add(latRef);
                params.add(latRef);
                params.add(lonRef);
            }

            query.append(" FROM location l ")
                 .append("JOIN restaurant r ON l.restaurant_id = r.restaurant_id ")
                 .append("LEFT JOIN view_location_rating v ON l.location_id = v.location_id) ")
                 .append("SELECT * FROM search_base WHERE 1=1 ");

            if (filter.getGeneralQuery() != null && !filter.getGeneralQuery().trim().isEmpty()) {
                query.append("AND (LOWER(restaurant_name) LIKE ? OR LOWER(name) LIKE ? ")
                     .append("OR LOWER(city) LIKE ? OR LOWER(address) LIKE ?) ");
                String pattern = "%" + filter.getGeneralQuery().trim().toLowerCase() + "%";
                params.add(pattern);
                params.add(pattern);
                params.add(pattern);
                params.add(pattern);
            }

            if (filter.getRestaurantName() != null && !filter.getRestaurantName().trim().isEmpty()) {
                query.append("AND LOWER(restaurant_name) LIKE ? ");
                params.add("%" + filter.getRestaurantName().trim().toLowerCase() + "%");
            }

            if (filter.getLocationName() != null && !filter.getLocationName().trim().isEmpty()) {
                query.append("AND LOWER(name) LIKE ? ");
                params.add("%" + filter.getLocationName().trim().toLowerCase() + "%");
            }

            if (filter.getCountry() != null && !filter.getCountry().trim().isEmpty()) {
                query.append("AND LOWER(country) = ? ");
                params.add(filter.getCountry().trim().toLowerCase());
            }

            if (filter.getCity() != null && !filter.getCity().trim().isEmpty()) {
                query.append("AND LOWER(city) = ? ");
                params.add(filter.getCity().trim().toLowerCase());
            }

            if (filter.getAddress() != null && !filter.getAddress().trim().isEmpty()) {
                query.append("AND LOWER(address) LIKE ? ");
                params.add("%" + filter.getAddress().trim().toLowerCase() + "%");
            }

            if (filter.getCuisineType() != null) {
                query.append("AND LOWER(restaurant_cuisine) = ? ");
                params.add(filter.getCuisineType().name().toLowerCase());
            }

            if (filter.getMaxPriceRange() != null) {
                query.append("AND price_range <= ? ");
                params.add(filter.getMaxPriceRange());
            }

            if (filter.getDelivery() != null) {
                query.append("AND delivery = ? ");
                params.add(filter.getDelivery());
            }

            if (filter.getTakeaway() != null) {
                query.append("AND takeaway = ? ");
                params.add(filter.getTakeaway());
            }

            if (filter.getMaxCapacity() != null) {
                query.append("AND max_capacity >= ? ");
                params.add(filter.getMaxCapacity());
            }

            if (filter.getVegetarianMenu() != null) {
                query.append("AND vegetarian_menu = ? ");
                params.add(filter.getVegetarianMenu());
            }

            if (filter.getVeganMenu() != null) {
                query.append("AND vegan_menu = ? ");
                params.add(filter.getVeganMenu());
            }

            if (filter.getGlutenFreeMenu() != null) {
                query.append("AND gluten_free_menu = ? ");
                params.add(filter.getGlutenFreeMenu());
            }

            if (filter.getOpenDay() != null) {
                String dayKey = filter.getOpenDay().name().toLowerCase();
                query.append("AND jsonb_exists(opening_hours, ?) ");
                params.add(dayKey);

                if (filter.getOpenTime() != null && !filter.getOpenTime().trim().isEmpty()) {
                    query.append("AND ?::time BETWEEN split_part(opening_hours ->> ?, '-', 1)::time ")
                         .append("AND split_part(opening_hours ->> ?, '-', 2)::time ");
                    params.add(filter.getOpenTime().trim());
                    params.add(dayKey);
                    params.add(dayKey);
                }
            }

            if (filter.getMinRating() != null) {
                query.append("AND COALESCE(avg_rating, 0.0) >= ? ");
                params.add(filter.getMinRating());
            }

            if (hasDistance) {
                query.append("AND distance <= ? ");
                params.add(filter.getRadiusKm());
            }

            query.append(hasDistance ? "ORDER BY distance ASC " : "ORDER BY COALESCE(avg_rating, 0.0) DESC ");

            if (filter.hasPagination()) {
                query.append("LIMIT ? OFFSET ?");
                params.add(filter.getLimit() != null ? filter.getLimit() : Integer.MAX_VALUE);
                params.add(filter.getOffset() != null ? filter.getOffset() : 0);
            }

            try (PreparedStatement stmt = conn.prepareStatement(query.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    stmt.setObject(i + 1, params.get(i));
                }

                List<LocationSearchResult> results = new ArrayList<>();
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        results.add(mapRowToSearchResult(rs, hasDistance));
                    }
                }
                return results;
            }
        }
    }

    /**
     * Function to map a row of ResultSet to a {@link Location} entity object.
     * Deserializes the opening_hours JSONB field into a Day-based Map. Keys are matched against
     * {@link Day} with {@code toLowerCase()} since the enum constants are lowercase ("monday", ...),
     * matching the JSON keys written by {@link #serializeOpeningTimes}.
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
                System.err.println("Failed to parse opening hours JSON: " + jsonHours);
            }
        }

        return new Location(name, id, country, city, address, latitude, longitude,
                priceRange, delivery, takeaway, maxCapacity, vegetarianMenu, veganMenu, glutenFreeMenu, openingTimes);
    }

    /**
     * Function to map a row of ResultSet from the {@code search} query into a {@link LocationSearchResult},
     * pairing the {@link Location} with the rating and restaurant fields the search query joins in.
     *
     * @param rs the ResultSet positioned on a search result row
     * @param hasDistance whether the row carries a computed "distance" column
     * @return the constructed search result
     * @throws SQLException if a database mapping error occurs
     */
    private LocationSearchResult mapRowToSearchResult(ResultSet rs, boolean hasDistance) throws SQLException {
        Location location = mapRowToLocation(rs);
        Double averageRating = rs.getObject("avg_rating") != null ? rs.getDouble("avg_rating") : null;
        long reviewCount = rs.getLong("review_count");
        String restaurantName = rs.getString("restaurant_name");
        String restaurantCuisine = rs.getString("restaurant_cuisine");
        Double distanceKm = hasDistance ? rs.getDouble("distance") : null;
        return new LocationSearchResult(location, averageRating, reviewCount, restaurantName, restaurantCuisine, distanceKm);
    }

    /**
     * Serializes a map of opening times (Day -> time string) into a JSON string for storage in JSONB.
     *
     * @param openingTimes the map of day to opening hours string (e.g., "09:00-22:00")
     * @return the JSON string representation of opening times, or "{}" if null or empty
     */
    private String serializeOpeningTimes(Map<Day, String> openingTimes) {
        if (openingTimes == null || openingTimes.isEmpty()) {
            return "{}";
        }
        Map<String, String> rawMap = new HashMap<>();
        for (Map.Entry<Day, String> entry : openingTimes.entrySet()) {
            rawMap.put(entry.getKey().name().toLowerCase(), entry.getValue());
        }
        return gson.toJson(rawMap);
    }

    /**
     * Creates a new location in the database for a specific restaurant.
     * Uses a transaction to ensure atomic insertion. Borrows a connection from
     * {@link DBConnectionPool} and returns it via try-with-resources.
     *
     * @param location the location object to create
     * @param restaurantId the id of the restaurant this location belongs to
     * @throws SQLException if a database operation error occurs, or if location/restaurantId is null
     */
    @Override
    public void create(Location location, String restaurantId) throws SQLException {
        if(location == null) throw new SQLException("location is null");
        if(restaurantId == null) throw new SQLException("restaurant id is null");

        String query = "INSERT INTO location(name, id, country, city, address, " +
                "latitude, longitude, price_range, delivery, takeaway, " +
                "max_capacity, vegetarian_menu, vegan_menu, gluten_free_Menu," +
                "opening_hours) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?::jsonb);";

        try (Connection conn = DBConnectionPool.getInstance().getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, location.getName());
                stmt.setString(2, location.getId());
                stmt.setString(3, location.getCountry());
                stmt.setString(4, location.getCity());
                stmt.setString(5, location.getAddress());
                stmt.setFloat(6, location.getLatitude());
                stmt.setFloat(7, location.getLongitude());
                stmt.setInt(8, location.getPriceRange());
                stmt.setBoolean(9, location.isDelivery());
                stmt.setBoolean(10, location.isTakeaway());
                stmt.setInt(11, location.getMaxCapacity());
                stmt.setBoolean(12, location.isVegetarianMenu());
                stmt.setBoolean(13, location.isVeganMenu());
                stmt.setBoolean(14, location.isGlutenFreeMenu());
                stmt.setString(15, serializeOpeningTimes(location.getOpeningTimes()));

                stmt.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommit);
            }
        }
    }

    /**
     * Retrieves a location by its unique identifier. Borrows a connection from
     * {@link DBConnectionPool} and returns it via try-with-resources.
     *
     * @param id the unique identifier of the location
     * @return an Optional containing the location if found, or empty if not found
     * @throws SQLException if a database query error occurs or the id is null/empty
     */
    @Override
    public Optional<Location> findById(String id) throws SQLException {
        if (id == null || id.isEmpty()) {
            throw new SQLException("id is null or empty");
        }
        String query = "SELECT * FROM location WHERE location_id = ?";

        try (Connection conn = DBConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToLocation(rs));
                }
                return Optional.empty();
            }
        }
    }

    /**
     * Retrieves a location by its unique identifier, plus its rating and parent restaurant info,
     * via the same join {@link #search} uses. Borrows a connection from {@link DBConnectionPool}
     * and returns it via try-with-resources.
     *
     * @param id the unique identifier of the location
     * @return the location's search result, or empty if not found
     * @throws SQLException if a database query error occurs or the id is null/empty
     */
    @Override
    public Optional<LocationSearchResult> findSearchResultById(String id) throws SQLException {
        if (id == null || id.isEmpty()) {
            throw new SQLException("id is null or empty");
        }
        String query = "SELECT l.*, r.name AS restaurant_name, r.cuisine_type AS restaurant_cuisine, " +
                "v.avg_rating, v.review_count " +
                "FROM location l " +
                "JOIN restaurant r ON l.restaurant_id = r.restaurant_id " +
                "LEFT JOIN view_location_rating v ON l.location_id = v.location_id " +
                "WHERE l.location_id = ?";

        try (Connection conn = DBConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRowToSearchResult(rs, false)) : Optional.empty();
            }
        }
    }

    /**
     * Retrieves all locations belonging to a specific restaurant. Borrows a connection from
     * {@link DBConnectionPool} and returns it via try-with-resources.
     *
     * @param restaurant the restaurant whose locations are to be retrieved
     * @return a list of Location objects associated with the specified restaurant
     * @throws SQLException if a database query error occurs or the restaurant is null
     */
    @Override
    public List<Location> findByRestaurant(Restaurant restaurant) throws SQLException {
        if(restaurant == null) throw new SQLException("restaurant is null");

        String query = "SELECT * FROM location WHERE restaurant_id = ?";

        try (Connection conn = DBConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, restaurant.getId());

            List<Location> locations = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    locations.add(mapRowToLocation(rs));
                }
            }
            return locations;
        }
    }

    /**
     * Finds the id of the restaurant a location belongs to. Borrows a connection from
     * {@link DBConnectionPool} and returns it via try-with-resources.
     *
     * @param locationId the location id
     * @return the owning restaurant id, if the location exists
     * @throws SQLException if a database query error occurs
     */
    @Override
    public Optional<String> findRestaurantIdById(String locationId) throws SQLException {
        String query = "SELECT restaurant_id FROM location WHERE location_id = ?";

        try (Connection conn = DBConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, locationId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(rs.getString("restaurant_id")) : Optional.empty();
            }
        }
    }

    /**
     * Updates an existing location's information in the database. Borrows a connection from
     * {@link DBConnectionPool} and returns it via try-with-resources.
     *
     * @param location the location object with updated values
     * @return the updated location object
     * @throws SQLException if a database operation error occurs, if no location was updated,
     *         or if the location/id is null/empty
     */
    @Override
    public Location update(Location location) throws SQLException {
        if (location == null) throw new SQLException("location is null");
        if (location.getId() == null || location.getId().isEmpty()) throw new SQLException("location id is null or empty");

        String query = "UPDATE location SET name = ?, country = ?, city = ?, address = ?, " +
                "latitude = ?, longitude = ?, price_range = ?, delivery = ?, takeaway = ?, " +
                "max_capacity = ?, vegetarian_menu = ?, vegan_menu = ?, gluten_free_menu = ?, " +
                "opening_hours = ?::jsonb WHERE location_id = ?;";

        try (Connection conn = DBConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, location.getName());
            stmt.setString(2, location.getCountry());
            stmt.setString(3, location.getCity());
            stmt.setString(4, location.getAddress());
            stmt.setFloat(5, location.getLatitude());
            stmt.setFloat(6, location.getLongitude());
            stmt.setInt(7, location.getPriceRange());
            stmt.setBoolean(8, location.isDelivery());
            stmt.setBoolean(9, location.isTakeaway());
            stmt.setInt(10, location.getMaxCapacity());
            stmt.setBoolean(11, location.isVegetarianMenu());
            stmt.setBoolean(12, location.isVeganMenu());
            stmt.setBoolean(13, location.isGlutenFreeMenu());
            stmt.setString(14, serializeOpeningTimes(location.getOpeningTimes()));
            stmt.setString(15, location.getId());

            int updated = stmt.executeUpdate();
            if (updated == 0) {
                throw new SQLException("No location updated, id may not exist: " + location.getId());
            }
            return location;
        }
    }

    /**
     * Deletes a location from the database by its id. Borrows a connection from
     * {@link DBConnectionPool} and returns it via try-with-resources.
     *
     * @param location the location object to delete (id is used)
     * @throws SQLException if a database operation error occurs, if no location was deleted,
     *         or if the location/id is null/empty
     */
    @Override
    public void delete(Location location) throws SQLException {
        if (location == null) throw new SQLException("location is null");
        if (location.getId() == null || location.getId().isEmpty()) throw new SQLException("location id is null or empty");

        String query = "DELETE FROM location WHERE location_id = ?;";

        try (Connection conn = DBConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, location.getId());
            int deleted = stmt.executeUpdate();
            if (deleted == 0) {
                throw new SQLException("No location deleted, id may not exist: " + location.getId());
            }
        }
    }

}
