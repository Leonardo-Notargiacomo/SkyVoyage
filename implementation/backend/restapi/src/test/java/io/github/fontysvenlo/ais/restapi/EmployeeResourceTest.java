package io.github.fontysvenlo.ais.restapi;

import io.github.fontysvenlo.ais.businesslogic.api.BusinessLogic;
import io.github.fontysvenlo.ais.businesslogic.api.EmployeeManager;
import io.github.fontysvenlo.ais.datarecords.EmployeeData;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class EmployeeResourceTest {
    private final Context context = mock(Context.class);

    private BusinessLogic businessLogic;
    private EmployeeManager employeeManager;
    private EmployeeResource employeeResource;

    @BeforeEach
    public void setup() {
        businessLogic = mock(BusinessLogic.class);
        employeeManager = mock(EmployeeManager.class);
        when(businessLogic.getEmployeeManager()).thenReturn(employeeManager);
        employeeResource = new EmployeeResource(employeeManager);
    }

    @Test
    public void testGetAllEmployees200() {
        // Given we have a list with employees
        when(employeeManager.list()).thenReturn(new ArrayList<>());

        // When we call the get all function to retrieve the employees
        employeeResource.getAll(context);

        // Then we should get the context with a status 200
        verify(context).status(200);
    }

    @Test
    public void testCreateEmployee() {
        // Given we have an employee
        EmployeeData employeeData = new EmployeeData(0, "John", "Smith", "john.smith@company.com", "johndoe1230", "SalesManager");
        when(employeeManager.add(employeeData)).thenReturn(employeeData);
        when(context.bodyAsClass(EmployeeData.class)).thenReturn(employeeData);

        // When we call the create function to add the employee
        employeeResource.create(context);

        // Then we should get the context with a status 201 and the employee data
        verify(context).status(201);
        verify(context).json(employeeData);
    }

    @Test
    public void testCreateEmployeeNull() {
        // Given we have a null employee
        when(context.body()).thenReturn(null);

        // When we call the create function to add the employee
        employeeResource.create(context);

        // Then we should get the context with a status 400
        verify(context).status(400);
    }

    @Test
    public void testDeleteEmployee() {
        // Given we have a test employee in our system
        String employeeId = "1";
        EmployeeData testEmployee = new EmployeeData(1, "John", "Smith", "john.smith@company.com", "johndoe1230", "SalesManager");
        ArrayList<EmployeeData> employees = new ArrayList<>();
        employees.add(testEmployee);

        // Set up mock to simulate actual deletion by removing from list
        doAnswer(invocation -> {
            String id = invocation.getArgument(0);
            employees.removeIf(emp -> String.valueOf(emp.id()).equals(id));
            return null;
        }).when(employeeManager).delete(employeeId);

        // When we call the delete function
        employeeResource.delete(context, employeeId);

        // Then we should verify deletion was attempted with correct ID
        verify(employeeManager).delete(employeeId);
        // And the employee should be removed from our collection
        assertTrue(employees.isEmpty(), "Employee should be deleted from collection");
        // And we should get the context with a status 204
        verify(context).status(204);
    }

    @Test
    public void testDeleteEmployeesNotFound() {
        // Given we have an employee ID that doesn't exist
        String employeeId = "999";
        // And deletion will throw an exception
        doThrow(new RuntimeException("Employee not found")).when(employeeManager).delete(employeeId);

        // When we call the delete function to delete the employee
        employeeResource.delete(context, employeeId);

        // Then we should get the context with a status 404
        verify(context).status(404);
        // And we should get an error message
        verify(context).json(argThat(arg ->
                arg instanceof Map && ((Map<?,?>)arg).containsKey("error")
        ));
    }
}
