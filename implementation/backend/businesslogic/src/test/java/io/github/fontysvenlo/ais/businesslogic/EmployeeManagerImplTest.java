package io.github.fontysvenlo.ais.businesslogic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import io.github.fontysvenlo.ais.datarecords.EmployeeData;
import io.github.fontysvenlo.ais.persistence.api.EmployeeRepository;

public class EmployeeManagerImplTest {

    private EmployeeRepository mockRepository;
    private EmployeeManagerImpl employeeManager;

    @BeforeEach
    public void setUp() {
        mockRepository = mock(EmployeeRepository.class);
        employeeManager = new EmployeeManagerImpl(mockRepository);
    }

    @Test
    public void testPasswordIsHashed() {
        // Arrange
        String plainPassword = "SecurePassword123!";
        EmployeeData employeeData = new EmployeeData(
            0, "John", "Doe", "john.doe@example.com", plainPassword, "SalesManager"
        );
        
        // Set up the mock to return the employee with the hashed password
        when(mockRepository.add(any(EmployeeData.class))).thenAnswer(invocation -> {
            EmployeeData passedEmployee = invocation.getArgument(0);
            return new EmployeeData(
                1, 
                passedEmployee.Firstname(), 
                passedEmployee.Lastname(), 
                passedEmployee.Email(), 
                passedEmployee.Password(), 
                passedEmployee.Type()
            );
        });

        // Act
        EmployeeData result = employeeManager.add(employeeData);

        // Assert
        // 1. Verify password is not stored in plain text
        assertNotEquals(plainPassword, result.Password());
        
        // 2. Verify that the password being passed to repository is hashed
        // Use ArgumentCaptor to capture what was passed to the repository
        var argumentCaptor = org.mockito.ArgumentCaptor.forClass(EmployeeData.class);
        verify(mockRepository).add(argumentCaptor.capture());
        
        EmployeeData capturedEmployee = argumentCaptor.getValue();
        
        // 3. A BCrypt hash should start with "$2a$", "$2b$", or "$2y$"
        assertTrue(
            capturedEmployee.Password().startsWith("$2a$") ||
            capturedEmployee.Password().startsWith("$2b$") ||
            capturedEmployee.Password().startsWith("$2y$"),
            "Password should be hashed with BCrypt"
        );
        
        // 4. Verify we can validate the original password against the hash
        assertTrue(BCrypt.checkpw(plainPassword, capturedEmployee.Password()));
    }
}
