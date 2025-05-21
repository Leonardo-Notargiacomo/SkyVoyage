package io.github.fontysvenlo.ais.businesslogic;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.fontysvenlo.ais.businesslogic.api.TicketManager;
import io.github.fontysvenlo.ais.persistence.api.TicketRepository;
import io.github.fontysvenlo.ais.datarecords.TicketData;

public class TicketManagerImpl implements TicketManager {

    private static final Logger logger = Logger.getLogger(EmployeeManagerImpl.class.getName());
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
     * Retrieves ticket data from a single booking.
     * 
     * @param id the booking ID
     * @return a list of ticket data
     */
    @Override
    public List<TicketData> GetFromBooking(String id) {
        return ticketRepository.getAll(id);
    }

}
