package io.github.fontysvenlo.ais.restapi;

import io.github.fontysvenlo.ais.businesslogic.api.LoginService;
import io.github.fontysvenlo.ais.datarecords.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private LoginService loginService;
    private AuthController authController;

    @BeforeEach
    void setUp() {
        loginService = mock(LoginService.class);
        authController = new AuthController(loginService);
    }

    @Test
    void testLoginSuccess() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        when(loginService.login(request.email(), request.password())).thenReturn(true);

        // Act
        ResponseEntity<String> response = authController.login(request);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Login successful");
        verify(loginService, times(1)).login(request.email(), request.password());
    }

    @Test
    void testLoginInvalidCredentials() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");
        when(loginService.login(request.email(), request.password())).thenReturn(false);

        // Act
        ResponseEntity<String> response = authController.login(request);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(401);
        assertThat(response.getBody()).isEqualTo("Invalid credentials");
        verify(loginService, times(1)).login(request.email(), request.password());
    }
}