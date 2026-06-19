package com.strazzullo_marocco_sibilla_marin.app.dao;

import java.sql.SQLException;

import strazzullo.User;

/**
 * Data Access Object (DAO) interface for the {@code user} table.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public interface UserDAO {

    /**
     * Find a user using his email
     *
     * @param email the user's email
     * @return the user with this email
     * @throws SQLException
     */
    User findByEmail(String email) throws SQLException;

    /**
     * Find a user using his id
     *
     * @param id the user's id
     * @return the user with this id
     * @throws SQLException
     */
    User findById(String id) throws SQLException;

    /**
     * Function to save a new user
     *
     * @param u the new user
     * @throws SQLException
     */
    void save(User u) throws SQLException;

    /**
     * Function to update the user's data
     *
     * @param u the updated user
     */
    void update(User u) throw SQLException;

}