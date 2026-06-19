package com.strazzullo_marocco_sibilla_marin.app.dao.impl;

import com.strazzullo_marocco_sibilla_marin.app.dao.UserDAO;
import com.strazzullo_marocco_sibilla_marin.app.DBConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import strazzullo.*;

/**
 * Concrete JDBC implementation of the {@link UserDAO} interface.
 * Connects to the PostgreSQL database via {@link DBConnectionPool}.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class UserDAOImpl implements UserDAO {

    /**
     * Implementation of the function to search the  user by his email address
     *
     * @param email the user's email
     * @return the user
     * @throws SQLException
     */
    public User findByEmail(String email) throws SQLException {

        if (email == null || email.isEmpty()) {
            throw new SQLException("Email is null or empty");
        }

        String query = "SELECT * FROM app_user WHERE email = ?;";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        User u = null;

        try {
            conn = DBConnectionPool.getInstance().getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            rs = stmt.executeQuery();

            if (rs.next()) {
                String id = rs.getString("user_id"),
                        name = rs.getString("first_name"),
                        surname = rs.getString("last_name"),
                        emailTmp = rs.getString("email"),
                        password = rs.getString("password_hash"),
                        dateOfBirth = rs.getString("date_of_birth"),
                        city = rs.getString("city"),
                        role = rs.getString("role");
                if (role.equalsIgnoreCase("cliente")) {
                    u = new Client(id, name, surname, emailTmp, password, city, dateOfBirth, true);
                } else if (role.equalsIgnoreCase("gestore")) {
                    u = new Manager(id, name, surname, emailTmp, password, city, dateOfBirth, true);
                }
                return u;
            } else {
                return null;
            }
        } finally {
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
        }
    }

    /**
     * Implementation of the function to search the  user by his id
     *
     * @param id the user's id
     * @return the user
     * @throws SQLException
     */
    public User findById(String id) throws SQLException {

        if (id == null || id.isEmpty()) {
            throw new SQLException("id is null or empty");
        }

        String query = "SELECT * FROM app_user WHERE id = ?;";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        User u = null;

        try {
            conn = DBConnectionPool.getInstance().getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setString(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                String idTmp = rs.getString("user_id"),
                        name = rs.getString("first_name"),
                        surname = rs.getString("last_name"),
                        email = rs.getString("email"),
                        password = rs.getString("password_hash"),
                        dateOfBirth = rs.getString("date_of_birth"),
                        city = rs.getString("city"),
                        role = rs.getString("role");
                if (role.equalsIgnoreCase("cliente")) {
                    u = new Client(idTmp, name, surname, email, password, city, dateOfBirth, true);
                } else if (role.equalsIgnoreCase("gestore")) {
                    u = new Manager(idTmp, name, surname, email, password, city, dateOfBirth, true);
                }
                return u;
            } else {
                return null;
            }
        } finally {
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
        }
    }

    /**
     * Implementation of the function to save a new user
     *
     * @param u the new user to save
     * @throws SQLException
     */
    public void save(User u) throws SQLException {

        if (u == null) {
            throw new SQLException("urser is null.");
        }

        String checkQuery = "SELECT count(*) AS recordCount FROM app_user WHERE user_id = ? OR email = ?";
        String query = "INSERT INTO app_user(user_id, first_name, last_name, email, password_hash, date_of_birth, city, role) VALUES(?,?,?,?,?,?,?,?);";
        Connection conn = null;
        PreparedStatement checkStmt = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (!(u instanceof Manager || u instanceof Client)) {
                throw new SQLException("User is not a Manager or a Client");
            }

            conn = DBConnectionPool.getInstance().getConnection();
            checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, u.getId());
            checkStmt.setString(2, u.getEmail());
            rs = checkStmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("recordCount");
                if (count > 0) {
                    throw new SQLException("User already exists.");
                }
            }


            stmt = conn.prepareStatement(query);

            stmt.setString(1, u.getId());
            stmt.setString(2, u.getName());
            stmt.setString(3, u.getSurname());
            stmt.setString(4, u.getEmail());
            stmt.setString(5, u.getPasswordHash());
            stmt.setString(6, u.getDateOfBirth());
            stmt.setString(7, u.getDomicile());
            stmt.setString(8, (u instanceof Client) ? "cliente" : "gestore");

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
     * Implementation of the function to update the user's data
     *
     * @param u the updated user
     */
    public void update(User u) throws SQLException {
        if (u == null) {
            throw new SQLException("urser is null.");
        }

        String query = "UPDATE app_user SET first_name = ?, last_name = ?, email = ?, password_hash = ?, date_of_birth = ?, city = ?, role = ? WHERE user_id = ?;";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            if (!(u instanceof Manager || u instanceof Client)) {
                throw new SQLException("User is not a Manager or a Client");
            }

            conn = DBConnectionPool.getInstance().getConnection();
            stmt = conn.prepareStatement(query);

            stmt.setString(1, u.getName());
            stmt.setString(2, u.getSurname());
            stmt.setString(3, u.getEmail());
            stmt.setString(4, u.getPasswordHash());
            stmt.setString(5, u.getDateOfBirth());
            stmt.setString(6, u.getDomicile());
            stmt.setString(7, u.getRole());
            stmt.setString(8, u.getId());
            stmt.executeUpdate();

        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                }
            }
        }
    }


}