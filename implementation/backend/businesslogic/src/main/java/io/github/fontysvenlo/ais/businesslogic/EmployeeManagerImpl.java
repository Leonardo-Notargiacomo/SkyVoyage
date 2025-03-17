package io.github.fontysvenlo.ais.businesslogic;

import java.util.List;

import io.github.fontysvenlo.ais.businesslogic.api.EmployeeManager;
import io.github.fontysvenlo.ais.datarecords.EmployeeData;
import io.github.fontysvenlo.ais.persistence.api.EmployeeRepository;

/**
 * Manages employees in the business logic.
 * Linking pin between GUI and persistence. Connected to EmployeeRepository
 * in order to retrieve employees and to persist changes.
 */
public class EmployeeManagerImpl implements EmployeeManager {

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
        // Validate
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
        return null;
    }

    @Override
    public EmployeeData delete(String id) {
        return employeeRepository.delete(id);
    }
}
