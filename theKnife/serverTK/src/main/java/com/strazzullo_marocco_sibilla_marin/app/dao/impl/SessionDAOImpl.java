package com.strazzullo_marocco_sibilla_marin.app.dao.impl;

import com.strazzullo_marocco_sibilla_marin.app.DBConnectionPool;
import com.strazzullo_marocco_sibilla_marin.app.dao.SessionDAO;
import strazzullo.Client;
import strazzullo.Manager;
import strazzullo.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Concrete JDBC implementation of {@link SessionDAO}.
 *
 * @version 1.0
 * @Author Marocco Stefano, 762192, VA
 */
public class SessionDAOImpl implements SessionDAO {

    private static final int SESSION_DAYS = 30;

    /**
     * {@inheritDoc}
     * Sets {@code expires_at} to {@value #SESSION_DAYS} days from the current instant.
     */
    @Override
    public void save(String token, String userId) throws SQLException {
        String sql = "INSERT INTO session (token, user_id, expires_at) VALUES (?, ?, ?)";
        Timestamp expiresAt = Timestamp.from(Instant.now().plus(SESSION_DAYS, ChronoUnit.DAYS));
        try (Connection conn = DBConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            stmt.setString(2, userId);
            stmt.setTimestamp(3, expiresAt);
            stmt.executeUpdate();
        }
    }

    /**
     * {@inheritDoc}
     * JOINs {@code session} with {@code app_user} and rejects rows whose {@code expires_at} is in
     * the past. Returns a {@link strazzullo.Client} or {@link strazzullo.Manager} depending on the
     * {@code role} column.
     */
    @Override
    public User findUserByToken(String token) throws SQLException {
        String sql = """
                SELECT u.user_id, u.first_name, u.last_name, u.email, u.password_hash,
                       u.date_of_birth, u.city, u.role
                FROM session s
                JOIN app_user u ON u.user_id = s.user_id
                WHERE s.token = ? AND s.expires_at > NOW()
                """;
        try (Connection conn = DBConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;
                String id          = rs.getString("user_id");
                String name        = rs.getString("first_name");
                String surname     = rs.getString("last_name");
                String email       = rs.getString("email");
                String password    = rs.getString("password_hash");
                String dateOfBirth = rs.getString("date_of_birth");
                String city        = rs.getString("city");
                String role        = rs.getString("role");
                if ("manager".equalsIgnoreCase(role)) {
                    return new Manager(id, name, surname, email, password, city, dateOfBirth);
                } else {
                    return new Client(id, name, surname, email, password, city, dateOfBirth);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(String token) throws SQLException {
        String sql = "DELETE FROM session WHERE token = ?";
        try (Connection conn = DBConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            stmt.executeUpdate();
        }
    }
}
