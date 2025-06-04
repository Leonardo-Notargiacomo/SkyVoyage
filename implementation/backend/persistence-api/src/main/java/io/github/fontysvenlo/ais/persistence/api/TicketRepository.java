package io.github.fontysvenlo.ais.persistence.api;

import io.github.fontysvenlo.ais.datarecords.TicketData;
import java.util.List;
import java.util.Optional;

public interface TicketRepository {
    List<Integer> getTicketIDsFromBooking(int id);
    TicketData getTicketById(int id);
}
