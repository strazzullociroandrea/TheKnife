package com.strazzullo_marocco_sibilla_marin.app.dao.impl;

import com.strazzullo_marocco_sibilla_marin.app.dao.ReviewDAO;
import com.strazzullo_marocco_sibilla_marin.app.DBConnectionPool;
import marin.Review;
import sibilla.Restaurant;
import strazzullo.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Implementation of the {@link ReviewDAO} interface.
 *
 * Provides database access operations for managing reviews,
 * including CRUD operations and like management.
 * This class interacts with the underlying data source
 * and translates persistence operations into SQL queries.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class ReviewDAOImpl implements ReviewDAO{

    /**
     * Finds all reviews associated with the specified restaurant.
     *
     * @param restId the identifier of the restaurant
     * @return a list containing the restaurant reviews
     * @throws SQLException if the restaurant identifier is invalid or
     *         an error occurs while accessing the database
     */
    @Override
    public List<Review> findByRestaurant(String restId) throws SQLException {
        if (restId == null || restId.isEmpty()) {
            throw new SQLException("restId is null or empty");
        }
        String query = "SELECT * FROM review WHERE location_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Review> r = new ArrayList<>();

        try{
            conn = DBConnectionPool.getInstance().getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setString(1, restId);
            rs = stmt.executeQuery();
            while (rs.next()){
                String userId = rs.getString("user_id");
                String locationId = rs.getString("location_id");
                int globalStars = rs.getInt("rating");
                int priceStars = rs.getInt("rating_price");
                int hospitalityStars = rs.getInt("rating_hospitality");
                int serviceStars = rs.getInt("rating_service");
                String text = rs.getString("body");
                if(text == null){
                    r.add(new Review(userId, locationId, globalStars, priceStars, hospitalityStars, serviceStars));
                } else{
                    r.add(new Review(userId, locationId, globalStars, priceStars, hospitalityStars, serviceStars, text));
                }
            }
            return r;
        } finally{
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
            }
        }
    }

    /**
     * Finds all reviews written by the specified user.
     *
     * @param uId the identifier of the user
     * @return a list containing the user's reviews
     * @throws SQLException if the user identifier is invalid or
     *         an error occurs while accessing the database
     */
    @Override
    public List<Review> findByUser(String uId) throws SQLException {
        if (uId == null || uId.isEmpty()) {
            throw new SQLException("uId is null or empty");
        }
        String query = "SELECT * FROM review WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Review> r = new ArrayList<>();

        try{
            conn = DBConnectionPool.getInstance().getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setString(1, uId);
            rs = stmt.executeQuery();
            while (rs.next()){
                String userId = rs.getString("user_id");
                String locationId = rs.getString("location_id");
                int globalStars = rs.getInt("rating");
                int priceStars = rs.getInt("rating_price");
                int hospitalityStars = rs.getInt("rating_hospitality");
                int serviceStars = rs.getInt("rating_service");
                String text = rs.getString("body");
                if(text == null){
                    r.add(new Review(userId, locationId, globalStars, priceStars, hospitalityStars, serviceStars));
                } else{
                    r.add(new Review(userId, locationId, globalStars, priceStars, hospitalityStars, serviceStars, text));
                }
            }
            return r;
        } finally{
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
            }
        }
    }

    /**
     * Save a new review in the database.
     *
     * @param rev the review to be saved
     * @throws SQLException if the review is null, already exists,
     *         or an error occurs while accessing the database
     */
    @Override
    public void save(Review rev) throws SQLException {
        if (rev == null) throw new SQLException("review is null");
        if (rev.getReviewId() == null || rev.getReviewId().isEmpty()) {
            rev.setReviewId(UUID.randomUUID().toString());
        }

        boolean text = rev.getText() == null || rev.getText().isEmpty();
        String query;
        if (!text){
            query = "INSERT INTO review(review_id, user_id, location_id, rating, rating_price, rating_hospitality, rating_service) VALUES(?, ?, ?, ?, ?, ?, ?)";
        } else{
            query = "INSERT INTO review(review_id, user_id, location_id, rating, rating_price, rating_hospitality, rating_service, body) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        }

        String checkQuery = "SELECT count(*) AS recordCount FROM review WHERE review_id = ?";
        Connection conn = null;
        PreparedStatement checkStmt = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionPool.getInstance().getConnection();
            checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, rev.getReviewId());
            rs = checkStmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("recordCount");
                if (count > 0) {
                    throw new SQLException("This review already exists.");
                }
            }

            stmt = conn.prepareStatement(query);

            if (!text) {
                stmt.setString(1, rev.getReviewId());
                stmt.setString(2, rev.getUserId());
                stmt.setString(3, rev.getLocationId());
                stmt.setInt(4, rev.getGlobalStars());
                stmt.setInt(5, rev.getPriceStars());
                stmt.setInt(6, rev.getHospitalityStars());
                stmt.setInt(7, rev.getServiceStars());
            } else {
                stmt.setString(1, rev.getReviewId());
                stmt.setString(2, rev.getUserId());
                stmt.setString(3, rev.getLocationId());
                stmt.setInt(4, rev.getGlobalStars());
                stmt.setInt(5, rev.getPriceStars());
                stmt.setInt(6, rev.getHospitalityStars());
                stmt.setInt(7, rev.getServiceStars());
                stmt.setString(8, rev.getText());
            }

            stmt.executeUpdate();

        } finally {
            if (rs != null) try {
                rs.close();
            } catch (SQLException e) {
            }
            if (checkStmt != null) try {
                checkStmt.close();
            } catch (SQLException e) {
            }
            if (stmt != null) try {
                stmt.close();
            } catch (SQLException e) {
            }
            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
            }
        }
    }

    /**
     * Updates an existing review in the database.
     *
     * @param rev the review containing the updated data
     * @throws SQLException if the review or its identifier is invalid,
     *         or an error occurs while accessing the database
     */
    @Override
    public void update(Review rev) throws SQLException {
        if (rev == null) throw new SQLException("review is null");
        if (rev.getReviewId() == null || rev.getReviewId().isEmpty()) {
            throw new SQLException("review id is null or empty");
        }

        boolean text = rev.getText() == null || rev.getText().isEmpty();
        String query;
        if (!text) {
            query = "UPDATE review SET rating = ?, rating_price = ?, rating_hospitality = ?, rating_service = ? WHERE review_id = ?";
        } else {
            query = "UPDATE review SET rating = ?, rating_price = ?, rating_hospitality = ?, rating_service = ?, body = ? WHERE review_id = ?";
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnectionPool.getInstance().getConnection();
            stmt = conn.prepareStatement(query);

            if (!text) {
                stmt.setInt(1, rev.getGlobalStars());
                stmt.setInt(2, rev.getPriceStars());
                stmt.setInt(3, rev.getHospitalityStars());
                stmt.setInt(4, rev.getServiceStars());
                stmt.setString(5, rev.getReviewId());
            } else {
                stmt.setInt(1, rev.getGlobalStars());
                stmt.setInt(2, rev.getPriceStars());
                stmt.setInt(3, rev.getHospitalityStars());
                stmt.setInt(4, rev.getServiceStars());
                stmt.setString(5, rev.getText());
                stmt.setString(6, rev.getReviewId());
            }
            stmt.executeUpdate();

        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
            }
        }
    }

    /**
     * Removes a review from the database.
     *
     * @param reviewId the identifier of the review to delete
     * @throws SQLException if the review identifier is invalid or
     *         an error occurs while accessing the database
     */
    @Override
    public void delete(String reviewId) throws SQLException {
        if (reviewId == null || reviewId.isEmpty()) throw new SQLException("reviewId is null or empty");

        String query = "DELETE FROM review WHERE review_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnectionPool.getInstance().getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setString(1, reviewId);
            stmt.executeUpdate();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
            }
        }
    }

    /**
     * Finds a review by its unique identifier.
     *
     * @param reviewId the identifier of the review to retrieve
     * @return the review object, or null if no review was found
     * @throws SQLException if an error occurs while accessing the data source
     */
    @Override
    public Review getReviewById(String reviewId) throws SQLException{
        if (reviewId == null || reviewId.isEmpty()) {
            throw new SQLException("reviewId is null or empty");
        }
        String query = "SELECT * FROM review WHERE review_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Review r = null;

        try{
            conn = DBConnectionPool.getInstance().getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setString(1, reviewId);
            rs = stmt.executeQuery();
            if (rs.next()){
                String userId = rs.getString("user_id");
                String locationId = rs.getString("location_id");
                int globalStars = rs.getInt("rating");
                int priceStars = rs.getInt("rating_price");
                int hospitalityStars = rs.getInt("rating_hospitality");
                int serviceStars = rs.getInt("rating_service");
                String text = rs.getString("body");
                if(text == null){
                    r = new Review(userId, locationId, globalStars, priceStars, hospitalityStars, serviceStars);
                } else{
                    r = new Review(userId, locationId, globalStars, priceStars, hospitalityStars, serviceStars, text);
                }
                r.setReviewId(reviewId);
            }
            return r;
        } finally{
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
            }
        }
    }

    /**
     * Adds a like to a review on behalf of a user.
     *
     * @param userId the identifier of the user adding the like
     * @param reviewId the identifier of the review to like
     * @throws SQLException if an error occurs while accessing the data source
     */
    @Override
    public void addLike(String userId, String reviewId) throws SQLException {
        if (userId == null || userId.isEmpty()) {
            throw new SQLException("userId is null or empty");
        }
        if (reviewId == null || reviewId.isEmpty()) throw new SQLException("reviewId is null or empty");

        String query = "INSERT INTO review_like(user_id, review_id) VALUES (?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;

        try{
            conn = DBConnectionPool.getInstance().getConnection();
            stmt = conn.prepareStatement(query);

            stmt.setString(1, userId);
            stmt.setString(2, reviewId);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("User already liked this review", e);
        } finally {
            if (stmt != null) try {
                stmt.close();
            } catch (SQLException e) {
            }
            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
            }
        }
    }

    /**
     * Removes a like from a review on behalf of a user.
     *
     * @param userId the identifier of the user removing the like
     * @param reviewId the identifier of the review to unlike
     * @throws SQLException if an error occurs while accessing the data source
     */
    @Override
    public void removeLike(String userId, String reviewId) throws SQLException {
        if (userId == null || userId.isEmpty()) {
            throw new SQLException("userId is null or empty");
        }
        if (reviewId == null || reviewId.isEmpty()) throw new SQLException("reviewId is null or empty");

        String query = "DELETE FROM review_like WHERE user_id = ? AND review_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try{
            conn = DBConnectionPool.getInstance().getConnection();
            stmt = conn.prepareStatement(query);

            stmt.setString(1, userId);
            stmt.setString(2, reviewId);

            stmt.executeUpdate();
        } finally {
            if (stmt != null) try {
                stmt.close();
            } catch (SQLException e) {
            }
            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
            }
        }
    }

    /**
     * Finds the total number of likes for a specific review.
     *
     * @param reviewId the identifier of the review
     * @return the total number of likes
     * @throws SQLException if an error occurs while accessing the data source
     */
    @Override
    public int getReviewLikes(String reviewId) throws SQLException{
        if (reviewId == null || reviewId.isEmpty()) throw new SQLException("reviewId is null or empty");

        String query = "SELECT COUNT(*) AS like_count FROM review_like WHERE  review_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try{
            conn = DBConnectionPool.getInstance().getConnection();
            stmt = conn.prepareStatement(query);

            stmt.setString(1, reviewId);
            rs = stmt.executeQuery();

            int likes = 0;
            if(rs.next()) {
                likes = rs.getInt("like_count");
            }
            return likes;
        } finally{
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
            }
        }

    }

    /**
     * Finds the identifiers of all reviews liked by a specific user.
     *
     * @param userId the identifier of the user
     * @return a list of review identifiers liked by the user
     * @throws SQLException if an error occurs while accessing the data source
     */
    @Override
    public List<String> getLikesByUser (String userId) throws SQLException{
        if (userId == null || userId.isEmpty()) throw new SQLException("userId is null or empty");

        String query = "SELECT review_id FROM review_like WHERE  user_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<String> likes = new ArrayList<>();

        try{
            conn = DBConnectionPool.getInstance().getConnection();
            stmt = conn.prepareStatement(query);

            stmt.setString(1, userId);
            rs = stmt.executeQuery();


            while(rs.next()) {
                likes.add(rs.getString("review_id"));
            }
            return likes;
        } finally{
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
            }
        }
    }
}