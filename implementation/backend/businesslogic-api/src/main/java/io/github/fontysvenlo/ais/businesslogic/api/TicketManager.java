package io.github.fontysvenlo.ais.businesslogic.api;

import java.util.ArrayList;
import java.util.List;

import io.github.fontysvenlo.ais.datarecords.EmployeeData;
import io.github.fontysvenlo.ais.datarecords.TicketData;

public interface TicketManager {

    /**
     * List all tickets
     * @return a list of TicketData objects
     */
    public List<TicketData> list();
    
}
