package io.github.fontysvenlo.ais.datarecords;

/**
 * Data record for a ticket.
 * A record is not mutable. Getter methods (e.g. flightNumber(), not getFlightNumber()),
 * hashCode(), equals() and toString() available for free.
 * @param flightNumber the flight number of the ticket
 * @param departureAirport the departure airport of the ticket
 * @param departureTerminal the departure terminal of the ticket
 * @param departureGate the departure gate of the ticket
 * @param departureTime the departure time of the ticket
 * @param departureDelay the departure delay of the ticket
 * @param arrivalAirport the arrival airport of the ticket
 * @param arrivalTerminal the arrival terminal of the ticket
 * @param arrivalGate the arrival gate of the ticket
 * @param arrivalTime the arrival time of the ticket
 * @param arrivalDelay the arrival delay of the ticket
 * @param hasSeat if the ticket has a seat
 * @param firstName the first name of the passenger
 * @param lastName the last name of the passenger
 * @param email the email address of the passenger
 * @param phoneNumber the phone number of the passenger
 * @param country the country of residence of the passenger
 * @param city the city of residence of the passenger
 * @param streetName the street name of residence of the passenger
 * @param houseNumber the house number of residence of the passenger
 */

public record TicketData(String flightNumber, String departureAirport, String departureTerminal, String departureGate, String departureTime, int departureDelay, String arrivalAirport, String arrivalTerminal, String arrivalGate, String arrivalTime, int arrivalDelay, boolean hasSeat, String firstName, String lastName, String email, String phoneNumber, String country, String city, String streetName, String houseNumber) 
{ }
