package io.github.fontysvenlo.ais.persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * This example test is testing the persistency layer including a test database
 * in a container.
 *
 * When you start setting up your database and persistence layer, this is a good
 * place to start. You will need to add some database setup and can prefill the
 * database with some data before a test.
 */
public class PersistenceIntegrationTest {

    private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
    private static DBConfig config;

    @BeforeAll
    static void beforeAll() {
        postgres.start();
        config = new DBConfig("test", postgres.getHost(), postgres.getFirstMappedPort(),
                "test", "ais", postgres.getUsername(), postgres.getPassword());

        // Initialize schema and tables for tests
        try (var connection = DBProvider.getDataSource(config).getConnection()) {
            try (var statement = connection.createStatement()) {
                // Create schema
                statement.execute("CREATE SCHEMA IF NOT EXISTS ais");

                // Create tables
                statement.execute("CREATE TABLE IF NOT EXISTS ais.customers ("
                        + "id SERIAL PRIMARY KEY, "
                        + "first_name VARCHAR(255) NOT NULL, "
                        + "last_name VARCHAR(255) NOT NULL, "
                        + "birth_date DATE NOT NULL)");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize test database", e);
        }
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @BeforeEach
    void setUp() {
    }
}
