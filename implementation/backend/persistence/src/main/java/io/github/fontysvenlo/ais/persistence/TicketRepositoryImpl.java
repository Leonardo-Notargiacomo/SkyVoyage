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

    /*
     * Gives back all the ticket IDs from the bookingID that has been provided
     */
    @Override
    public List<String> getTicketID(String id) {
        List<String> ticketIDs = new ArrayList<>();
        try (Connection connection = db.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT t.ID AS ticketID\r\n" + //
                        "        FROM Ticket t\r\n" + //
                        "        JOIN Flight f ON t.FlightID = f.ID\r\n" + //
                        "        JOIN Booking_Flight bf ON bf.FlightID = f.ID\r\n" + //
                        "        WHERE bf.BookingID = ?")) {
            statement.setString(1, id);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                ticketIDs.add(resultSet.getString("ticketID"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving ticket IDs", e);
        }

        return ticketIDs;
    }

    /*
     * Gives back all the ticket data from the ticket
     */
    @Override
    public TicketData getTicketData(String id) {
        try (Connection connection = db.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT f.ID AS flightNumber,\r\n" + //
                        "        f.Departure_Airport,\r\n" + //
                        "        f.Departure_Terminal,\r\n" + //
                        "        f.Departure_Gate,\r\n" + //
                        "        f.Departure_Scheduled_Time,\r\n" + //
                        "        f.Departure_Delay,\r\n" + //
                        "        f.Arrival_Airport,\r\n" + //
                        "        f.Arrival_Terminal,\r\n" + //
                        "        f.Arrival_Gate,\r\n" + //
                        "        f.Arrival_Scheduled_Time,\r\n" + //
                        "        f.Arrival_Delay,\r\n" + //
                        "        c.Firstname,\r\n" + //
                        "        c.Lastname,\r\n" + //
                        "        c.Email,\r\n" + //
                        "        c.PhoneNumber,\r\n" + //
                        "        c.IsInfant,\r\n" + //
                        "        a.Country,\r\n" + //
                        "        a.City,\r\n" + //
                        "        a.Street,\r\n" + //
                        "        a.HouseNumber\r\n" + //
                        "FROM Ticket t\r\n" + //
                        "JOIN Flight f ON t.FlightID = f.ID\r\n" + //
                        "JOIN Customer c ON t.CustomerID = c.ID\r\n" + //
                        "JOIN Address a ON c.AddressID = a.ID\r\n" + //
                        "WHERE t.ID = ?")) {

            statement.setString(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                boolean isInfant = resultSet.getBoolean("IsInfant");
                boolean hasSeat = !isInfant; // no seat for infants

                return new TicketData(
                        resultSet.getString("flightNumber"),
                        resultSet.getString("Departure_Airport"),
                        resultSet.getString("Departure_Terminal"),
                        resultSet.getString("Departure_Gate"),
                        resultSet.getString("Departure_Scheduled_Time"),
                        resultSet.getInt("Departure_Delay"),
                        resultSet.getString("Arrival_Airport"),
                        resultSet.getString("Arrival_Terminal"),
                        resultSet.getString("Arrival_Gate"),
                        resultSet.getString("Arrival_Scheduled_Time"),
                        resultSet.getInt("Arrival_Delay"),
                        hasSeat,
                        resultSet.getString("Firstname"),
                        resultSet.getString("Lastname"),
                        resultSet.getString("Email"),
                        resultSet.getString("PhoneNumber"),
                        resultSet.getString("Country"),
                        resultSet.getString("City"),
                        resultSet.getString("Street"),
                        resultSet.getString("HouseNumber"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving ticket data", e);
        }

        return null;
    }

}
