package com.strazzullo_marocco_sibilla_marin.app.dao;

import marocco.SearchFilter;
import sibilla.Location;

import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object (DAO) interface for performing search operations on restaurant location data.
 * Follows the Dependency Inversion Principle, decoupling business logic from JDBC implementation.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA - author of this file
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public interface LocationDAO {

    /**
     * Function to dynamically search restaurant locations using criteria defined in the {@link SearchFilter}.
     * Calculates distance filtering if geographical reference points and radius are supplied.
     *
     * @param filter the filter criteria
     * @return a list of locations matching the filter criteria
     * @throws SQLException if a database query error occurs
     */
    List<Location> search(SearchFilter filter) throws SQLException;
}
