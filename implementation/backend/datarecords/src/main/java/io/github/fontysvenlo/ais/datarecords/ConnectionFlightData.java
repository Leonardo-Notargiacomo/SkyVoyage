package io.github.fontysvenlo.ais.datarecords;

import java.time.LocalDateTime;

/**
 * Data record representing a connection flight segment.
 * This is a simplified version of FlightData to avoid circular references.
 */
public record ConnectionFlightData(
    String id,
    String airline,
    String departureAirportShort,
    String departureTerminal,
    LocalDateTime departureScheduledTime,
    String arrivalAirportShort,
    String arrivalTerminal,
    LocalDateTime arrivalScheduledTime,
    Integer duration
) {
    /**
     * Basic constructor with minimal required information.
     */
    public ConnectionFlightData(String id, String departureAirportShort, String arrivalAirportShort, Integer duration) {
        this(id, null, departureAirportShort, null, null, arrivalAirportShort, null, null, duration);
    }
}
