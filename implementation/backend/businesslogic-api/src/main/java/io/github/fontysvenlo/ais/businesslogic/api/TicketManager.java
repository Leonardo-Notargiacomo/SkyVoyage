package io.github.fontysvenlo.ais.businesslogic.api;

import java.util.List;
import java.util.Optional;

import io.github.fontysvenlo.ais.datarecords.TicketData;

public interface TicketManager {

    /**
     * List all tickets for a specific booking
     * 
     * @param id the booking ID
     * @return a list of TicketData objects
     */
    public Optional<List<TicketData>> getFromBooking(String id);
    
}
