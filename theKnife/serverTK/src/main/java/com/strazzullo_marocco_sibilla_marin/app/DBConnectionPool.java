package com.strazzullo_marocco_sibilla_marin.app;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Singleton class DBConnectionPool to provide pooled JDBC connections to PostgreSQL.
 * Backed by a {@link HikariDataSource} so concurrent RMI calls each get their own connection
 * instead of contending on a single shared one. Every {@link #getConnection()} call must be
 * paired with closing the returned {@link Connection} (e.g. via try-with-resources) to return
 * it to the pool.
 *
 * @version 2.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA - author of this revision
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class DBConnectionPool {

    private static DBConnectionPool instance;
    private final HikariDataSource dataSource;

    private DBConnectionPool(String url, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setPoolName("TheKnifeDBPool");
        config.setConnectionTimeout(30_000);
        config.setIdleTimeout(600_000);
        config.setMaxLifetime(1_800_000);
        config.setKeepaliveTime(60_000);
        config.setConnectionTestQuery("SELECT 1");
        this.dataSource = new HikariDataSource(config);
    }

    /**
     * Returns the existing pool instance.
     *
     * @return the singleton pool instance
     * @throws SQLException if the pool was never initialized or has since been closed
     */
    public static synchronized DBConnectionPool getInstance() throws SQLException {
        if (instance == null || instance.dataSource.isClosed()) {
            throw new SQLException("Database connection pool is not initialized. Please restart the server.");
        }
        return instance;
    }

    /**
     * Initializes the pool on first call with the given credentials; subsequent calls return the
     * already-initialized pool, ignoring the credentials passed in.
     *
     * @param url the JDBC connection URL
     * @param username the database username
     * @param password the database password
     * @return the singleton pool instance
     */
    public static synchronized DBConnectionPool getInstance(String url, String username, String password) {
        if (instance == null || instance.dataSource.isClosed()) {
            instance = new DBConnectionPool(url, username, password);
        }
        return instance;
    }

    /**
     * Borrows a connection from the pool. Callers must close it (e.g. via try-with-resources)
     * to return it to the pool rather than leaking it.
     *
     * @return a pooled JDBC connection
     * @throws SQLException if a connection could not be obtained from the pool
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Shuts the pool down, closing all underlying connections. After this call,
     * {@link #getInstance()} will throw until {@link #getInstance(String, String, String)} re-initializes it.
     */
    public static synchronized void shutdown() {
        if (instance != null) {
            instance.dataSource.close();
            instance = null;
        }
    }
}
