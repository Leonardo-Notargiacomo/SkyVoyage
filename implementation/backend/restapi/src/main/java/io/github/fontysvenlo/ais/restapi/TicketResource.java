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

class TicketResource implements CrudHandler {
    private static final Logger logger = LoggerFactory.getLogger(TicketResource.class);
    private final TicketManager ticketManager;

    TicketResource(TicketManager ticketManager) {
        this.ticketManager = ticketManager;
    }

    @Override
    public void getOne(Context context, String id) {
        context.status(200);
        context.json(ticketManager.getTicketData(id));
        
    }

    /*
     * The following methods are required by the CrudHandler to exist and be
     * implemented, but since they are not used, they return a 405 Method Not
     * Allowed status.
     */
    @Override
    public void getAll(Context context) {context.status(405);}

    @Override
    public void create(Context context) {context.status(405);}

    @Override
    public void update(Context context, String id) {context.status(405);}

    @Override
    public void delete(Context context, String id) {context.status(405);}
}
