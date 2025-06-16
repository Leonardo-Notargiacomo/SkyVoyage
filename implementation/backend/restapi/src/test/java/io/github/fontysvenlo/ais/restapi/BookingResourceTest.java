package io.github.fontysvenlo.ais.restapi;


import io.github.fontysvenlo.ais.businesslogic.api.BookingManager;
import io.github.fontysvenlo.ais.datarecords.BookingData;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

public class BookingResourceTest {
    private BookingManager bookingManager;
    private BookingResource bookingResource;
    private Context ctx;

    @BeforeEach
    void setUp() {
        bookingManager = mock(BookingManager.class);
        bookingResource = new BookingResource(bookingManager);
        ctx = mock(Context.class);

        when(ctx.status(any())).thenReturn(ctx);
    }

    @Test
    void testListBookings() {
        List<BookingData> bookings = List.of(mock(BookingData.class));
        when(bookingManager.list()).thenReturn(bookings);

        bookingResource.list(ctx);

        verify(ctx).json(bookings);
    }

    @Test
    void testGetOneBookingFound() {
        BookingData booking = mock(BookingData.class);
        when(ctx.pathParam("id")).thenReturn("1");
        when(bookingManager.getOne(1)).thenReturn(booking);

        bookingResource.getOne(ctx);

        verify(ctx).json(booking);
    }

    @Test
    void testGetOneBookingNotFound() {
        when(ctx.pathParam("id")).thenReturn("99");
        when(bookingManager.getOne(99)).thenReturn(null);

        bookingResource.getOne(ctx);

        verify(ctx).status(HttpStatus.NOT_FOUND);
        verify(ctx).json(Map.of("error", "Booking not found"));
    }

    @Test
    void testGetOneBookingInvalidId() {
        when(ctx.pathParam("id")).thenReturn("abc");

        bookingResource.getOne(ctx);

        verify(ctx).status(HttpStatus.BAD_REQUEST);
        verify(ctx).json(Map.of("error", "Invalid booking ID format"));
    }

    @Test
    void testGetByCustomerId() {
        List<BookingData> bookings = List.of(mock(BookingData.class));
        when(ctx.pathParam("customerId")).thenReturn("5");
        when(bookingManager.getByCustomerId(5)).thenReturn(bookings);

        bookingResource.getByCustomerId(ctx);

        verify(ctx).json(bookings);
    }

    @Test
    void testCheckCustomerEmailExists() {
        when(ctx.queryParam("email")).thenReturn("test@example.com");
        Map<String, Object> customer = Map.of("id", 1, "email", "test@example.com");
        when(bookingManager.findCustomerByEmail("test@example.com")).thenReturn(customer);

        bookingResource.checkCustomerEmail(ctx);

        verify(ctx).status(HttpStatus.OK);
        verify(ctx).json(Map.of("exists", true, "customer", customer));
    }

    @Test
    void testCheckCustomerEmailNotExists() {
        when(ctx.queryParam("email")).thenReturn("notfound@example.com");
        when(bookingManager.findCustomerByEmail("notfound@example.com")).thenReturn(null);

        bookingResource.checkCustomerEmail(ctx);

        verify(ctx).status(HttpStatus.OK);
        verify(ctx).json(Map.of("exists", false));
    }

    @Test
    void testCheckCustomerEmailMissing() {
        when(ctx.queryParam("email")).thenReturn(null);

        bookingResource.checkCustomerEmail(ctx);

        verify(ctx).status(HttpStatus.BAD_REQUEST);
        verify(ctx).json(Map.of("error", "Email parameter is required"));
    }

    @Test
    void testDeleteBooking() {
        when(ctx.pathParam("id")).thenReturn("1");

        bookingResource.softDelete(ctx);

        verify(bookingManager).softDelete(1);
        verify(ctx).status(204);
    }
}