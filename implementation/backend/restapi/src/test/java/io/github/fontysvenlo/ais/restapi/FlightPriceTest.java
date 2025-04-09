package io.github.fontysvenlo.ais.restapi;

import io.github.fontysvenlo.ais.businesslogic.api.FlightManager;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import java.util.Map;

public class FlightPriceTest {
    private final Context context = mock(Context.class);
    private AviationStackClient aviationStackClient;
    private FlightResource flightResource;

    @BeforeEach
    public void setup() {
        aviationStackClient = mock(AviationStackClient.class);
        FlightManager flightManager = mock(FlightManager.class);
        flightResource = new FlightResource(flightManager, aviationStackClient);
        when(context.status(anyInt())).thenReturn(context);
    }

    @Test
    public void testUpdatePriceSuccessful() {
        // Arrange
        Map<String, Integer> priceUpdate = Map.of("price", 15);
        when(context.bodyAsClass(Map.class)).thenReturn(priceUpdate);

        // Act
        flightResource.updatePrice(context);

        // Assert
        verify(aviationStackClient).updatePrice(15);
        verify(context).json(Map.of("price", 15));
    }

    @Test
    public void testUpdatePriceInvalidNegative() {
        // Arrange
        Map<String, Integer> invalidPrice = Map.of("price", -15);
        when(context.bodyAsClass(Map.class)).thenReturn(invalidPrice);

        // Act
        flightResource.updatePrice(context);

        // Assert
        verify(context).json(Map.of("error", "Price per kilometer cannot be negative"));
        verify(aviationStackClient, never()).updatePrice(anyInt());
    }

    @Test
    public void testUpdatePriceInvalidFormat() {
        // Arrange
        when(context.bodyAsClass(Map.class)).thenReturn(Map.of("wrongKey", 15));

        // Act
        flightResource.updatePrice(context);

        // Assert
        verify(context).json(Map.of("error", "Invalid price format"));
        verify(aviationStackClient, never()).updatePrice(anyInt());
    }
}