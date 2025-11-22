package repository;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Connection pool using HikariCP for efficient database access.
 * Replaces DatabaseConnection for production use.
 */
public class DatabaseConnectionPool {
    private static HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:oracle:thin:@vsgate-s1.dei.isep.ipp.pt:10796:XE");
        config.setUsername("C##LAPR3_G164");
        config.setPassword("oracle");

        // Pool configuration
        config.setMaximumPoolSize(10);           // Max 10 connections
        config.setMinimumIdle(2);                // Keep 2 idle
        config.setConnectionTimeout(30000);      // 30s timeout
        config.setIdleTimeout(600000);           // 10min idle
        config.setMaxLifetime(1800000);          // 30min max lifetime

        // Performance
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);

        System.out.println("✓ Connection pool initialized");
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("✓ Connection pool shutdown");
        }
    }
}