package io.github.fontysvenlo.ais.datarecords;

/**
 * Data record for a ticket.
 * A record is not mutable. Getter methods (e.g. flightNumber(), not getFlightNumber()),
 * hashCode(), equals() and toString() available for free.
 * @param flightNumber the flight number of the flight
 * @param departureAirport the departure airport of the flight
 * @param departureTerminal the departure terminal of the flight
 * @param departureGate the departure gate of the flight
 * @param departureTime the departure time of the flight
 * @param arrivalAirport the arrival airport of the flight
 * @param arrivalTime the arrival time of the flight
 * @param firstName the first name of the passenger
 * @param lastName the last name of the passenger
 * @param hasSeat if the passenger has a seat
 */

public record TicketData(
    String flightNumber, 
    String departureAirport, 
    String departureTerminal, 
    String departureGate, 
    String departureTime, 
    String arrivalAirport, 
    String arrivalTime, 
    String firstName, 
    String lastName,
    boolean hasSeat) 
{ }
