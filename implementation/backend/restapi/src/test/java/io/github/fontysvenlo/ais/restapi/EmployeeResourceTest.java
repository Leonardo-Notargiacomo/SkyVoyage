package io.github.fontysvenlo.ais.restapi;

import io.github.fontysvenlo.ais.businesslogic.api.BusinessLogic;
import io.github.fontysvenlo.ais.businesslogic.api.CustomerManager;
import io.github.fontysvenlo.ais.businesslogic.api.EmployeeManager;
import io.github.fontysvenlo.ais.datarecords.CustomerData;
import io.github.fontysvenlo.ais.datarecords.EmployeeData;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;

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

        // When we call the get all function to retreive the employees
        employeeResource.getAll(context);

        // Then we should get the context with a status 200
        verify(context).status(200);
    }

    @Test
    public void testPostEmployees201() {
        // Given we have an employee
        EmployeeData employeeData = new EmployeeData(0, "John", "Doe", "johndoe@company.com", "johndoe1230", "SalesManager");
        when(employeeManager.add(employeeData)).thenReturn(employeeData);
        when(context.bodyAsClass(EmployeeData.class)).thenReturn(employeeData);

        // When we call the create function to add the employee
        employeeResource.create(context);

        // Then we should get the context with a status 201 and the employee data
        verify(context).status(201);
        verify(context).json(employeeData);
    }

    @Test
    public void testPostEmployeesNull400() {
        // Given we have a null employee
        when(context.body()).thenReturn(null);

        // When we call the create function to add the employee
        employeeResource.create(context);

        // Then we should get the context with a status 400
        verify(context).status(400);
    }

    @Test
    public void testDeleteEmployees() {
        // Given we have an employee
        EmployeeData employeeData = new EmployeeData(12, "John", "Doe", "johndoe@company.com", "johndoe1230", "SalesManager");
        when(employeeManager.add(employeeData)).thenReturn(employeeData);
        when(context.bodyAsClass(EmployeeData.class)).thenReturn(employeeData);
        String employeeId = "1";

        // When we call the delete function to delete the employee
        employeeResource.delete(context, employeeId);

        // Then we should get the context with a status 405
        verify(context).status(204);
    }

//    @Test
//    public void testGetAllCustomers200() {
//        // Arrange
//        when(customerManager.list()).thenReturn(new ArrayList<>());
//
//        // Act
//        customerResource.getAll(context);
//
//        // Assert
//        verify(context).status(200);
//    }
//
//    @Test
//    public void testPostCustomers201() {
//        // Arrange
//        CustomerData customerData = new CustomerData(0, "John", "Doe", LocalDate.of(2025, 1, 1));
//        when(customerManager.add(customerData)).thenReturn(customerData);
//        when(context.bodyAsClass(CustomerData.class)).thenReturn(customerData);
//
//        // Act
//        customerResource.create(context);
//
//        // Assert
//        verify(context).status(201);
//        verify(context).json(customerData);
//    }
//
//    @Test
//    public void testPostCustomersNull400() {
//        // Arrange
//        when(context.body()).thenReturn(null);
//
//        // Act
//        customerResource.create(context);
//
//        // Assert
//        verify(context).status(400);
//    }
}
