package io.github.fontysvenlo.ais.businesslogic;

import java.util.List;
import java.util.logging.Logger;

import io.github.fontysvenlo.ais.businesslogic.api.EmployeeManager;
import io.github.fontysvenlo.ais.businesslogic.api.ValidatorInterface;
import io.github.fontysvenlo.ais.businesslogic.resources.ErrorMessages;
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
            throw new IllegalArgumentException(ErrorMessages.getMessage("invalid_name"));
        }

        if (!validator.isValidName(employeeData.Lastname())) {
            throw new IllegalArgumentException(ErrorMessages.getMessage("invalid_name"));
        }

        if (!validator.isValidEmail(employeeData.Email())) {
            throw new IllegalArgumentException(ErrorMessages.getMessage("invalid_email"));
        }

        if (!validator.isValidPassword(employeeData.Password())) {
            throw new IllegalArgumentException(ErrorMessages.getMessage("invalid_password"));
        }

        // Updated employee type validation to include "Admin"
        if (employeeData.Type() == null || employeeData.Type().trim().isEmpty()
                || (!employeeData.Type().equals("SalesManager")
                && !employeeData.Type().equals("SalesEmployee")
                && !employeeData.Type().equals("AccountManager")
                && !employeeData.Type().equals("Admin"))) {
            throw new IllegalArgumentException(ErrorMessages.getMessage("invalid_type"));
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
        ValidatorInterface validator = new Validator();

        // Validate required fields and formats
        if (employeeData.Firstname() == null || employeeData.Firstname().trim().isEmpty() ||
            !validator.isValidName(employeeData.Firstname())) {
            throw new IllegalArgumentException(ErrorMessages.getMessage("invalid_name"));
        }

        if (employeeData.Lastname() == null || employeeData.Lastname().trim().isEmpty() ||
            !validator.isValidName(employeeData.Lastname())) {
            throw new IllegalArgumentException(ErrorMessages.getMessage("invalid_name"));
        }

        if (employeeData.Email() == null || employeeData.Email().trim().isEmpty() ||
            !validator.isValidEmail(employeeData.Email())) {
            throw new IllegalArgumentException(ErrorMessages.getMessage("invalid_email"));
        }

        // Password validation is only required if a new password is provided
        if (employeeData.Password() != null && !employeeData.Password().trim().isEmpty() && 
            !validator.isValidPassword(employeeData.Password())) {
            throw new IllegalArgumentException(ErrorMessages.getMessage("invalid_password"));
        }

        // Employee type validation
        if (employeeData.Type() == null || employeeData.Type().trim().isEmpty() ||
            (!employeeData.Type().equals("SalesManager") && 
             !employeeData.Type().equals("SalesEmployee") && 
             !employeeData.Type().equals("AccountManager") &&
             !employeeData.Type().equals("Admin"))) {
            throw new IllegalArgumentException(ErrorMessages.getMessage("invalid_type"));
        }

        return employeeRepository.update(employeeData);
    }

    @Override
    public EmployeeData delete(String id) {
        return employeeRepository.delete(id);
    }
}
