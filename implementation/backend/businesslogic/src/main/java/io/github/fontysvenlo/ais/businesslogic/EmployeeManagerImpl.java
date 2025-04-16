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

        // Validate required fields and formats
        if (!validator.isValidName(employeeData.Firstname())) {
            throw new IllegalArgumentException(ErrorMessages.getMessage("invalid_name"));
        }

        if (!validator.isValidName(employeeData.Lastname())) {
            throw new IllegalArgumentException(ErrorMessages.getMessage("invalid_name"));
        }

        if (!validator.isValidEmail(employeeData.Email())) {
            throw new IllegalArgumentException(ErrorMessages.getMessage("invalid_email", employeeData.Email()));
        }

        if (!validator.isValidPassword(employeeData.Password())) {
            throw new IllegalArgumentException(ErrorMessages.getMessage("invalid_password"));
        }

        if (!validator.isValidType(employeeData.Type())) {
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

    /**
     * Retrieves a single employee from the storage.
     *
     * @param id the employee id
     * @return the employee data
     */
    @Override
    public EmployeeData getOne(String id) {
        return employeeRepository.getOne(id);
    }

    /**
     * Updates an employee in the storage.
     *
     * @param employeeData the employee to update
     * @return the updated employee data
     */
    @Override
    public EmployeeData update(EmployeeData employeeData) {
        ValidatorInterface validator = new Validator();

        // Validate required fields and formats
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

        if (!validator.isValidType(employeeData.Type())) {
            throw new IllegalArgumentException(ErrorMessages.getMessage("invalid_type"));
        }

        return employeeRepository.update(employeeData);
    }

    @Override
    public EmployeeData delete(String id) {
        return employeeRepository.delete(id);
    }
    @Override
    public EmployeeData getByEmail(String email) {
        return employeeRepository.getByEmail(email);
    }
}
