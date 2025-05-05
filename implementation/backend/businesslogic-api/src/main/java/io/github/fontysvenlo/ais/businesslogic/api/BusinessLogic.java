package io.github.fontysvenlo.ais.businesslogic.api;

/**
 * API of the BusinessLogic layer.
 */
public interface BusinessLogic {

    /**
     * Get the CustomerManager.
     *
     * @return the CustomerManager
     */
    CustomerManager getCustomerManager();

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
     * Get the FlightStatsManager.
     *
     * @return the FlightStatsManager
     */
    FlightStatsManager getFlightStatsManager();
}
