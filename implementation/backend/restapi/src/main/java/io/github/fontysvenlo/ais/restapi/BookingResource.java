package io.github.fontysvenlo.ais.restapi;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.fontysvenlo.ais.businesslogic.api.BookingManager;
import io.github.fontysvenlo.ais.datarecords.BookingData;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;

/**
 * This class is responsible for handling the requests for the booking resource.
 */
class BookingResource implements CrudHandler {

    private static final Logger logger = LoggerFactory.getLogger(BookingResource.class);
    private final BookingManager bookingManager;

    /**
     * Initializes the controller with the business logic.
     */
    BookingResource(BookingManager bookingManager) {
        this.bookingManager = bookingManager;
    }

    /**
     * Adds a booking to the storage.
     */
    @Override
    public void create(Context context) {
        try {
            // Parse the booking data from the request body
            BookingData bookingData = context.bodyAsClass(BookingData.class);

            // Validate the booking data
            if (bookingData == null) {
                context.status(400).json(Map.of("error", "Invalid booking data"));
                return;
            }

            // Save the booking data to the database
            BookingData savedBooking = bookingManager.add(bookingData);

            // Return the saved booking data
            context.status(201).json(savedBooking);
        } catch (Exception e) {
            logger.error("Failed to create booking", e);
            context.status(500).json(Map.of("error", "Failed to create booking: " + e.getMessage()));
        }
    }

    /**
     * Retrieves all bookings from the storage.
     */
    @Override
    public void getAll(Context context) {
        try {
            context.json(bookingManager.list());
        } catch (Exception e) {
            logger.error("Failed to retrieve bookings", e);
            context.status(500).json(Map.of("error", "Failed to retrieve bookings: " + e.getMessage()));
        }
    }

    @Override
    public void delete(Context context, String bookingId) {
        // Not implementing delete operation for bookings
        context.status(405).json(Map.of("error", "Method not allowed"));
    }

    @Override
    public void getOne(Context context, String bookingId) {
        try {
            Integer id = Integer.parseInt(bookingId);
            BookingData booking = bookingManager.getOne(id);
            if (booking != null) {
                context.json(booking);
            } else {
                context.status(404).json(Map.of("error", "Booking not found"));
            }
        } catch (NumberFormatException e) {
            context.status(400).json(Map.of("error", "Invalid booking ID format"));
        } catch (Exception e) {
            logger.error("Failed to retrieve booking", e);
            context.status(500).json(Map.of("error", "Failed to retrieve booking: " + e.getMessage()));
        }
    }

    @Override
    public void update(Context context, String bookingId) {
        try {
            Integer id = Integer.parseInt(bookingId);
            BookingData existingBooking = bookingManager.getOne(id);

            if (existingBooking == null) {
                context.status(404).json(Map.of("error", "Booking not found"));
                return;
            }

            BookingData updatedBooking = context.bodyAsClass(BookingData.class);

            if (updatedBooking == null) {
                context.status(400).json(Map.of("error", "Invalid booking data"));
                return;
            }

            // Ensure the ID in the path matches the ID in the body
            BookingData bookingToUpdate = new BookingData(
                    id,
                    updatedBooking.flightId(),
                    updatedBooking.airline(),
                    updatedBooking.price(),
                    updatedBooking.adultPassengers(),
                    updatedBooking.infantPassengers(),
                    updatedBooking.travelClass(),
                    updatedBooking.discount(),
                    updatedBooking.discountReason(),
                    updatedBooking.bookedAt(),
                    updatedBooking.status(),
                    updatedBooking.customers()
            );

            BookingData result = bookingManager.update(bookingToUpdate);
            context.json(result);
        } catch (NumberFormatException e) {
            context.status(400).json(Map.of("error", "Invalid booking ID format"));
        } catch (Exception e) {
            logger.error("Failed to update booking", e);
            context.status(500).json(Map.of("error", "Failed to update booking: " + e.getMessage()));
        }
    }
}
