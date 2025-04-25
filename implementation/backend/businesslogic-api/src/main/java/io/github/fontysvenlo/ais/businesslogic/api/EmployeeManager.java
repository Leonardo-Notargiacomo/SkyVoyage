package io.github.fontysvenlo.ais.businesslogic.api;

import java.util.List;

import io.github.fontysvenlo.ais.datarecords.EmployeeData;

/**
 * Interface for managing employees in the business logic layer. Provides
 * methods for CRUD operations on employee data.
 */
public interface EmployeeManager {

    /**
     * Adds an employee to the storage.
     *
     * @param employeeData the employee to add
     * @return the added employee data
     */
    public EmployeeData add(EmployeeData employeeData);

    /**
     * Retrieves all employees from the storage.
     *
     * @return a list of all employees
     */
    public List<EmployeeData> list();

    /**
     * Retrieves a single employee from the storage.
     *
     * @param id the employee id
     * @return the employee data
     */
    public EmployeeData getOne(String id);

    /**
     * Updates an employee in the storage.
     *
     * @param employeeData the employee to update
     * @return the updated employee data
     */
    public EmployeeData update(EmployeeData employeeData);

    /**
     * Deletes an employee from the storage.
     *
     * @param id the employee id to delete
     * @return the deleted employee data
     */
    public EmployeeData delete(String id);

    public EmployeeData getByEmail(String email);
}
