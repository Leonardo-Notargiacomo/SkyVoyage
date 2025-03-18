package io.github.fontysvenlo.ais.restapi;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.fontysvenlo.ais.businesslogic.api.EmployeeManager;
import io.github.fontysvenlo.ais.datarecords.EmployeeData;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;

/**
 * This class is responsible for handling the requests for the employee
 * resource.
 */
class EmployeeResource implements CrudHandler {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeResource.class);
    final private EmployeeManager employeeManager;

    /**
     * Initializes the controller with the business logic.
     */
    EmployeeResource(EmployeeManager employeeManager) {
        this.employeeManager = employeeManager;
    }

    /**
     * Adds an employee to the storage. - If the employee data is null, the
     * status is set to 400 (Bad Request). - Otherwise, the status is set to 201
     * (Created) and the added employee is returned as JSON.
     */
    @Override
    public void create(Context context) {
        try {
            EmployeeData employeeData = context.bodyAsClass(EmployeeData.class);
            if (employeeData == null) {
                context.status(400);
                logger.error("Received null employee data");
                context.json(Map.of("error", "Invalid employee data format"));
                return;
            }
            logger.info("Received employee data: {}", employeeData);
            EmployeeData addedEmployee = employeeManager.add(employeeData);
            context.status(201);
            context.json(addedEmployee);
        } catch (IllegalArgumentException e) {
            context.status(400);
            logger.error("Error adding employee: {}", e.getMessage());
            context.json(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            context.status(500);
            logger.error("Unexpected error: {}", e.getMessage());
            context.json(Map.of("error", "Unexpected error occurred"));
        }
    }

    /**
     * Retrieves all employees from the storage. - The status is set to 200 (OK)
     * and the list of employees is returned as JSON.
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
    public void update(Context context, String employeeId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
