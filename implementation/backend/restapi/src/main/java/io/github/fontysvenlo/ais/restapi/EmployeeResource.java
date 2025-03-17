package io.github.fontysvenlo.ais.restapi;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.fontysvenlo.ais.businesslogic.Validator;
import io.github.fontysvenlo.ais.businesslogic.api.EmployeeManager;
import io.github.fontysvenlo.ais.businesslogic.api.ValidatorInterface;
import io.github.fontysvenlo.ais.datarecords.EmployeeData;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;

/**
 * This class is responsible for handling the requests for the customer
 * resource.
 */
class EmployeeResource implements CrudHandler {

    private static final Logger logger = Logger.getLogger(EmployeeResource.class.getName());
    final private EmployeeManager employeeManager;
    final private ValidatorInterface validator;

    /**
     * Initializes the controller with the business logic.
     */
    EmployeeResource(EmployeeManager employeeManager) {
        this.employeeManager = employeeManager;
        this.validator = new Validator();
    }

    /**
     * Validates all employee fields and returns a map of validation errors.
     *
     * @param employee The employee data to validate
     * @return A map of field names to error messages, empty if validation
     * passes
     */
    private Map<String, String> validateEmployee(EmployeeData employee) {
        Map<String, String> errors = new HashMap<>();

        // Validate first name
        if (employee.Firstname() == null || !validator.isValidName(employee.Firstname())) {
            errors.put("firstname", "First name must be at least 2 characters and contain only letters");
        }

        // Validate last name
        if (employee.Lastname() == null || !validator.isValidName(employee.Lastname())) {
            errors.put("lastname", "Last name must be at least 2 characters and contain only letters");
        }

        // Validate email
        if (employee.Email() == null || !validator.isValidEmail(employee.Email())) {
            errors.put("email", "Please enter a valid email address");
        }

        // Validate password
        if (employee.Password() == null || !validator.isValidPassword(employee.Password())) {
            errors.put("password", "Password must be at least 8 characters and include uppercase, lowercase, digit, and special character");
        }

        // Validate employee type
        if (employee.Type() == null || employee.Type().trim().isEmpty()) {
            errors.put("type", "Employee type is required");
        }

        return errors;
    }

    /**
     * Adds a customer to the storage.
     */
    @Override
    public void create(Context context) {
        try {
            logger.info("Received create employee request");
            String body = context.body();
            logger.info("Request body: " + body);

            if (body == null || body.isEmpty()) {
                logger.warning("Request body is null or empty");
                context.status(400);
                context.json(Map.of("error", "Request body cannot be empty"));
                return;
            }

            EmployeeData employeeData = context.bodyAsClass(EmployeeData.class);
            logger.info("Parsed employee data: " + employeeData);

            if (employeeData == null) {
                logger.warning("Failed to parse employee data from request");
                context.status(400);
                context.json(Map.of("error", "Invalid employee data format"));
                return;
            }

            // Validate all employee fields
            Map<String, String> validationErrors = validateEmployee(employeeData);

            // If there are validation errors, return them with 400 status
            if (!validationErrors.isEmpty()) {
                logger.warning("Validation errors: " + validationErrors);
                context.status(400);
                context.json(Map.of(
                        "error", "Validation failed",
                        "validationErrors", validationErrors
                ));
                return;
            }

            // Log individual fields for debugging
            logger.info(String.format(
                    "Employee fields - Firstname: %s, Lastname: %s, Email: %s, Password: [REDACTED], Type: %s",
                    employeeData.Firstname(),
                    employeeData.Lastname(),
                    employeeData.Email(),
                    employeeData.Type()
            ));

            EmployeeData addedEmployee = employeeManager.add(employeeData);
            context.status(201);
            context.json(addedEmployee);
            logger.info("Employee created successfully with ID: " + addedEmployee.id());

        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Invalid employee data", e);
            context.status(400);
            context.json(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error creating employee", e);
            context.status(500);
            context.json(Map.of("error", "Server error: " + e.getMessage()));
        }
    }

    /**
     * Retrieves all customers from the storage. - The status is set to 200 (OK)
     * and the list of customers is returned as JSON.
     */
    @Override
    public void getAll(Context context) {
        context.status(200);
        context.json(employeeManager.list());
    }

    @Override
    public void delete(Context context, String employeeId) {
        try {
            employeeManager.delete(employeeId);
            context.status(204); // No Content - successful deletion
        } catch (Exception e) {
            context.status(404); // Not Found
            context.json(Map.of("error", "Employee not found"));
        }
    }

    @Override
    public void getOne(Context context, String employeeId) {
        EmployeeData employeeData = employeeManager.getOne(employeeId);
        if (employeeData != null) {
            context.status(200);
            context.json(employeeData);
        } else {
            context.status(404);
            context.json(Map.of("error", "Employee not found"));
        }
    }

    @Override
    public void update(Context context, String customerId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
