package com.strazzullo_marocco_sibilla_marin.app.dao.impl;

import com.strazzullo_marocco_sibilla_marin.app.DBConnectionPool;
import com.strazzullo_marocco_sibilla_marin.app.dao.RestaurantDAO;
import sibilla.Restaurant;
import sibilla.Cuisine;
import strazzullo.User;
import strazzullo.Manager;
import strazzullo.Client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Concrete JDBC implementation of the {@link RestaurantDAO} interface.
 * Manages database operations for restaurants including CRUD operations,
 * owner management, and retrieval of restaurants by various criteria.
 * Connects to the PostgreSQL database via {@link DBConnectionPool}.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA - author of this file
 * @Author Marin Marco, 760622, VA
 */
public class RestaurantDAOImpl implements RestaurantDAO {

    /**
     * Maps a single row from the restaurant table ResultSet to a Restaurant entity object.
     * Deserializes the cuisine_type field into a Cuisine enum.
     *
     * @param rs the ResultSet containing a restaurant table row
     * @return the constructed Restaurant entity with empty owners and locations lists
     * @throws SQLException if a database mapping error occurs
     */
    private Restaurant mapRow(ResultSet rs) throws SQLException {
        String id = rs.getString("restaurant_id");
        String name = rs.getString("name");
        String cuisineStr = rs.getString("cuisine_type");
        Cuisine cuisine = null;
        if (cuisineStr != null && !cuisineStr.trim().isEmpty()) {
            try {
                cuisine = Cuisine.valueOf(cuisineStr);
            } catch (IllegalArgumentException e) {
                // unknown cuisine stored, ignore
            }
        }
        return new Restaurant(id, name, cuisine, new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Retrieves all owners (User objects) associated with a specific restaurant
     * by querying the restaurant_owner and app_user tables.
     *
     * @param restaurantId the restaurant's unique identifier
     * @return a list of User objects (Manager or Client) representing the owners
     * @throws SQLException if a database query error occurs
     */
    private List<User> getOwnersForRestaurant(String restaurantId) throws SQLException {
        String sql = "SELECT u.user_id, u.first_name, u.last_name, u.email, u.password_hash, u.date_of_birth, u.city, u.role " +
                "FROM restaurant_owner ro JOIN app_user u ON ro.user_id = u.user_id WHERE ro.restaurant_id = ?";
        List<User> owners = new ArrayList<>();
        Connection conn = DBConnectionPool.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, restaurantId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String uid = rs.getString("user_id");
                    String first = rs.getString("first_name");
                    String last = rs.getString("last_name");
                    String email = rs.getString("email");
                    String pwd = rs.getString("password_hash");
                    String dob = rs.getString("date_of_birth");
                    String city = rs.getString("city");
                    String role = rs.getString("role");
                    if (role != null && role.equalsIgnoreCase("gestore")) {
                        owners.add(new Manager(uid, first, last, email, pwd, city, dob, true));
                    } else {
                        owners.add(new Client(uid, first, last, email, pwd, city, dob, true));
                    }
                }
            }
        }
        return owners;
    }

    /**
     * Retrieves a restaurant by its unique identifier and populates its owners list.
     *
     * @param id the unique identifier of the restaurant
     * @return an Optional containing the restaurant if found, or empty if not found
     * @throws SQLException if a database query error occurs or the id is null/empty
     */
    @Override
    public Optional<Restaurant> findById(String id) throws SQLException {
        if (id == null || id.isEmpty()) throw new SQLException("id is null or empty");

        String sql = "SELECT restaurant_id, name, cuisine_type FROM restaurant WHERE restaurant_id = ?";
        Connection conn = DBConnectionPool.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                Restaurant r = mapRow(rs);
                r.setOwners(getOwnersForRestaurant(id));
                return Optional.of(r);
            }
        }
    }

    /**
     * Retrieves all restaurants owned by a specific user (manager).
     *
     * @param id the unique identifier of the owner (user)
     * @return a list of Restaurant objects owned by the specified user; each with its owners list populated
     * @throws SQLException if a database query error occurs or the id is null/empty
     */
    @Override
    public List<Restaurant> findByOwner(String id) throws SQLException {
        if (id == null || id.isEmpty()) throw new SQLException("owner id is null or empty");
        String sql = "SELECT r.restaurant_id, r.name, r.cuisine_type FROM restaurant r " +
                "JOIN restaurant_owner ro ON r.restaurant_id = ro.restaurant_id WHERE ro.user_id = ?";

        List<Restaurant> results = new ArrayList<>();
        Connection conn = DBConnectionPool.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Restaurant r = mapRow(rs);
                    r.setOwners(getOwnersForRestaurant(r.getId()));
                    results.add(r);
                }
            }
        }
        return results;
    }

    /**
     * Creates a new restaurant in the database and links it to an initial owner.
     * If the restaurant does not have an id, a UUID is automatically generated.
     * Uses a transaction to ensure both operations succeed atomically.
     *
     * @param restaurant the restaurant object to create
     * @param ownerId the user id of the initial owner to link to this restaurant
     * @return the created restaurant with database-generated attributes and owners list populated
     * @throws SQLException if a database operation error occurs, or if restaurant/ownerId is null/empty
     */
    @Override
    public Restaurant create(Restaurant restaurant, String ownerId) throws SQLException {
        if (restaurant == null) throw new SQLException("restaurant is null");
        if (ownerId == null || ownerId.isEmpty()) throw new SQLException("owner id is null or empty");
        if (restaurant.getId() == null || restaurant.getId().isEmpty()) {
            restaurant.setId(UUID.randomUUID().toString());
        }

        String sql = "INSERT INTO restaurant(restaurant_id, name, cuisine_type) VALUES(?, ?, ?)";
        String linkSql = "INSERT INTO restaurant_owner(restaurant_id, user_id) VALUES(?, ?)";
        Connection conn = DBConnectionPool.getInstance().getConnection();
        boolean autoCommit = conn.getAutoCommit();

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, restaurant.getId());
                stmt.setString(2, restaurant.getName());
                stmt.setString(3, restaurant.getCuisine() != null ? restaurant.getCuisine().name() : null);
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(linkSql)) {
                stmt.setString(1, restaurant.getId());
                stmt.setString(2, ownerId);
                stmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            try {
                conn.setAutoCommit(autoCommit);
            } catch (SQLException e) {
                // ignore
            }
        }

        return findById(restaurant.getId()).orElse(restaurant);
    }

    /**
     * Updates an existing restaurant's name and cuisine type in the database.
     *
     * @param restaurant the restaurant object with updated values
     * @return the updated restaurant with its owners list populated
     * @throws SQLException if a database operation error occurs, if no restaurant was updated,
     *         or if the restaurant/id is null/empty
     */
    @Override
    public Restaurant update(Restaurant restaurant) throws SQLException {
        if (restaurant == null) throw new SQLException("restaurant is null");
        if (restaurant.getId() == null || restaurant.getId().isEmpty()) throw new SQLException("restaurant id is null or empty");

        String sql = "UPDATE restaurant SET name = ?, cuisine_type = ? WHERE restaurant_id = ?";
        Connection conn = DBConnectionPool.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, restaurant.getName());
            stmt.setString(2, restaurant.getCuisine() != null ? restaurant.getCuisine().name() : null);
            stmt.setString(3, restaurant.getId());
            int updated = stmt.executeUpdate();
            if (updated == 0) throw new SQLException("No restaurant updated, id may not exist: " + restaurant.getId());
        }
        return findById(restaurant.getId()).orElse(restaurant);
    }

    /**
     * Deletes a restaurant from the database by its id.
     * Cascading deletes will remove associated owners, locations, and reviews.
     *
     * @param id the unique identifier of the restaurant to delete
     * @throws SQLException if a database operation error occurs or if id is null/empty
     */
    @Override
    public void delete(String id) throws SQLException {
        if (id == null || id.isEmpty()) throw new SQLException("id is null or empty");

        String sql = "DELETE FROM restaurant WHERE restaurant_id = ?";
        Connection conn = DBConnectionPool.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Adds a user as an owner of a restaurant by inserting a new entry in the restaurant_owner table.
     *
     * @param restaurantId the unique identifier of the restaurant
     * @param ownerId the unique identifier of the user to add as owner
     * @return the updated restaurant with its owners list populated
     * @throws SQLException if a database operation error occurs, or if restaurantId/ownerId is null/empty
     */
    @Override
    public Restaurant addOwnerToRestaurant(String restaurantId, String ownerId) throws SQLException {
        if (restaurantId == null || restaurantId.isEmpty()) throw new SQLException("restaurant id is null or empty");
        if (ownerId == null || ownerId.isEmpty()) throw new SQLException("owner id is null or empty");

        String sql = "INSERT INTO restaurant_owner(restaurant_id, user_id) VALUES(?, ?)";
        Connection conn = DBConnectionPool.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, restaurantId);
            stmt.setString(2, ownerId);
            stmt.executeUpdate();
        }
        return findById(restaurantId).orElseThrow(() -> new SQLException("Restaurant not found after adding owner"));
    }

    /**
     * Removes a user from being an owner of a restaurant by deleting the entry in the restaurant_owner table.
     *
     * @param restaurantId the unique identifier of the restaurant
     * @param ownerId the unique identifier of the user to remove as owner
     * @return the updated restaurant with its owners list populated
     * @throws SQLException if a database operation error occurs, or if restaurantId/ownerId is null/empty
     */
    @Override
    public Restaurant removeOwnerFromRestaurant(String restaurantId, String ownerId) throws SQLException {
        if (restaurantId == null || restaurantId.isEmpty()) throw new SQLException("restaurant id is null or empty");
        if (ownerId == null || ownerId.isEmpty()) throw new SQLException("owner id is null or empty");

        String sql = "DELETE FROM restaurant_owner WHERE restaurant_id = ? AND user_id = ?";
        Connection conn = DBConnectionPool.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, restaurantId);
            stmt.setString(2, ownerId);
            stmt.executeUpdate();
        }
        return findById(restaurantId).orElseThrow(() -> new SQLException("Restaurant not found after removing owner"));
    }

    /**
     * Checks whether a specific user is an owner of a restaurant.
     *
     * @param restaurantId the unique identifier of the restaurant
     * @param ownerId the unique identifier of the user
     * @return true if the user is an owner of the restaurant; false otherwise
     * @throws SQLException if a database query error occurs, or if restaurantId/ownerId is null/empty
     */
    @Override
    public Boolean isOwner(String restaurantId, String ownerId) throws SQLException {
        if (restaurantId == null || restaurantId.isEmpty()) throw new SQLException("restaurant id is null or empty");
        if (ownerId == null || ownerId.isEmpty()) throw new SQLException("owner id is null or empty");

        String sql = "SELECT 1 FROM restaurant_owner WHERE restaurant_id = ? AND user_id = ?";
        Connection conn = DBConnectionPool.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, restaurantId);
            stmt.setString(2, ownerId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}
