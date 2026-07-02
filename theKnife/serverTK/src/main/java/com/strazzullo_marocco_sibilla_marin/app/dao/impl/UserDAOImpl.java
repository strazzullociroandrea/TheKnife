package com.strazzullo_marocco_sibilla_marin.app.dao.impl;

import com.strazzullo_marocco_sibilla_marin.app.dao.UserDAO;
import com.strazzullo_marocco_sibilla_marin.app.DBConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import strazzullo.*;
import java.sql.Types;

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
        User u = null;

        try (Connection conn = DBConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String id = rs.getString("user_id"),
                            name = rs.getString("first_name"),
                            surname = rs.getString("last_name"),
                            emailTmp = rs.getString("email"),
                            password = rs.getString("password_hash"),
                            dateOfBirth = rs.getString("date_of_birth"),
                            city = rs.getString("city"),
                            role = rs.getString("role");
                    if (role.equalsIgnoreCase("customer")) {
                        u = new Client(id, name, surname, emailTmp, password, city, dateOfBirth);
                    } else if (role.equalsIgnoreCase("manager")) {
                        u = new Manager(id, name, surname, emailTmp, password, city, dateOfBirth);
                    }
                    return u;
                } else {
                    return null;
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

        String query = "SELECT * FROM app_user WHERE user_id = ?;";
        User u = null;

        try (Connection conn = DBConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String idTmp = rs.getString("user_id"),
                            name = rs.getString("first_name"),
                            surname = rs.getString("last_name"),
                            email = rs.getString("email"),
                            password = rs.getString("password_hash"),
                            dateOfBirth = rs.getString("date_of_birth"),
                            city = rs.getString("city"),
                            role = rs.getString("role");
                    if (role.equalsIgnoreCase("customer")) {
                        u = new Client(idTmp, name, surname, email, password, city, dateOfBirth);
                    } else if (role.equalsIgnoreCase("manager")) {
                        u = new Manager(idTmp, name, surname, email, password, city, dateOfBirth);
                    }
                    return u;
                } else {
                    return null;
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

        if (!(u instanceof Manager || u instanceof Client)) {
            throw new SQLException("User is not a Manager or a customer");
        }

        String checkQuery = "SELECT count(*) AS recordCount FROM app_user WHERE email = ?";
        String query = "INSERT INTO app_user(user_id, first_name, last_name, email, password_hash, date_of_birth, city, role) VALUES(?,?,?,?,?,?,?,?);";

        try (Connection conn = DBConnectionPool.getInstance().getConnection()) {
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, u.getEmail());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt("recordCount");
                        if (count > 0) {
                            throw new SQLException("User already exists.");
                        }
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, u.getId());
                stmt.setString(2, u.getName());
                stmt.setString(3, u.getSurname());
                stmt.setString(4, u.getEmail());
                stmt.setString(5, u.getPasswordHash());
                if (u.getDateOfBirth() == null || u.getDateOfBirth().isEmpty()) {
                    stmt.setNull(6, Types.DATE);
                } else {
                    stmt.setDate(6, Date.valueOf(u.getDateOfBirth()));
                }
                stmt.setString(7, u.getDomicile());
                stmt.setObject(8, u.getRole(), Types.OTHER);

                stmt.executeUpdate();
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

        if (!(u instanceof Manager || u instanceof Client)) {
            throw new SQLException("User is not a Manager or a Client");
        }

        String query = "UPDATE app_user SET first_name = ?, last_name = ?, email = ?, password_hash = ?, date_of_birth = ?, city = ?, role = ? WHERE user_id = ?;";

        try (Connection conn = DBConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, u.getName());
            stmt.setString(2, u.getSurname());
            stmt.setString(3, u.getEmail());
            stmt.setString(4, u.getPasswordHash());
            if (u.getDateOfBirth() == null || u.getDateOfBirth().isEmpty()) {
                stmt.setNull(5, Types.DATE);
            } else {
                stmt.setDate(5, Date.valueOf(u.getDateOfBirth()));
            }
            stmt.setString(6, u.getDomicile());
            stmt.setObject(7, u.getRole(), Types.OTHER);
            stmt.setString(8, u.getId());
            stmt.executeUpdate();
        }
    }


}
