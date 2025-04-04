package io.github.fontysvenlo.ais.datarecords;

import java.time.LocalDateTime;

/**
 * Data record for flight information.
 */
public record FlightData(
        String id,
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
        Integer duration) {

}
