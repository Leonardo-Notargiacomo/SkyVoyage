package io.github.fontysvenlo.ais.persistence;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import io.github.fontysvenlo.ais.datarecords.TicketData;
import io.github.fontysvenlo.ais.persistence.api.TicketRepository;

public class TicketRepositoryImpl implements TicketRepository {

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
        String query = " SELECT t.id " +
                " FROM public.Ticket t " +
                " WHERE t.flightid IN ( " +
                " SELECT bf.flightid " +
                " FROM public.Booking_Flight bf " +
                " WHERE bf.bookingid = ? )";
        // Query performed
        try (PreparedStatement statement = db.getConnection().prepareStatement(query)) {
            statement.setInt(1, bookingId);
            ResultSet resultSet = statement.executeQuery();
            // Adding IDs to a list
            while (resultSet.next()) {
                ticketIDs.add(resultSet.getInt("id"));
            }
        } catch (SQLException e) {
            return ticketIDs; // Return empty list if an error occurs
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
    public TicketData getTicketById(int id) {
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
                " FROM public.flight f " +
                " WHERE f.id in ( " +
                " SELECT t.FlightID " +
                " FROM public.ticket t " +
                " WHERE t.id = ? ) ";
        // Query performed
        try (PreparedStatement statement = db.getConnection().prepareStatement(flightQuery)) {
            statement.setInt(1, ticketId);
            ResultSet resultSet = statement.executeQuery();
            // Adding the flight data to a list
            if (resultSet.next()) {
                flightData.add(resultSet.getString("id"));
                flightData.add(resultSet.getString("departure_airport"));
                flightData.add(resultSet.getString("departure_terminal"));
                flightData.add(resultSet.getString("departure_gate"));
                flightData.add(resultSet.getString("departure_scheduled_time"));
                flightData.add(resultSet.getString("arrival_airport"));
                flightData.add(resultSet.getString("arrival_scheduled_time"));
            }
        } catch (SQLException e) {
            return null; // Return null if an error occurs
        }

        // Next the passenger data
        String customerFirstname = "";
        String customerLastname = "";
        boolean hasSeat = true;
        // Query for getting the passenger data
        String customerQuery = " SELECT c.firstname, " +
                " c.lastname, " +
                " c.isinfant " +
                " FROM public.customer c " +
                " WHERE c.id IN ( " +
                " SELECT t.CustomerID " +
                " FROM public.ticket t " +
                " WHERE t.id = ? ) ";
        // Query performed
        try (PreparedStatement statement = db.getConnection().prepareStatement(customerQuery)) {
            statement.setInt(1, ticketId);
            ResultSet resultSet = statement.executeQuery();
            // Adding the passenger data to variables
            if (resultSet.next()) {
                customerFirstname = resultSet.getString("firstname");
                customerLastname = resultSet.getString("lastname");
                hasSeat = !resultSet.getBoolean("isinfant");
            }
        } catch (SQLException e) {
            return null; // Return null if an error occurs
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
