package io.github.fontysvenlo.ais.restapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.fontysvenlo.ais.businesslogic.api.BookingManager;
import io.github.fontysvenlo.ais.datarecords.BookingData;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

/**
 * REST resource for booking operations.
 */
public class BookingResource {

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
            Map<String, Object> bookingMap = ctx.bodyAsClass(Map.class);
            
            // Process the booking data to ensure it has valid flight information
            bookingMap = processBookingData(bookingMap);
            
            // Call the booking manager to create the booking
            Map<String, Object> result = bookingManager.add(bookingMap);

            ctx.status(HttpStatus.CREATED);
            ctx.json(result);
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Process booking data to ensure it has all necessary flight information.
     * 
     * @param bookingMap The original booking data map
     * @return The processed booking data map
     * @throws IllegalArgumentException If the booking data is invalid
     */
    private Map<String, Object> processBookingData(Map<String, Object> bookingMap) {
        if (bookingMap == null) {
            throw new IllegalArgumentException("Invalid booking details - request body is null");
        }
        
        // Check if we need to construct the mainFlights array from various sources
        if (!bookingMap.containsKey("mainFlights")) {
            List<Map<String, Object>> mainFlights = new ArrayList<>();
            
            // Process outbound flight if present
            Map<String, Object> outboundFlight = getSafeMap(bookingMap, "outboundFlight");
            if (outboundFlight != null) {
                ensureFlightId(outboundFlight);
                mainFlights.add(outboundFlight);
            }
            
            // Process return flight if present
            Map<String, Object> returnFlight = getSafeMap(bookingMap, "returnFlight");
            if (returnFlight != null) {
                ensureFlightId(returnFlight);
                mainFlights.add(returnFlight);
            }
            
            // Use regular flight if no outbound/return
            if (mainFlights.isEmpty()) {
                Map<String, Object> flight = getSafeMap(bookingMap, "flight");
                if (flight != null) {
                    ensureFlightId(flight);
                    mainFlights.add(flight);
                } else {
                    String flightId = (String) bookingMap.get("flightId");
                    if (flightId != null && !flightId.isEmpty()) {
                        Map<String, Object> basicFlight = Map.of(
                            "id", flightId,
                            "airline", bookingMap.getOrDefault("airline", "Unknown")
                        );
                        mainFlights.add(basicFlight);
                    }
                }
            }
            
            // Add the list to the booking map if we have anything
            if (!mainFlights.isEmpty()) {
                bookingMap.put("mainFlights", mainFlights);
            } else {
                throw new IllegalArgumentException("Invalid booking details - no valid flight data found");
            }
        }
        
        return bookingMap;
    }

    /**
     * Helper method to ensure a flight has a valid ID
     * 
     * @param flight The flight map to check
     */
    private void ensureFlightId(Map<String, Object> flight) {
        if (flight == null) return;
        
        // If no ID but we have IATA codes, generate one
        if (!flight.containsKey("id") || flight.get("id") == null) {
            if (flight.containsKey("departureAirportShort") && flight.containsKey("arrivalAirportShort")) {
                String depCode = (String) flight.get("departureAirportShort");
                String arrCode = (String) flight.get("arrivalAirportShort");
                String flightNumber = flight.containsKey("number") ? flight.get("number").toString() : "0";
                
                if (depCode != null && arrCode != null) {
                    String id = flightNumber + "-" + depCode + "-" + arrCode;
                    flight.put("id", id);
                }
            }
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
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> getSafeMap(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key)) return null;
        Object value = map.get(key);
        return value instanceof Map ? (Map<String, Object>)value : null;
    }
}
