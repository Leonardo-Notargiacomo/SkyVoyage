package io.github.fontysvenlo.ais.restapi;

import io.github.fontysvenlo.ais.businesslogic.api.TicketManager;
import io.github.fontysvenlo.ais.datarecords.TicketData;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TicketResource implements CrudHandler {
    private static final Logger logger = LoggerFactory.getLogger(TicketResource.class);
    private final TicketManager ticketManager;

    public TicketResource(TicketManager ticketManager) {
        this.ticketManager = ticketManager;
    }

    @Override
    public void getOne(Context ctx, String id) {
        try {
            Optional<List<TicketData>> tickets = ticketManager.getFromBooking(id);
            if (tickets.isPresent()) {
                ctx.json(tickets.get());
            } else {
                ctx.status(404).json(Map.of("error", "No tickets found for booking " + id));
            }
        } catch (Exception e) {
            logger.error("Error retrieving tickets for booking " + id, e);
            ctx.status(500).json(Map.of("error", "Internal server error"));
        }
    }

    // Required by CrudHandler but not used
    @Override public void getAll(Context ctx) { ctx.status(405); }
    @Override public void create(Context ctx) { ctx.status(405); }
    @Override public void update(Context ctx, String id) { ctx.status(405); }
    @Override public void delete(Context ctx, String id) { ctx.status(405); }
}
