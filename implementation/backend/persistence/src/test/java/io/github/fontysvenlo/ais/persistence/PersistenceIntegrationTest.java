package io.github.fontysvenlo.ais.persistence;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import io.github.fontysvenlo.ais.datarecords.CustomerData;
import io.github.fontysvenlo.ais.persistence.api.CustomerRepository;
import io.github.fontysvenlo.ais.persistence.api.Persistence;

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

    @Test
    void testAddingAndRetrievingCustomers() {
        // Arrange
        Persistence persistenceAPI = PersistenceFactory.getInstance(config);
        CustomerRepository CustomerRepository = persistenceAPI.getCustomerRepository();

        // Act
        CustomerRepository.add(new CustomerData(0, "John", "Doe", LocalDate.of(2025, 1, 1)));
        List<CustomerData> customers = CustomerRepository.getAll();

        // Assert
        // Note: this assumes that the database was empty before the test.
        // The intention of the test is to make sure that if we store a customer, we can retrieve it.
        assertThat(customers).hasSize(1);
        assertThat(customers.get(0).firstName()).isEqualTo("John");
    }
}
