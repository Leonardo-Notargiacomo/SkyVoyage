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
        // Arrange
        when(employeeManager.list()).thenReturn(new ArrayList<>());

        // Act
        employeeResource.getAll(context);

        // Assert
        verify(context).status(200);
    }

    @Test
    public void testPostEmployees201() {
        // Arrange
        EmployeeData employeeData = new EmployeeData(0, "John", "Doe", "johndoe@company.com", "johndoe1230", "SalesManager");
        when(employeeManager.add(employeeData)).thenReturn(employeeData);
        when(context.bodyAsClass(EmployeeData.class)).thenReturn(employeeData);

        // Act
        employeeResource.create(context);

        // Assert
        verify(context).status(201);
        verify(context).json(employeeData);
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
