package io.github.fontysvenlo.ais.businesslogic;

import io.github.fontysvenlo.ais.businesslogic.api.*;
import io.github.fontysvenlo.ais.persistence.api.Persistence;

/**
 * Actual business logic implementation.
 */
class BusinessLogicImpl implements BusinessLogic {

    final Persistence persistenceAPI;

    /**
     * Constructor.
     * @param persistenceAPI the PersistenceAPI
     */
    BusinessLogicImpl(Persistence persistenceAPI) {
        this.persistenceAPI = persistenceAPI;
    }

    /**
     * Get the implementation of the CustomerManager.
     * @return the implementation of the CustomerManager
     */
    @Override
    public CustomerManager getCustomerManager() {
        return new CustomerManagerImpl(persistenceAPI.getCustomerRepository());
    }

    @Override
    public EmployeeManager getEmployeeManager() {
        return new EmployeeManagerImpl(persistenceAPI.getEmployeeRepository());
    }

    @Override
    public FlightManager getFlightManager() {
        return new FlightManagerImpl(persistenceAPI.getFlightRepository());
    }

    @Override
    public PriceManager getPriceManager() {
        return PriceManagerImpl.getInstance(persistenceAPI.getPriceRepository());
  
    @Override  
    public LoginService getLoginService() {
        return new LoginServiceImpl(persistenceAPI.getUserRepository());
    }
}
