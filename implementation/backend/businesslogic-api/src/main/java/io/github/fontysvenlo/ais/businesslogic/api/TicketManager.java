package io.github.fontysvenlo.ais.businesslogic.api;

import java.util.ArrayList;
import java.util.List;

import io.github.fontysvenlo.ais.datarecords.EmployeeData;
import io.github.fontysvenlo.ais.datarecords.TicketData;

public interface TicketManager {

    /**
     * Retrieves ticket data as a list of lists. Each inner list has 20 indexes,
     * which can contain empty strings or null values.
     *
     * @param id the identifier for the ticket data
     * @return an ArrayList of ArrayLists, each with 20 indexes
     */
    public List<TicketData> list();
    
}
