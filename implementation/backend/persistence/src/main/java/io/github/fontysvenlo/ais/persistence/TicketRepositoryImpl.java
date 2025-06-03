package io.github.fontysvenlo.ais.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import io.github.fontysvenlo.ais.datarecords.TicketData;
import io.github.fontysvenlo.ais.persistence.api.TicketRepository;

public class TicketRepositoryImpl implements TicketRepository {

    private static final Logger LOGGER = Logger.getLogger(TicketRepositoryImpl.class.getName());
    private final DataSource db;

    public TicketRepositoryImpl(DBConfig config) {
        this.db = DBProvider.getDataSource(config);
    }

    /**
     * Retrieves ticket IDs from a single booking.
     *
     * @param id the booking ID
     * @return a list of ticket IDs
     */
    @Override
    public List<Integer> getTicketIDsFromBooking(int id) {
        int bookingId = id;
        List<Integer> ticketIDs = new ArrayList<>();
        // Query setup
        String query = " SELECT t.ID " +
                " FROM Ticket t " +
                " WHERE t.FlightID IN ( " +
                " SELECT bf.FlightID " +
                " FROM Booking_Flight bf " +
                " WHERE bf.BookingID = ? )";
        // Query performed
        try (PreparedStatement statement = db.getConnection().prepareStatement(query)) {
            statement.setInt(1, bookingId);
            ResultSet rs = statement.executeQuery();
            // Adding IDs to a list
            while (rs.next()) {
                ticketIDs.add(rs.getInt("t.ID"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving ticket IDs: ", e);
        }
        // Return the list with the IDs
        return ticketIDs;
    }

    /**
     * Retrieves a ticket by its ID.
     * 
     * The method first pulls the data from the flight
     * After that it pulls the passenger data
     * Finally, it adds the data to the record TicketData and returns it
     *
     * @param id the ticket ID
     * @return the TicketData object if found, otherwise null
     */
    @Override
    public TicketData getTicketById(Integer id) {
        int ticketId = id;

        // The flight data first
        List<String> flightData = new ArrayList<>();
        // Query for getting the flightData
        String flightQuery = " SELECT f.id, " +
                " f.departure_airport, " +
                " f.departure_terminal, " +
                " f.departure_gate, " +
                " f.departure_scheduled_time, " +
                " f.arrival_airport," +
                " f.arrival_scheduled_time " +
                " FROM flight f " +
                " WHERE f.id in ( " +
                " SELECT t.FlightID " +
                " FROM ticket t " +
                " WHERE t.id = ? ) ";
        // Query performed
        try (PreparedStatement statement = db.getConnection().prepareStatement(flightQuery)) {
            statement.setInt(1, ticketId);
            ResultSet rs = statement.executeQuery();
            // Adding the flight data to a list
            if (rs.next()) {
                flightData.add(rs.getString("f.id"));
                flightData.add(rs.getString("f.departure_airport"));
                flightData.add(rs.getString("f.departure_terminal"));
                flightData.add(rs.getString("f.departure_gate"));
                flightData.add(rs.getString("f.departure_scheduled_time"));
                flightData.add(rs.getString("f.arrival_airport"));
                flightData.add(rs.getString("f.arrival_scheduled_time"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving flight data: ", e);
        }

        // Next the passenger data
        String customerFirstname = "";
        String customerLastname = "";
        boolean hasSeat = true;
        // Query for getting the passenger data
        String customerQuery = " SELECT c.firstname, " +
                " c.lastname, " +
                " c.isinfant " +
                " FROM customer c " +
                " WHERE c.id IN ( " +
                " SELECT t.CustomerID " +
                " FROM ticket t " +
                " WHERE t.id = ? ) ";
        // Query performed
        try (PreparedStatement statement = db.getConnection().prepareStatement(customerQuery)) {
            statement.setInt(1, ticketId);
            ResultSet rs = statement.executeQuery();
            // Adding the passenger data to variables
            if (rs.next()) {
                customerFirstname = rs.getString("c.firstname");
                customerLastname = rs.getString("c.lastname");
                hasSeat = !rs.getBoolean("c.isinfant");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving customer data: ", e);
        }

        // Finally creating the new TicketData and returning it
        return new TicketData(
                flightData.get(0),
                flightData.get(1),
                flightData.get(2),
                flightData.get(3),
                flightData.get(4),
                flightData.get(5),
                flightData.get(6),
                customerFirstname,
                customerLastname,
                hasSeat);
    }
}
