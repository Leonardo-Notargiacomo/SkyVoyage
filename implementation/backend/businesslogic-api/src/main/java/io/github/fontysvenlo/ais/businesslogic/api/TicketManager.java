package io.github.fontysvenlo.ais.businesslogic.api;

import java.util.ArrayList;
import java.util.List;

import io.github.fontysvenlo.ais.datarecords.TicketData;

public interface TicketManager {

    /**
     * List all tickets for a specific booking
     * 
     * @param id the booking ID
     * @return a list of TicketData objects
     */
    public List<TicketData> GetFromBooking(String id);
    
}
