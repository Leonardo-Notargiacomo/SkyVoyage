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

            try (PreparedStatement ps = conn.prepareStatement("SELECT SUM(tariff) FROM public.ticket");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) totalRevenue = rs.getLong(1);
            }

            try (PreparedStatement ps = conn.prepareStatement("SELECT arrival_airport, COUNT(*) as count FROM public.flight GROUP BY arrival_airport ORDER BY count DESC LIMIT 1");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) topDestination = rs.getString("arrival_airport");
            }

            try (PreparedStatement ps = conn.prepareStatement("SELECT SUM(duration)* 800 FROM public.flight");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) totalKilometers= rs.getLong(1);
            }

            return new FlightStats(totalRevenue, topDestination, totalKilometers);
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching stats", e);
        }
    }
    }

