package io.github.fontysvenlo.ais.persistence;

import io.github.fontysvenlo.ais.persistence.api.FlightStatsRepository;
import io.github.fontysvenlo.ais.datarecords.FlightStats;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FlightStatsRepositoryImpl implements FlightStatsRepository {

    private final DataSource db;


    public FlightStatsRepositoryImpl(DBConfig config) {
        this.db = DBProvider.getDataSource(config);
    }

    @Override
    public FlightStats getStatsForFlight() {

        try (Connection conn = db.getConnection()) {
           double totalRevenue = 0;
           String topDestination = "";
           double totalKilometers = 0;

            try (var sqlStatement = conn.createStatement();
                 var result = sqlStatement.executeQuery("SELECT SUM(tariff) FROM public.ticket")) {
                if (result.next()) totalRevenue = result.getLong(1);
            }

            try (var sqlStatement = conn.createStatement();
                 var result = sqlStatement.executeQuery("SELECT arrival_airport, COUNT(*) as count FROM public.flight GROUP BY arrival_airport ORDER BY count DESC LIMIT 1")) {
                if (result.next()) topDestination = result.getString("arrival_airport");
            }

            try (var sqlStatement = conn.createStatement();
                 var result = sqlStatement.executeQuery("SELECT SUM(duration)* 800 FROM public.flight")) {
                if (result.next()) totalKilometers= result.getLong(1);
            }

            return new FlightStats(totalRevenue, topDestination, totalKilometers);
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching stats", e);
        }
    }
    }

