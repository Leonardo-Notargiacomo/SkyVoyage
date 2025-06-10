package io.github.fontysvenlo.ais.businesslogic.api;

import java.util.List;
import java.util.Map;

import io.github.fontysvenlo.ais.datarecords.BookingData;

/**
 * Interface for managing bookings.
 */
public interface BookingManager {
    /**
     * Adds a booking to the system.
     * 
     * @param bookingData The booking data to add
     * @return The added booking data
     */
    BookingData add(BookingData bookingData);
    
    /**
     * Adds a booking using a simple map structure instead of complex records.
     * 
     * @param bookingMap Map containing booking data
     * @return Map containing the result
     */
    Map<String, Object> addSimple(Map<String, Object> bookingMap);
    
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
     * Updates a booking.
     * 
     * @param bookingData The updated booking data
     * @return The updated booking data
     */
    BookingData update(BookingData bookingData);
    
    /**
     * Checks if a customer with the given email already exists.
     * 
     * @param email the email to check
     * @return a map containing customer data if found, or null if not found
     */
    Map<String, Object> findCustomerByEmail(String email);
    
    /**
     * Gets bookings by customer ID.
     * 
     * @param customerId The customer ID
     * @return List of bookings for the customer
     */
    List<BookingData> getByCustomerId(Integer customerId);
}
