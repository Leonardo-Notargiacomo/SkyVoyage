package io.github.fontysvenlo.ais.businesslogic.api;

import java.util.List;
import java.util.Map;

import io.github.fontysvenlo.ais.datarecords.BookingData;

/**
 * Interface for managing bookings.
 */
public interface BookingManager {
    /**
     * Gets all bookings.
     * 
     * @return A list of all bookings
     */
    List<BookingData> list();
    
    /**
     * Gets a booking by its ID.
     * 
     * @param id The booking ID
     * @return The booking data, or null if not found
     */
    BookingData getOne(Integer id);
    
    /**
     * Gets bookings by customer ID.
     * 
     * @param customerId The customer ID
     * @return List of bookings for the customer
     */
    List<BookingData> getByCustomerId(Integer customerId);

    /**
     * Checks if a customer with the given email already exists.
     * 
     * @param email the email to check
     * @return a map containing customer data if found, or null if not found
     */
    Map<String, Object> findCustomerByEmail(String email);
    
    /**
     * Adds a booking using a map structure.
     * 
     * @param bookingMap Map containing booking data
     * @return Map containing the result
     */
    Map<String, Object> add(Map<String, Object> bookingMap);
}
