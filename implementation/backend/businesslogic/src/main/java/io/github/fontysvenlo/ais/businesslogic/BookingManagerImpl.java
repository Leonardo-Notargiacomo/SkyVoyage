package io.github.fontysvenlo.ais.businesslogic;

import java.util.List;
import java.util.Map;

import io.github.fontysvenlo.ais.businesslogic.api.BookingManager;
import io.github.fontysvenlo.ais.datarecords.BookingData;
import io.github.fontysvenlo.ais.persistence.api.BookingRepository;

public class BookingManagerImpl implements BookingManager {
    
    private final BookingRepository bookingRepository;
    
    public BookingManagerImpl(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
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
    public List<BookingData> getByCustomerId(Integer customerId) {
        return bookingRepository.getByCustomerId(customerId);
    }
    
    @Override
    public Map<String, Object> findCustomerByEmail(String email) {
        return bookingRepository.findCustomerByEmail(email);
    }
    
    @Override
    public Map<String, Object> add(Map<String, Object> bookingMap) {
        return bookingRepository.add(bookingMap);
    }

    @Override
    public void delete(int id) {
        bookingRepository.delete(id);
    }
}
