package io.github.fontysvenlo.ais.businesslogic.api;

import java.util.List;

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
     * Gets bookings by customer ID.
     * 
     * @param customerId The customer ID
     * @return List of bookings for the customer
     */
    List<BookingData> getByCustomerId(Integer customerId);
}
