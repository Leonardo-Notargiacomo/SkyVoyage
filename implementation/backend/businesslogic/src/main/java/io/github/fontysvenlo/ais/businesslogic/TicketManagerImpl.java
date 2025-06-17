package io.github.fontysvenlo.ais.businesslogic;

import java.util.ArrayList;
import java.util.List;

import io.github.fontysvenlo.ais.businesslogic.api.TicketManager;
import io.github.fontysvenlo.ais.persistence.api.TicketRepository;
import io.github.fontysvenlo.ais.datarecords.TicketData;

public class TicketManagerImpl implements TicketManager {

    private final TicketRepository ticketRepository;

    /**
     * Constructor
     *
     * @param ticketRepository the ticket storage service
     */
    public TicketManagerImpl(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    /**
     * Retrieves ticket IDs from a single booking.
     *
     * @param id the booking ID
     * @return a list of ticket IDs
     */
    @Override
    public List<TicketData> getTicketData(String id) {
        int bookingId = Integer.parseInt(id);
        List<Integer> ticketIDs = new ArrayList<>();
        List<TicketData> ticketDataList = new ArrayList<>();
        TicketData ticketData;
        // Get ticket IDs
        try {
            ticketIDs = ticketRepository.getTicketIDsFromBooking(bookingId);
            if (ticketIDs.size() == 0) {
                return ticketDataList; // Return empty list if no tickets found
            }
        } catch (Exception e) {
            return ticketDataList; // Return empty list if an error decides to exist
        }
        // Get ticket data
        for (int ticketID : ticketIDs) {
            try {
                ticketData = ticketRepository.getTicketById(ticketID);
                ticketDataList.add(ticketData);
            } catch (Exception e) {
                return ticketDataList; // Return empty list if an error shows up
            }
        }
        // Return the list
        return ticketDataList;
    }
}
