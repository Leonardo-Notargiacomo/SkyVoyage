package io.github.fontysvenlo.ais.businesslogic.api;

import java.util.List;
import java.util.Optional;

import io.github.fontysvenlo.ais.datarecords.TicketData;

public interface TicketManager {

    /**
     * List ticket IDs for a specific booking
     * 
     * @param id the booking ID
     * @return a list of ticket IDs
     */
    public List<TicketData> getTicketData(int id);

}
