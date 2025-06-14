package io.github.fontysvenlo.ais.persistence;

import io.github.fontysvenlo.ais.persistence.api.BookingRepository;
import io.github.fontysvenlo.ais.persistence.api.EmployeeRepository;
import io.github.fontysvenlo.ais.persistence.api.FlightRepository;
import io.github.fontysvenlo.ais.persistence.api.Persistence;
import io.github.fontysvenlo.ais.persistence.api.PriceRepository;
import io.github.fontysvenlo.ais.persistence.api.UserRepository;

/**
 * Actual creator of storage services.
 */
class PersistenceImpl implements Persistence {

    private DBConfig config;

    PersistenceImpl(DBConfig config) {
        this.config = config;
    }

    @Override
    public EmployeeRepository getEmployeeRepository() {
        return new EmployeeRepositoryImpl(config);
    }

    @Override
    public FlightRepository getFlightRepository() {
        return new FlightRepositoryImpl(config);
    }

    @Override
    public PriceRepository getPriceRepository() { return new PriceRepositoryImpl(config);}

    @Override
    public UserRepository getUserRepository() { return new UserRepositoryImpl(config);
    }

    @Override
    public FlightStatsRepository getFlightStatsRepository() {
        return new FlightStatsRepositoryImpl(config);
    }
    @Override
    public DiscountRepository getDiscountRepository() {
        return new DiscountRepositoryImpl(config);
    }
    
    @Override
    public BookingRepository getBookingRepository() {
        return new BookingRepositoryImpl(config);
    }
}
