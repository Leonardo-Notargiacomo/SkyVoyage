package io.github.fontysvenlo.ais.businesslogic;

import java.util.List;
import java.util.logging.Logger;

import io.github.fontysvenlo.ais.businesslogic.api.EmployeeManager;
import io.github.fontysvenlo.ais.businesslogic.api.ValidatorInterface;
import io.github.fontysvenlo.ais.datarecords.EmployeeData;
import io.github.fontysvenlo.ais.persistence.api.EmployeeRepository;

/**
 * Manages customers in the business logic. Linking pin between GUI and
 * persistence. Connected to CustomerRepository in order to retrieve customers
 * and to persist changes.
 */
public class EmployeeManagerImpl implements EmployeeManager {

    private static final Logger logger = Logger.getLogger(EmployeeManagerImpl.class.getName());
    private final EmployeeRepository employeeRepository;

    /**
     * Constructor
     *
     * @param employeeRepository the customer storage service
     */
    public EmployeeManagerImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /**
     * Adds a customer to the storage.
     *
     * @param employeeData the customer to add
     * @return the added customer data
     */
    @Override
    public EmployeeData add(EmployeeData employeeData) {
        ValidatorInterface validator = new Validator();
        logger.info("Adding employee: " + employeeData);

        if (!validator.isValidName(employeeData.Firstname())) {
            logger.warning("Invalid firstname: " + employeeData.Firstname());
            throw new IllegalArgumentException("Invalid employee name: " + employeeData.Firstname());
        }

        if (!validator.isValidName(employeeData.Lastname())) {
            logger.warning("Invalid lastname: " + employeeData.Lastname());
            throw new IllegalArgumentException("Invalid employee name: " + employeeData.Lastname());
        }

        if (!validator.isValidEmail(employeeData.Email())) {
            logger.warning("Invalid email: " + employeeData.Email());
            throw new IllegalArgumentException("Invalid employee email: " + employeeData.Email());
        }

        if (!validator.isValidPassword(employeeData.Password())) {
            logger.warning("Invalid password: " + employeeData.Password());
            throw new IllegalArgumentException("Invalid employee password: " + employeeData.Password());
        }

        // Log the exact type being validated
        logger.info("About to validate employee type: '" + employeeData.Type() + "'");
        
        // Updated employee type validation to include "Admin"
        if (employeeData.Type() == null || employeeData.Type().trim().isEmpty()
                || (!employeeData.Type().equals("SalesManager")
                && !employeeData.Type().equals("SalesEmployee")
                && !employeeData.Type().equals("AccountManager")
                && !employeeData.Type().equals("Admin"))) {  // Added Admin validation here
            logger.warning("Invalid employee type: '" + employeeData.Type() + "'");
            throw new IllegalArgumentException("Invalid employee Type: " + employeeData.Type()
                    + ". Must be SalesManager, SalesEmployee, AccountManager, or Admin");
        }
        
        logger.info("Employee type validation passed for: '" + employeeData.Type() + "'");

        Employee employee = new Employee(employeeData);
        return employeeRepository.add(employeeData);
    }

    /**
     * Retrieves all customers from the storage.
     *
     * @return a list of all customers
     */
    @Override
    public List<EmployeeData> list() {
        return employeeRepository.getAll();
    }

    @Override
    public EmployeeData getOne(String id) {
        return employeeRepository.getOne(id);
    }

    @Override
    public EmployeeData update(EmployeeData employeeData) {
        return null;
    }

    @Override
    public EmployeeData delete(String id) {
        return employeeRepository.delete(id);
    }

}
