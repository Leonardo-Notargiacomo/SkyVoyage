package io.github.fontysvenlo.ais.businesslogic.api;

import java.util.ArrayList;

import io.github.fontysvenlo.ais.datarecords.TicketData;

public interface TicketManager {

    /**
     * Retrieves ticket data as a list of lists. Each inner list has 19 indexes,
     * which can contain empty strings or null values.
     *
     * @param id the identifier for the ticket data
     * @return an ArrayList of ArrayLists, each with 19 indexes
     */
    public ArrayList<ArrayList<String>> getTicketData(String id);
    
}
