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

    LoginService getLoginService();
}
