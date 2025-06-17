package io.github.fontysvenlo.ais.businesslogic.api;

/**
 * API of the BusinessLogic layer.
 */
public interface BusinessLogic {
    /**
     * Get the EmployeeManager.
     *
     * @return the EmployeeManager
     */
    EmployeeManager getEmployeeManager();

    /**
     * Get the FlightManager.
     *
     * @return the FlightManager
     */
    FlightManager getFlightManager();
    /**
     * Get the FlightManager.
     *
     * @return the FlightManager
     */
    PriceManager getPriceManager();

    LoginService getLoginService();

    /**
     * Get the TicketManager.
     *
     * @return the TicketManager
     */
    TicketManager getTicketManager();
  
    /**
     * Get the BookingManager.
     * 
     * @return the booking manager
     */
    BookingManager getBookingManager();

    /**
     * Get the FlightStatsManager.
     *
     * @return the FlightStatsManager
     */
    FlightStatsManager getFlightStatsManager();
    
    /**
     * Get the DiscountManager.
     *
     * @return the DiscountManager
     */
    DiscountManager getDiscountManager();
}
