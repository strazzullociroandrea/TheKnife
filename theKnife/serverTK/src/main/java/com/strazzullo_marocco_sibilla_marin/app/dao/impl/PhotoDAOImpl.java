package com.strazzullo_marocco_sibilla_marin.app.dao.impl;

import com.strazzullo_marocco_sibilla_marin.app.DBConnectionPool;
import com.strazzullo_marocco_sibilla_marin.app.dao.PhotoDAO;
import sibilla.Photo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Concrete JDBC implementation of the {@link PhotoDAO} interface.
 * Connects to the PostgreSQL database via {@link DBConnectionPool}.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA - author of this file
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class PhotoDAOImpl implements PhotoDAO {

    /**
     * Function to persist a new photo row.
     *
     * @param photo the photo to insert
     * @throws SQLException if a database error occurs
     */
    @Override
    public void insert(Photo photo) throws SQLException {
        String sql = "INSERT INTO photo (photo_id, location_id, url, uploaded_at) VALUES (?, ?, ?, ?)";
        Connection conn = DBConnectionPool.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, photo.getId());
            stmt.setString(2, photo.getLocationId());
            stmt.setString(3, photo.getUrl());
            stmt.setTimestamp(4, Timestamp.valueOf(photo.getUploadedAt()));
            stmt.executeUpdate();
        }
    }

    /**
     * Function to list every photo belonging to a location, ordered by upload date.
     *
     * @param locationId the location id
     * @return the list of photos
     * @throws SQLException if a database error occurs
     */
    @Override
    public List<Photo> findByLocation(String locationId) throws SQLException {
        String sql = "SELECT photo_id, location_id, url, uploaded_at FROM photo WHERE location_id = ? ORDER BY uploaded_at ASC";
        List<Photo> results = new ArrayList<>();
        Connection conn = DBConnectionPool.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, locationId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    /**
     * Function to find a single photo by id.
     *
     * @param photoId the photo id
     * @return the photo, if found
     * @throws SQLException if a database error occurs
     */
    @Override
    public Optional<Photo> findById(String photoId) throws SQLException {
        String sql = "SELECT photo_id, location_id, url, uploaded_at FROM photo WHERE photo_id = ?";
        Connection conn = DBConnectionPool.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, photoId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    /**
     * Function to find the id of the restaurant a photo's location belongs to.
     *
     * @param locationId the location id
     * @return the owning restaurant id, if the location exists
     * @throws SQLException if a database error occurs
     */
    @Override
    public Optional<String> findRestaurantIdByLocation(String locationId) throws SQLException {
        String sql = "SELECT restaurant_id FROM location WHERE location_id = ?";
        Connection conn = DBConnectionPool.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, locationId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(rs.getString("restaurant_id")) : Optional.empty();
            }
        }
    }

    /**
     * Function to check whether a user is one of the owners (managers) of a restaurant.
     *
     * @param restaurantId the restaurant id
     * @param userId       the user id
     * @return true if the user owns the restaurant
     * @throws SQLException if a database error occurs
     */
    @Override
    public boolean isRestaurantOwner(String restaurantId, String userId) throws SQLException {
        String sql = "SELECT 1 FROM restaurant_owner WHERE restaurant_id = ? AND user_id = ?";
        Connection conn = DBConnectionPool.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, restaurantId);
            stmt.setString(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Function to delete a photo row by id.
     *
     * @param photoId the photo id
     * @throws SQLException if a database error occurs
     */
    @Override
    public void delete(String photoId) throws SQLException {
        String sql = "DELETE FROM photo WHERE photo_id = ?";
        Connection conn = DBConnectionPool.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, photoId);
            stmt.executeUpdate();
        }
    }

    private Photo mapRow(ResultSet rs) throws SQLException {
        return new Photo(
                rs.getString("photo_id"),
                rs.getString("location_id"),
                rs.getString("url"),
                rs.getTimestamp("uploaded_at").toLocalDateTime());
    }
}
