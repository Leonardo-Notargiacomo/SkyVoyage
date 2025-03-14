package io.github.fontysvenlo.ais.businesslogic;

import io.github.fontysvenlo.ais.businesslogic.api.EmployeeManager;
import io.github.fontysvenlo.ais.businesslogic.api.ValidatorInterface;
import io.github.fontysvenlo.ais.datarecords.EmployeeData;
import io.github.fontysvenlo.ais.persistence.api.EmployeeRepository;

import java.util.List;


/**
 * Manages customers in the business logic.
 * Linking pin between GUI and persistence. Connected to CustomerRepository
 * in order to retrieve customers and to persist changes.
 */
public class EmployeeManagerImpl implements EmployeeManager {

    private final EmployeeRepository employeeRepository;

    /**
     * Constructor
     * @param employeeRepository the customer storage service
     */
    public EmployeeManagerImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /**
     * Adds a customer to the storage.
     * @param employeeData the customer to add
     * @return the added customer data
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

        if ( employeeData.Type().isEmpty() || !employeeData.Type().equals("SalesManager") && !employeeData.Type().equals("SalesEmployee") && !employeeData.Type().equals("AcountManager")) {
            throw new IllegalArgumentException("Invalid employee Type: " + employeeData.Type());
        }

        Employee employee = new Employee(employeeData);
        return employeeRepository.add(employeeData);
    }

    /**
     * Retrieves all customers from the storage.
     * @return a list of all customers
     */
    @Override
    public List<EmployeeData> list(){
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
