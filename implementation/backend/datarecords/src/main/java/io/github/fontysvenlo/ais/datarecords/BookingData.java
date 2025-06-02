package io.github.fontysvenlo.ais.datarecords;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data record representing a flight booking, which may include multiple passengers.
 */
public record BookingData(
    Integer id,
    String flightId,  // ID of the main flight
    String airline,
    double price,
    int adultPassengers,
    int infantPassengers,
    String travelClass,
    int discount,
    String discountReason,
    LocalDateTime bookedAt,
    String status,
    List<CustomerData> customers,
    FlightData flight,  // Main flight
    List<FlightData> connectionFlights  // Connection flights
) {
    /**
     * Constructor with default ID and booking time.
     * Sets the booking time to current time.
     */
    public BookingData(
            String flightId,
            String airline,
            double price,
            int adultPassengers,
            int infantPassengers,
            String travelClass,
            int discount,
            String discountReason,
            String status,
            List<CustomerData> customers,
            FlightData flight,
            List<FlightData> connectionFlights) {
        this(null, flightId, airline, price, adultPassengers, infantPassengers, travelClass, 
             discount, discountReason, LocalDateTime.now(), status, customers, flight, connectionFlights);
    }
    
    /**
     * Constructor with default ID and booking time but without flight data.
     */
    public BookingData(
            String flightId,
            String airline,
            double price,
            int adultPassengers,
            int infantPassengers,
            String travelClass,
            int discount,
            String discountReason,
            String status,
            List<CustomerData> customers) {
        this(null, flightId, airline, price, adultPassengers, infantPassengers, travelClass, 
             discount, discountReason, LocalDateTime.now(), status, customers, null, null);
    }
    
    /**
     * Constructor for backward compatibility with older code
     */
    public BookingData(
            Integer id,
            String flightId,
            String airline,
            double price,
            int adultPassengers,
            int infantPassengers,
            String travelClass,
            int discount,
            String discountReason,
            LocalDateTime bookedAt,
            String status,
            List<CustomerData> customers,
            FlightData flight) {
        this(id, flightId, airline, price, adultPassengers, infantPassengers, travelClass, 
             discount, discountReason, bookedAt, status, customers, flight, null);
    }

    /**
     * Constructor without flight data
     */
    public BookingData(
            Integer id,
            String flightId,
            String airline,
            double price,
            int adultPassengers,
            int infantPassengers,
            String travelClass,
            int discount,
            String discountReason,
            LocalDateTime bookedAt,
            String status,
            List<CustomerData> customers) {
        this(id, flightId, airline, price, adultPassengers, infantPassengers, travelClass, 
             discount, discountReason, bookedAt, status, customers, null, null);
    }

    /**
     * Constructor with basic flight information and no customers (for reading from database)
     */
    public BookingData(
            Integer id,
            String flightId,
            String airline,
            double price,
            int adultPassengers,
            int infantPassengers,
            String travelClass,
            String status) {
        this(id, flightId, airline, price, adultPassengers, infantPassengers, travelClass, 
             0, "", null, status, null, null, null);
    }
}
