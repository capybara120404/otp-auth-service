package otp.auth.service.infrastructure.persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import otp.auth.service.application.interfaces.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseContext {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseContext.class);
    private final HikariDataSource dataSource;

    public DatabaseContext(Configuration configuration) {
        ensureDatabaseExists(configuration);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(configuration.getDatabaseUrl());
        config.setUsername(configuration.getDbUser());
        config.setPassword(configuration.getDbPassword());

        this.dataSource = new HikariDataSource(config);
        initializeSchema();
    }

    private void ensureDatabaseExists(Configuration config) {
        String url = config.getDatabaseUrl();
        String dbName = url.substring(url.lastIndexOf("/") + 1);
        String masterUrl = url.substring(0, url.lastIndexOf("/") + 1) + "postgres";

        java.util.Properties props = new java.util.Properties();
        props.setProperty("user", config.getDbUser());
        props.setProperty("password", config.getDbPassword());
        props.setProperty("ssl", "false");

        try (Connection conn = DriverManager.getConnection(masterUrl, props);
                Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("CREATE DATABASE " + dbName);
            logger.info("Database {} created successfully.", dbName);
        } catch (SQLException e) {
            if (!"42P04".equals(e.getSQLState())) {
                throw new RuntimeException("Failed to create database", e);
            }
        }
    }

    private void initializeSchema() {
        String schemaSql = """
                CREATE TABLE IF NOT EXISTS users (
                    id SERIAL PRIMARY KEY,
                    username VARCHAR(255) UNIQUE NOT NULL,
                    password_hash VARCHAR(255) NOT NULL,
                    role VARCHAR(50) NOT NULL
                );
                CREATE TABLE IF NOT EXISTS otp_configuration (
                    id INTEGER PRIMARY KEY DEFAULT 1,
                    life_time_seconds INTEGER NOT NULL DEFAULT 300,
                    code_length INTEGER NOT NULL DEFAULT 6,
                    CONSTRAINT single_row CHECK (id = 1)
                );
                CREATE TABLE IF NOT EXISTS otp_codes (
                    id SERIAL PRIMARY KEY,
                    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
                    code VARCHAR(10) NOT NULL,
                    status VARCHAR(20) NOT NULL,
                    expires_at TIMESTAMP NOT NULL
                );
                INSERT INTO otp_configuration (id, life_time_seconds, code_length)
                VALUES (1, 300, 6) ON CONFLICT (id) DO NOTHING;
                """;

        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(schemaSql);
            logger.info("Database schema initialized successfully.");
        } catch (SQLException exception) {
            logger.error("Failed to initialize database schema", exception);
            throw new RuntimeException("Database initialization failed", exception);
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
