package io.github.fontysvenlo.ais.businesslogic;

import java.util.List;
import java.util.Map;

import io.github.fontysvenlo.ais.businesslogic.api.BookingManager;
import io.github.fontysvenlo.ais.datarecords.BookingData;
import io.github.fontysvenlo.ais.persistence.api.BookingRepository;

/**
 * Implementation of the BookingManager interface.
 */
public class BookingManagerImpl implements BookingManager {
    
    private final BookingRepository bookingRepository;
    
    /**
     * Initializes a new booking manager.
     * 
     * @param bookingRepository the booking repository
     */
    public BookingManagerImpl(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }
    
    @Override
    public BookingData add(BookingData bookingData) {
        return bookingRepository.add(bookingData);
    }
    
    @Override
    public Map<String, Object> addSimple(Map<String, Object> bookingMap) {
        return bookingRepository.addSimple(bookingMap);
    }
    
    @Override
    public List<BookingData> list() {
        return bookingRepository.list();
    }
    
    @Override
    public BookingData getOne(Integer id) {
        return bookingRepository.getOne(id);
    }
    
    @Override
    public BookingData update(BookingData bookingData) {
        return bookingRepository.update(bookingData);
    }
    
    @Override
    public List<BookingData> getByCustomerId(Integer customerId) {
        return bookingRepository.getByCustomerId(customerId);
    }
}
