package io.github.fontysvenlo.ais.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import io.github.fontysvenlo.ais.datarecords.TicketData;
import io.github.fontysvenlo.ais.persistence.api.TicketRepository;

public class TicketRepositoryImpl implements TicketRepository {

    private static final Logger LOGGER = Logger.getLogger(EmployeeRepositoryImpl.class.getName());
    private final DataSource db;

    public TicketRepositoryImpl(DBConfig config) {
        this.db = DBProvider.getDataSource(config);
    }

    @Override
    public List<TicketData> getAll(String id) {
        int bookingId = Integer.parseInt(id);
        List<TicketData> result = new ArrayList<>();

        try (Connection connection = db.getConnection();
                PreparedStatement stmt = connection.prepareStatement(
                        "SELECT  f.ID AS flightNumber, f.Departure_Airport AS departureAirport, f.Departure_Terminal AS departureTerminal, f.Departure_Gate AS departureGate, TO_CHAR(f.Departure_Scheduled_Time, 'YYYY-MM-DD HH24:MI:SS') AS departureTime, f.Departure_Delay AS departureDelay, f.Arrival_Airport AS arrivalAirport, f.Arrival_Terminal AS arrivalTerminal, f.Arrival_Gate AS arrivalGate, TO_CHAR(f.Arrival_Scheduled_Time, 'YYYY-MM-DD HH24:MI:SS') AS arrivalTime, f.Arrival_Delay AS arrivalDelay, CASE  WHEN c.IsInfant = TRUE THEN FALSE ELSE TRUE END AS hasSeat, c.Firstname AS firstName, c.Lastname AS lastName, c.Email AS email, c.PhoneNumber AS phoneNumber, a.Country AS country, a.City AS city, a.Street AS streetName, a.HouseNumber AS houseNumber "
                                + "FROM Booking_Flight bf JOIN Flight f ON bf.FlightID = f.ID JOIN Ticket t ON t.FlightID = f.ID JOIN Customer c ON t.CustomerID = c.ID JOIN Address a ON c.AddressID = a.ID WHERE bf.BookingID = ?")) {
            stmt.setInt(1, bookingId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new TicketData(
                            rs.getString("flightNumber"),
                            rs.getString("departureAirport"),
                            rs.getString("departureTerminal"),
                            rs.getString("departureGate"),
                            rs.getString("departureTime"),
                            rs.getInt("departureDelay"),
                            rs.getString("arrivalAirport"),
                            rs.getString("arrivalTerminal"),
                            rs.getString("arrivalGate"),
                            rs.getString("arrivalTime"),
                            rs.getInt("arrivalDelay"),
                            rs.getBoolean("hasSeat"),
                            rs.getString("firstName"),
                            rs.getString("lastName"),
                            rs.getString("email"),
                            rs.getString("phoneNumber"),
                            rs.getString("country"),
                            rs.getString("city"),
                            rs.getString("streetName"),
                            rs.getString("houseNumber")));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving tickets: " + e.getMessage(), e);
        }

        return result;
    }
}
