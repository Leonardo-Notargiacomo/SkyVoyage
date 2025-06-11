package io.github.fontysvenlo.ais.persistence.api;

import java.util.List;
import java.util.Map;

import io.github.fontysvenlo.ais.datarecords.BookingData;

/**
 * Repository interface for bookings.
 */
public interface BookingRepository {
    /**
     * Adds a booking.
     * 
     * @param bookingData the booking to add
     * @return the added booking with generated ID
     */
    BookingData add(BookingData bookingData);
    
    /**
     * Lists all bookings.
     * 
     * @return a list of all bookings
     */
    List<BookingData> list();
    
    /**
     * Gets a booking by its ID.
     * 
     * @param id the booking ID
     * @return the booking, or null if not found
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
     * Finds a customer by email.
     * 
     * @param email the email to search for
     * @return a map containing customer data if found, or null if not found
     */
    Map<String, Object> findCustomerByEmail(String email);
    
    /**
     * Adds a booking using a simple map structure.
     * 
     * @param bookingMap the booking data as a map
     * @return a map containing the created booking details
     */
    Map<String, Object> addSimple(Map<String, Object> bookingMap);
}
