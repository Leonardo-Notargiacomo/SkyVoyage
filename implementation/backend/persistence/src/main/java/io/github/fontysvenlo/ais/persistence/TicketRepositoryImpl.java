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
    public List<Integer> getTicketIDsFromBooking(String id) {
        String bookingId = id;
        List<Integer> ticketIDs = new ArrayList<>();
        String query = " SELECT t.ID " +
                       " FROM Ticket t " +
                       " WHERE t.FlightID IN ( " +
                            " SELECT bf.FlightID " +
                            " FROM Booking_Flight bf " +
                            " WHERE bf.BookingID = ? )";

        try (PreparedStatement statement = db.getConnection().prepareStatement(query)) {
            statement.setString(1, bookingId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                ticketIDs.add(rs.getInt("t.ID"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving ticket IDs: ", e);
        }
        return ticketIDs;
    }

    /**
     * Retrieves a ticket by its ID.
     *
     * @param id the ticket ID
     * @return the TicketData object if found, otherwise null
     */
    @Override
    public TicketData getTicketById(Integer id) {


        return null;
    }
}
