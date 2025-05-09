package io.github.fontysvenlo.ais.persistence.api;

import java.util.List;

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
     * Updates a booking.
     * 
     * @param bookingData the updated booking data
     * @return the updated booking
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
