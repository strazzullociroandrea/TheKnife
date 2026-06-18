package com.strazzullo_marocco_sibilla_marin.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton class DBConnectionPool to establish a db connection.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class DBConnectionPool {

    private static DBConnectionPool instance;
    private Connection connection;
    private String url, username, password;


    private DBConnectionPool(String url, String username, String password) throws SQLException {
        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException error) {
            throw new SQLException(error);
        }
    }

    public void initializeCredentials(String url, String username, String password) {
        if (url.isEmpty() || username.isEmpty() || password.isEmpty()) {
            throw new IllegalArgumentException("[initializeCredentials] Empty data");
        }
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public static synchronized DBConnectionPool getInstance() throws SQLException {
        if (instance == null || instance.connection.isClosed()) {
            throw new SQLException("Database connection is closed. Please restart the server.");
        }
        return instance;
    }

    public static synchronized DBConnectionPool getInstance(String url, String username, String password) throws SQLException {
        if (instance == null || instance.connection.isClosed()) {
            instance = new DBConnectionPool(url, username, password);
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }


}