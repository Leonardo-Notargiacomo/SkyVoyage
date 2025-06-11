package io.github.fontysvenlo.ais.persistence;

import io.github.fontysvenlo.ais.persistence.api.*;

/**
 * Actual creator of storage services.
 */
class PersistenceImpl implements Persistence {

    private DBConfig config;

    PersistenceImpl(DBConfig config) {
        this.config = config;
    }

    /**
     * Get the implementation of the CustomerRepository.
     *
     * @return the implementation of the CustomerRepository
     */
    @Override
    public CustomerRepository getCustomerRepository() {
        return new CustomerRepositoryImpl(config);
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
  
    @Override
    public DiscountRepository getDiscountRepository() {
        return new DiscountRepositoryImpl(config);
    }
}
