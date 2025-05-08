package io.github.fontysvenlo.ais.restapi;

import io.github.fontysvenlo.ais.businesslogic.api.FlightManager;
import io.github.fontysvenlo.ais.businesslogic.api.PriceManager;
import io.github.fontysvenlo.ais.datarecords.PricePerKmData;
import io.javalin.http.Context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PriceResourceTest {

    private final Context context = mock(Context.class);
    private PriceManager priceManager;
    private FlightManager flightManager;
    private PriceResource priceResource;

    @BeforeEach
    public void setup() {

        priceManager = mock(PriceManager.class);
        flightManager = mock(FlightManager.class);

        when(context.status(anyInt())).thenReturn(context);
        priceResource = new PriceResource(priceManager, flightManager);
    }

    @Test
    public void testSetPrice() {

        when(priceManager.getPrice()).thenReturn(15);
        when(context.queryParam("price")).thenReturn("15");
        
        // When the actor submits a new price
        priceResource.create(context);
        
        // Then the price should be updated
        ArgumentCaptor<PricePerKmData> priceCaptor = ArgumentCaptor.forClass(PricePerKmData.class);
        verify(priceManager).setPrice(priceCaptor.capture());
        
        // Verify the correct price was set
        assertThat(priceCaptor.getValue().price()).isEqualTo(15);
        
        // Verify the response status and message
        verify(context).status(201);
        verify(context).json(any(PricePerKmData.class));
    }
    
    @Test
    public void testSetPriceInvalid() {

        when(priceManager.getPrice()).thenReturn(15); 
        when(context.queryParam("price")).thenReturn("-15");
        
        // When the actor submits an invalid price
        priceResource.create(context);
        
        // Then the system should reject the price
        verify(priceManager, never()).setPrice(any());
        
        // Verify error response
        verify(context).status(400);
        
        // Capture and check the error message
        ArgumentCaptor<Map<String, String>> jsonCaptor = ArgumentCaptor.forClass(Map.class);
        verify(context).json(jsonCaptor.capture());
        
        Map<String, String> response = jsonCaptor.getValue();
        assertThat(response).containsKey("error");
        assertThat(response.get("error")).isEqualTo("Price cannot be negative");
    }
    
    @Test
    public void testInvalidPriceFormat() {

        when(context.queryParam("price")).thenReturn("abc");
        
        // When the actor submits an invalid price format
        priceResource.create(context);
        
        // Then the system should reject the price
        verify(priceManager, never()).setPrice(any());
        
        // Verify error response
        verify(context).status(400);
        
        // Capture and check the error message
        ArgumentCaptor<Map<String, String>> jsonCaptor = ArgumentCaptor.forClass(Map.class);
        verify(context).json(jsonCaptor.capture());
        
        Map<String, String> response = jsonCaptor.getValue();
        assertThat(response).containsKey("error");
        assertThat(response.get("error")).isEqualTo("Invalid price format");
    }

    @Test
    public void testZeroPrice() {
        // Given a zero price
        when(context.queryParam("price")).thenReturn("0");
        
        // When the actor submits a zero price
        priceResource.create(context);
        
        // Then the system should accept it (since it's not negative)
        ArgumentCaptor<PricePerKmData> priceCaptor = ArgumentCaptor.forClass(PricePerKmData.class);
        verify(priceManager).setPrice(priceCaptor.capture());
        
        // Verify the correct price was set
        assertThat(priceCaptor.getValue().price()).isEqualTo(0);
        
        // Verify the response status
        verify(context).status(201);
    }
} 