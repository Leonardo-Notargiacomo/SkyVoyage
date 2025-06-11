package io.github.fontysvenlo.ais.restapi;

import io.github.fontysvenlo.ais.businesslogic.api.TicketManager;
import io.github.fontysvenlo.ais.datarecords.TicketData;
import io.javalin.http.Context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TicketResourceTest {
    private final Context context = mock(Context.class);
    private TicketManager ticketManager;
    private TicketResource ticketResource;

    @BeforeEach
    public void setUp() {
        ticketManager = mock(TicketManager.class);
        ticketResource = new TicketResource(ticketManager);
    }

    @Test
    public void testGetTicketData() {
        // Given there is a booking in the system with at least one ticket
        String bookingId = "2";
        TicketData ticketData = new TicketData("FFL002", "Berlin Tegel", "A", "3", "2025-05-12 14:00:00", "JFK International", "2025-05-12 18:30:00", "Jane", "Smith", true);
        when(ticketManager.getTicketData(bookingId)).thenReturn(List.of(ticketData));

        // When we call the getOne function with the booking ID
        ticketResource.getOne(context, bookingId);

        // Then we should get the context with a status 200 and the ticket data
        verify(context).status(200);
        verify(context).json(List.of(ticketData));
    }

    @Test
    public void testGetTicketDataWithNoTickets() {
        // Given there is no booking in the system with ID 3
        String bookingId = "3";
        when(ticketManager.getTicketData(bookingId)).thenReturn(List.of());

        // When we call the getOne function with the booking ID
        ticketResource.getOne(context, bookingId);

        // Then we should get the context with a status 200 but no ticket data
        verify(context).status(200);
        verify(context).json(List.of());
    }
}
