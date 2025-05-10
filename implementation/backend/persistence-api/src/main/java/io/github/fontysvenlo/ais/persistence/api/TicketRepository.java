package io.github.fontysvenlo.ais.persistence.api;

import io.github.fontysvenlo.ais.datarecords.TicketData;

public interface TicketRepository {
    TicketData getTicketID(String id);

    TicketData getTicketData(String id);
    
}
