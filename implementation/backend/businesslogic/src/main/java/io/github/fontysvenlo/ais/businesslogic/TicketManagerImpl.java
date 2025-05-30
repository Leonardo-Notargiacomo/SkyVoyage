package io.github.fontysvenlo.ais.businesslogic;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.fontysvenlo.ais.businesslogic.api.TicketManager;
import io.github.fontysvenlo.ais.persistence.api.TicketRepository;
import io.github.fontysvenlo.ais.datarecords.TicketData;

public class TicketManagerImpl implements TicketManager {

    private static final Logger logger = Logger.getLogger(TicketManagerImpl.class.getName());
    private final TicketRepository ticketRepository;

    /**
     * Constructor
     *
     * @param ticketRepository the ticket storage service
     */
    public TicketManagerImpl(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
        logger.log(Level.INFO, "TicketManagerImpl initialized");
    }

    /**
     * Retrieves ticket IDs from a single booking.
     *
     * @param id the booking ID
     * @return a list of ticket IDs
     */
    @Override
    public List<TicketData> getTicketData(int id) {
        int bookingId = id;
        List<Integer> ticketIDs = new ArrayList<>();
        List<TicketData> ticketDataList = new ArrayList<>();
        TicketData ticketData;
        try {
            ticketIDs = ticketRepository.getTicketIDsFromBooking(bookingId);
            if (ticketIDs.size() == 0) {
                logger.log(Level.WARNING, "No tickets found for booking ID: " + bookingId);
                return ticketDataList; // Return empty list if no tickets found
            }
            for (Integer ticketID : ticketIDs) {
                try {
                    ticketData = ticketRepository.getTicketById(ticketID);
                    ticketDataList.add(ticketData);
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return ticketDataList;
    }
}
