package com.platform.analytics.tenant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Provides per-tenant database connections by setting the PostgreSQL
 * search_path to the tenant's schema. Uses a single shared HikariCP
 * connection pool — no per-tenant pools needed.
 *
 * Schema pattern: tenant_{slug} (e.g., tenant_acme)
 * Public schema entities use explicit @Table(schema="public") and are
 * always resolved correctly regardless of the search_path setting.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantConnectionProvider implements MultiTenantConnectionProvider<String> {

    private final DataSource dataSource;

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        Connection connection = dataSource.getConnection();
        try (Statement stmt = connection.createStatement()) {
            // Validate schema name to prevent injection (only alphanumeric + underscore)
            if (!tenantIdentifier.matches("[a-zA-Z0-9_]+")) {
                throw new IllegalArgumentException("Invalid tenant schema name: " + tenantIdentifier);
            }
            stmt.execute("SET search_path TO " + tenantIdentifier + ", public");
            log.debug("Connection search_path set to: {}", tenantIdentifier);
        } catch (SQLException e) {
            connection.close();
            throw e;
        }
        return connection;
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("SET search_path TO public");
        } finally {
            connection.close();
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        throw new UnsupportedOperationException("Cannot unwrap TenantConnectionProvider");
    }
}
