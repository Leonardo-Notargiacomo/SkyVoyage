package io.github.fontysvenlo.ais.restapi;

import io.github.fontysvenlo.ais.businesslogic.api.EmployeeManager;
import io.github.fontysvenlo.ais.businesslogic.api.ValidatorInterface;
import io.github.fontysvenlo.ais.datarecords.EmployeeData;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;

import java.util.Map;

/**
 * This class is responsible for handling the requests for the customer resource.
 */
class EmployeeResource implements CrudHandler {
    final private EmployeeManager employeeManager;
    /**
     * Initializes the controller with the business logic.
     */
    EmployeeResource(EmployeeManager employeeManager) {
        this.employeeManager = employeeManager;
    }

    /**
     * Adds a customer to the storage.
     * - If the customer data is null, the status is set to 400 (Bad Request).
     * - Otherwise, the status is set to 201 (Created) and the added customer is returned as JSON.
     */
    @Override
    public void create(Context context) {
        try {
            EmployeeData employeeData = context.bodyAsClass(EmployeeData.class);
            if (employeeData == null) {
                context.status(400);
                return;
            }
            context.status(201);
            context.json(employeeManager.add(employeeData));
        } catch (IllegalArgumentException e) {
            context.status(400);
            context.json(Map.of("error", e.getMessage()));
        }
    }
    /**
     * Retrieves all customers from the storage.
     * - The status is set to 200 (OK) and the list of customers is returned as JSON.
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