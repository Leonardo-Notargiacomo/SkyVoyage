package io.github.fontysvenlo.ais.restapi;

import io.github.fontysvenlo.ais.businesslogic.api.BusinessLogic;
import io.github.fontysvenlo.ais.businesslogic.api.EmployeeManager;
import io.github.fontysvenlo.ais.datarecords.EmployeeData;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.github.fontysvenlo.ais.businesslogic.EmployeeManagerImpl;
import io.github.fontysvenlo.ais.persistence.api.EmployeeRepository;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static org.mockito.Mockito.*;

public class EmployeeResourceTest {
    private final Context context = mock(Context.class);

    private BusinessLogic businessLogic;
    private EmployeeManager employeeManager;
    private EmployeeResource employeeResource;

    @BeforeEach
    public void setup() {
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        employeeManager = new EmployeeManagerImpl(employeeRepository);
        businessLogic = mock(BusinessLogic.class);
        when(businessLogic.getEmployeeManager()).thenReturn(employeeManager);
        employeeResource = new EmployeeResource(employeeManager);
    }

    @Test
    public void testGetAllEmployees() {
        // Given we have a list with employees
        when(employeeManager.list()).thenReturn(new ArrayList<>());

        // When we call the get all function to retrieve the employees
        employeeResource.getAll(context);

        // Then we should get the context with a status 200
        verify(context).status(200);
    }

    @Test
    public void testCreateEmployee() {
        // Given we have an employee with valid data
        EmployeeData employeeData = new EmployeeData(1, "John", "Smith", "john.smith@company.com", "Johndoe1230@", "SalesManager");
        when(context.bodyAsClass(EmployeeData.class)).thenReturn(employeeData);

        // When we call the create function to add the employee
        employeeResource.create(context);

        // Then we should get the context with a status 201 and the employee data
        verify(context).status(201);
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
    public void testCreateEmployeeInvalidInformation() {
        // Given we have an employee with an invalid email
        EmployeeData employeeData = new EmployeeData(0, "John", "Smith", "john.smith", "john12345", "SalesManager");
        when(context.bodyAsClass(EmployeeData.class)).thenReturn(employeeData);

        // When we call the create function to add the employee
        employeeResource.create(context);

        // Then we should get the context with a status 400
        verify(context).status(400);
        // And we should get an error message
        verify(context).json(argThat(arg ->
                arg instanceof Map && ((Map<?,?>)arg).containsKey("error")
        ));

        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(context).json(captor.capture());
        System.out.println(captor.getValue().get("error"));
    }

    @Test
    public void testGetEmployee() {
        // Given we have a test employee in our system
        String employeeId = "1";
        EmployeeData testEmployee = new EmployeeData(1, "John", "Smith", "john.smith@company.com", "john12345", "SalesManager");
        when(employeeManager.getOne(employeeId)).thenReturn(testEmployee);

        // When we call the get function to retrieve the employee
        employeeResource.getOne(context, employeeId);

        // Then we should get the context with a status 200 and the employee data
        verify(context).status(200);
        verify(context).json(testEmployee);
    }

    @Test
    public void testDeleteEmployee() {
        // Given we have an existing employee ID
        String employeeId = "1";

        // When we call the delete function
        employeeResource.delete(context, employeeId);

        // Then we should get the context with status 204 (No Content)
        verify(context).status(204);
        verify(context, never()).json(any());
    }

    @Test
    public void testGetAll() {
        // Given we have multiple employees with valid data
        EmployeeData employeeData1 = new EmployeeData(1, "John", "Smith", "john.smith@company.com", "johndoe1230", "SalesManager");
        EmployeeData employeeData2 = new EmployeeData(2, "Jane", "Doe", "jane.doe@company.com", "janedoe1230", "HRManager");

        // Mock the employee manager to return the list of employees
        when(employeeManager.list()).thenReturn(Arrays.asList(employeeData1, employeeData2));

        // When we call the get all function to retrieve the employees
        employeeResource.getAll(context);

        // Then we should get the context with a status 200 and the list of employees
        verify(context).status(200);
        verify(context).json(Arrays.asList(employeeData1, employeeData2));
    }
}
