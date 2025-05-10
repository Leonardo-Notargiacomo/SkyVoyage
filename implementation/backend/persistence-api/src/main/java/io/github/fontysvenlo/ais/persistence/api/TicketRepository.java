package io.github.fontysvenlo.ais.persistence.api;

import java.util.List;

import io.github.fontysvenlo.ais.datarecords.TicketData;

public interface TicketRepository {
    List<String> getTicketID(String id);

    TicketData getTicketData(String id);
    
}
