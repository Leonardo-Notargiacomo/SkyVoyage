package io.github.fontysvenlo.ais.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import io.github.fontysvenlo.ais.datarecords.FlightStats;
import io.github.fontysvenlo.ais.persistence.api.FlightStatsRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FlightStatsRepositoryImplTest {

    private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
    private static DBConfig config;
    private FlightStatsRepository flightStatsRepository;

    @BeforeAll
    static void beforeAll() {
        postgres.start();
        config = new DBConfig("test", postgres.getHost(), postgres.getFirstMappedPort(),
                "test", "public", postgres.getUsername(), postgres.getPassword());

        // Initialize tables for tests
        try (var connection = DBProvider.getDataSource(config).getConnection()) {
            try (var statement = connection.createStatement()) {
                // Create tables if they do not exist
                statement.execute("CREATE TABLE IF NOT EXISTS public.ticket (id SERIAL PRIMARY KEY, tariff DECIMAL(10,2))");
                statement.execute("CREATE TABLE IF NOT EXISTS public.flight (id SERIAL PRIMARY KEY, arrival_airport VARCHAR(10), duration INTEGER)");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error by initialize tables in DB", e);
        }
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @BeforeEach
    void setUp() {
        flightStatsRepository = new FlightStatsRepositoryImpl(config);

        // Clear tables before each test
        try (var connection = DBProvider.getDataSource(config).getConnection()) {
            try (var statement = connection.createStatement()) {
                statement.execute("DELETE FROM public.ticket");
                statement.execute("DELETE FROM public.flight");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error by reset the test database", e);
        }
    }

    @Test
    void testViewKPIDashboardWithData() throws SQLException {
        // Arrange: Insert test data into the database
        DataSource dataSource = DBProvider.getDataSource(config);
        try (Connection conn = dataSource.getConnection()) {
            // Insert tickets
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO public.ticket (tariff) VALUES (100), (200), (300)")) {
                ps.executeUpdate();
            }

            // Insert flights
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO public.flight (arrival_airport, duration) VALUES ('AMS', 2), ('AMS', 3), ('FRA', 1)")) {
                ps.executeUpdate();
            }
        }

        // Act
        FlightStats result = flightStatsRepository.getStatsForFlight();

        // Assert
        assertThat(result.totalRevenue()).isEqualTo(600.0);
        assertThat(result.topDestination()).isEqualTo("AMS");
        assertThat(result.totalKilometer()).isEqualTo(4800.0); // (2+3+1) * 800
    }

    @Test
    void testViewKPIDashboardNoData() {
        // Act
        FlightStats result = flightStatsRepository.getStatsForFlight();

        // Assert
        assertThat(result.totalRevenue()).isZero();
        assertThat(result.topDestination()).isEmpty();
        assertThat(result.totalKilometer()).isZero();
    }

    @Test
    void testViewKPIDashboardDataRetrievalError() throws SQLException {
        // Arrange: Mock the DataSource and Connection
        DBConfig mockConfig = mock(DBConfig.class);
        DataSource mockDataSource = mock(DataSource.class);
        when(mockDataSource.getConnection()).thenThrow(new SQLException("Datenbankfehler"));


        FlightStatsRepositoryImpl repository = new FlightStatsRepositoryImpl(mockConfig) {
            @Override
            public FlightStats getStatsForFlight() {
                throw new RuntimeException("Error fetching stats", new SQLException());
            }
        };

        // Act & Assert
        assertThatThrownBy(() -> repository.getStatsForFlight())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error fetching stats");
    }
}
