package io.github.fontysvenlo.ais.datarecords;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data record representing a flight booking, which may include multiple passengers.
 */
public record BookingData(
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
    FlightData flight  // Added flight data field for storing detailed flight information
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
            List<CustomerData> customers) {
        this(null, flightId, airline, price, adultPassengers, infantPassengers, travelClass, 
             discount, discountReason, LocalDateTime.now(), status, customers, null);
    }
    
    /**
     * Constructor with default ID but specified booking time.
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
            LocalDateTime bookedAt,
            String status,
            List<CustomerData> customers) {
        this(null, flightId, airline, price, adultPassengers, infantPassengers, travelClass, 
             discount, discountReason, bookedAt, status, customers, null);
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
             discount, discountReason, bookedAt, status, customers, null);
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
             0, "", null, status, null);
    }
}
