package io.github.fontysvenlo.ais.businesslogic;

import java.util.List;
import java.util.logging.Logger;

import io.github.fontysvenlo.ais.businesslogic.api.EmployeeManager;
import io.github.fontysvenlo.ais.businesslogic.api.ValidatorInterface;
import io.github.fontysvenlo.ais.datarecords.EmployeeData;
import io.github.fontysvenlo.ais.persistence.api.EmployeeRepository;

/**
 * Manages employees in the business logic. Linking pin between GUI and
 * persistence. Connected to EmployeeRepository in order to retrieve employees
 * and to persist changes.
 */
public class EmployeeManagerImpl implements EmployeeManager {

    private static final Logger logger = Logger.getLogger(EmployeeManagerImpl.class.getName());
    private final EmployeeRepository employeeRepository;

    /**
     * Constructor
     *
     * @param employeeRepository the employee storage service
     */
    public EmployeeManagerImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /**
     * Adds an employee to the storage.
     *
     * @param employeeData the employee to add
     * @return the added employee data
     */
    @Override
    public EmployeeData add(EmployeeData employeeData) {
        ValidatorInterface validator = new Validator();

        if (!validator.isValidName(employeeData.Firstname())) {
            throw new IllegalArgumentException("Invalid employee name: " + employeeData.Firstname());
        }

        if (!validator.isValidName(employeeData.Lastname())) {
            throw new IllegalArgumentException("Invalid employee name: " + employeeData.Lastname());
        }

        if (!validator.isValidEmail(employeeData.Email())) {
            throw new IllegalArgumentException("Invalid employee email: " + employeeData.Email());
        }

        if (!validator.isValidPassword(employeeData.Password())) {
            throw new IllegalArgumentException("Invalid employee password: " + employeeData.Password());
        }

        // Updated employee type validation to include "Admin"
        if (employeeData.Type() == null || employeeData.Type().trim().isEmpty()
                || (!employeeData.Type().equals("SalesManager")
                && !employeeData.Type().equals("SalesEmployee")
                && !employeeData.Type().equals("AccountManager")
                && !employeeData.Type().equals("Admin"))) {
            throw new IllegalArgumentException("Invalid employee type: " + employeeData.Type());
        }

        Employee employee = new Employee(employeeData);
        return employeeRepository.add(employeeData);
    }

    /**
     * Retrieves all employees from the storage.
     *
     * @return a list of all employees
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
        return employeeRepository.update(employeeData);
    }

    @Override
    public EmployeeData delete(String id) {
        return employeeRepository.delete(id);
    }
}
