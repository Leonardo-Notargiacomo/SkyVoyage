package io.github.fontysvenlo.ais.businesslogic;

import io.github.fontysvenlo.ais.businesslogic.api.*;
import io.github.fontysvenlo.ais.persistence.api.FlightStatsRepository;
import io.github.fontysvenlo.ais.businesslogic.api.BusinessLogic;
import io.github.fontysvenlo.ais.businesslogic.api.DiscountManager;
import io.github.fontysvenlo.ais.businesslogic.api.EmployeeManager;
import io.github.fontysvenlo.ais.businesslogic.api.FlightManager;
import io.github.fontysvenlo.ais.businesslogic.api.LoginService;
import io.github.fontysvenlo.ais.businesslogic.api.PriceManager;
import io.github.fontysvenlo.ais.businesslogic.api.TicketManager;
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
    }
    
    @Override  
    public LoginService getLoginService() {
        return new LoginServiceImpl(persistenceAPI.getUserRepository());
    }

    @Override
    public TicketManager getTicketManager() {
        return new TicketManagerImpl(persistenceAPI.getTicketRepository());
    }
    
    @Override
    public FlightStatsManager getFlightStatsManager() {
        return new FlightStatsManagerImpl(persistenceAPI.getFlightStatsRepository());
    }
    @Override
    public DiscountManager getDiscountManager() {
        return new DiscountManagerImpl(persistenceAPI.getDiscountRepository());
    }

    @Override
    public BookingManager getBookingManager() {
        return new BookingManagerImpl(persistenceAPI.getBookingRepository());
    }
}
