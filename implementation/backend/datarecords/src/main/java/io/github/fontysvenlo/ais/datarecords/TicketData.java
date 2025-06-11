package io.github.fontysvenlo.ais.datarecords;

/**
 * Data record for a ticket.
 * A record is not mutable. Getter methods (e.g. flightNumber(), not getFlightNumber()),
 * hashCode(), equals() and toString() available for free.
 * 
 * Flight data
 * @param flightNumber the flight number 
 * @param departureAirport the departure airport 
 * @param departureTerminal the departure terminal 
 * @param departureGate the departure gate 
 * @param departureTime the departure time 
 * @param arrivalAirport the arrival airport 
 * @param arrivalTime the arrival time 
 * 
 * Passenger data
 * @param firstName the first name 
 * @param lastName the last name 
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
