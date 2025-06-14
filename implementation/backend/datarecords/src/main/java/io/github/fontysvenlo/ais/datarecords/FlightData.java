package io.github.fontysvenlo.ais.datarecords;

import java.time.LocalDateTime;

/**
 * Data record representing a flight with all its details.
 */
public record FlightData(
    String id,
    String airline,
    double price,
    String departureAirport,
    String departureAirportShort,
    String departureTerminal,
    String departureGate,
    LocalDateTime departureScheduledTime,
    Integer departureDelay,
    String arrivalAirport,
    String arrivalAirportShort,
    String arrivalTerminal,
    String arrivalGate,
    LocalDateTime arrivalScheduledTime,
    Integer arrivalDelay,
    Integer duration,
    String status,
    boolean isConnection  // Flag to indicate if this is a connection flight
) {
    /**
     * Constructor to support AviationStackClient signature.
     * This constructor converts primitive int to Integer where needed.
     */
    public FlightData(
            String id,
            String airline,
            String departureAirport,
            String departureAirportShort,
            String departureTerminal,
            LocalDateTime departureScheduledTime,
            int departureDelay,
            String arrivalAirport,
            String arrivalAirportShort,
            String arrivalTerminal,
            String arrivalGate,
            LocalDateTime arrivalScheduledTime,
            int arrivalDelay,
            int duration) {
        this(id, airline, 0.0, departureAirport, departureAirportShort, departureTerminal, null,
             departureScheduledTime, departureDelay, arrivalAirport, arrivalAirportShort, 
             arrivalTerminal, arrivalGate, arrivalScheduledTime, arrivalDelay, duration, "SCHEDULED", false);
    }
    
    /**
     * Constructor with default values for optional fields.
     */
    public FlightData(
            String id,
            String airline,
            double price,
            String departureAirport,
            String departureAirportShort,
            String arrivalAirport,
            String arrivalAirportShort,
            Integer duration) {
        this(id, airline, price, departureAirport, departureAirportShort, null, null, null, 0,
             arrivalAirport, arrivalAirportShort, null, null, null, 0, duration, "SCHEDULED", false);
    }
    
    /**
     * Basic constructor with minimal required information.
     */
    public FlightData(String id, String departureAirportShort, String arrivalAirportShort) {
        this(id, null, 0.0, null, departureAirportShort, null, null, null, 0,
             null, arrivalAirportShort, null, null, null, 0, 180, "SCHEDULED", false);
    }
    
    /**
     * Create a connection flight.
     */
    public static FlightData createConnectionFlight(
            String id,
            String airline,
            String departureAirportShort,
            String departureTerminal,
            LocalDateTime departureScheduledTime,
            String arrivalAirportShort, 
            String arrivalTerminal,
            LocalDateTime arrivalScheduledTime,
            Integer duration) {
        return new FlightData(
            id, airline, 0.0, 
            "Airport " + departureAirportShort, departureAirportShort, departureTerminal, null, 
            departureScheduledTime, 0, 
            "Airport " + arrivalAirportShort, arrivalAirportShort, arrivalTerminal, null,
            arrivalScheduledTime, 0, 
            duration, "SCHEDULED", true);
    }
}
