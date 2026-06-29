package com.strazzullo_marocco_sibilla_marin.app.dao;

import strazzullo.User;

import java.sql.SQLException;

/**
 * Data Access Object interface for the {@code session} table.
 *
 * @version 1.0
 * @Author Marocco Stefano, 762192, VA
 */
public interface SessionDAO {

    /**
     * Persists a new session token for the given user, expiring after 30 days.
     *
     * @param token  the UUID session token
     * @param userId the owner's user id
     * @throws SQLException if the insert fails
     */
    void save(String token, String userId) throws SQLException;

    /**
     * Looks up the user associated with a token, validating that it has not expired.
     *
     * @param token the session token
     * @return the owner {@link User}, or {@code null} if the token is not found or expired
     * @throws SQLException if the query fails
     */
    User findUserByToken(String token) throws SQLException;

    /**
     * Deletes a single session by token.
     *
     * @param token the session token to delete
     * @throws SQLException if the delete fails
     */
    void delete(String token) throws SQLException;
}
