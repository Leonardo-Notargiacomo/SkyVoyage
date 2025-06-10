package io.github.fontysvenlo.ais.restapi;

import io.github.fontysvenlo.ais.businesslogic.api.BookingManager;
import io.github.fontysvenlo.ais.datarecords.BookingData;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * REST resource for booking operations.
 */
public class BookingResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookingResource.class);
    private final BookingManager bookingManager;

    /**
     * Creates a new BookingResource with the given BookingManager.
     *
     * @param bookingManager the booking manager
     */
    public BookingResource(BookingManager bookingManager) {
        this.bookingManager = bookingManager;
    }

    /**
     * Creates a new booking.
     *
     * @param ctx the Javalin context
     */
    public void create(Context ctx) {
        try {
            // Parse request as a Map instead of BookingData
            Map<String, Object> bookingMap = ctx.bodyAsClass(Map.class);

            LOGGER.info("Creating booking with data: {}", bookingMap);

            // Use the map directly in the repository
            Map<String, Object> result = bookingManager.addSimple(bookingMap);

            ctx.status(HttpStatus.CREATED);
            ctx.json(result);
        } catch (Exception e) {
            LOGGER.error("Failed to create booking", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lists all bookings.
     *
     * @param ctx the Javalin context
     */
    public void list(Context ctx) {
        try {
            List<BookingData> bookings = bookingManager.list();
            ctx.json(bookings);
        } catch (Exception e) {
            LOGGER.error("Failed to list bookings", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Gets a specific booking by ID.
     *
     * @param ctx the Javalin context
     */
    public void getOne(Context ctx) {
        try {
            String idParam = ctx.pathParam("id");
            Integer id = Integer.parseInt(idParam);
            BookingData booking = bookingManager.getOne(id);

            if (booking == null) {
                ctx.status(HttpStatus.NOT_FOUND);
                ctx.json(Map.of("error", "Booking not found"));
                return;
            }

            ctx.json(booking);
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(Map.of("error", "Invalid booking ID format"));
        } catch (Exception e) {
            LOGGER.error("Failed to get booking", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Updates a booking.
     *
     * @param ctx the Javalin context
     */
    public void update(Context ctx) {
        try {
            String idParam = ctx.pathParam("id");
            Integer id = Integer.parseInt(idParam);
            BookingData existingBooking = bookingManager.getOne(id);

            if (existingBooking == null) {
                ctx.status(HttpStatus.NOT_FOUND);
                ctx.json(Map.of("error", "Booking not found"));
                return;
            }

            BookingData updatedBooking = ctx.bodyAsClass(BookingData.class);

            // Ensure the ID in the path matches the ID in the body
            if (updatedBooking.id() != null && !updatedBooking.id().equals(id)) {
                ctx.status(HttpStatus.BAD_REQUEST);
                ctx.json(Map.of("error", "Booking ID in path doesn't match ID in body"));
                return;
            }

            // Create a new BookingData with the ID from the path
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
                    updatedBooking.customers(),
                    updatedBooking.flight(),
                    updatedBooking.connectionFlights()
            );

            BookingData result = bookingManager.update(bookingToUpdate);
            ctx.json(result);
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(Map.of("error", "Invalid booking ID format"));
        } catch (Exception e) {
            LOGGER.error("Failed to update booking", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Deletes a booking.
     *
     * @param ctx the Javalin context
     */
    public void delete(Context ctx) {
        try {
            String idParam = ctx.pathParam("id");
            Integer id = Integer.parseInt(idParam);
            BookingData existingBooking = bookingManager.getOne(id);

            if (existingBooking == null) {
                ctx.status(HttpStatus.NOT_FOUND);
                ctx.json(Map.of("error", "Booking not found"));
                return;
            }

            // Create a booking with canceled status
            BookingData bookingToCancel = new BookingData(
                    id,
                    existingBooking.flightId(),
                    existingBooking.airline(),
                    existingBooking.price(),
                    existingBooking.adultPassengers(),
                    existingBooking.infantPassengers(),
                    existingBooking.travelClass(),
                    existingBooking.discount(),
                    existingBooking.discountReason(),
                    existingBooking.bookedAt(),
                    "CANCELLED",
                    existingBooking.customers(),
                    existingBooking.flight(),
                    existingBooking.connectionFlights()
            );

            BookingData result = bookingManager.update(bookingToCancel);
            ctx.json(result);
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(Map.of("error", "Invalid booking ID format"));
        } catch (Exception e) {
            LOGGER.error("Failed to delete booking", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Gets bookings for a specific customer.
     *
     * @param ctx the Javalin context
     */
    public void getByCustomerId(Context ctx) {
        try {
            String customerIdParam = ctx.pathParam("customerId");
            Integer customerId = Integer.parseInt(customerIdParam);
            List<BookingData> bookings = bookingManager.getByCustomerId(customerId);
            ctx.json(bookings);
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(Map.of("error", "Invalid customer ID format"));
        } catch (Exception e) {
            LOGGER.error("Failed to get bookings for customer", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Checks if a customer with the given email already exists.
     *
     * @param ctx the Javalin context
     */
    public void checkCustomerEmail(Context ctx) {
        try {
            String email = ctx.queryParam("email");

            if (email == null || email.trim().isEmpty()) {
                ctx.status(400).json(Map.of("error", "Email parameter is required"));
                return;
            }

            Map<String, Object> existingCustomer = bookingManager.findCustomerByEmail(email);

            if (existingCustomer != null) {
                ctx.status(200).json(Map.of(
                        "exists", true,
                        "customer", existingCustomer
                ));
            } else {
                ctx.status(200).json(Map.of("exists", false));
            }
        } catch (Exception e) {
            LOGGER.error("Error checking customer by email", e);
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }
    }
}
