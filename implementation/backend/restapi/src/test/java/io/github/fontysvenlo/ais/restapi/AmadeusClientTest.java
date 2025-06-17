package io.github.fontysvenlo.ais.restapi;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import io.github.fontysvenlo.ais.businesslogic.api.PriceManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ExtendWith(MockitoExtension.class)
class AmadeusClientTest {

    @Mock
    private PriceManager priceManager;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    private AmadeusClient amadeusClient;
    private ObjectMapper objectMapper;
    
    private static final String TEST_CLIENT_ID = "test_client_id";
    private static final String TEST_CLIENT_SECRET = "test_client_secret";

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // Create a real ObjectMapper for JSON operations
        objectMapper = new ObjectMapper();
        
        // Create a test instance with mocked HttpClient
        amadeusClient = new AmadeusClient(TEST_CLIENT_ID, TEST_CLIENT_SECRET) {
            @Override
            protected HttpClient createHttpClient() {
                return httpClient;
            }
        };

        amadeusClient.setPriceManager(priceManager);
    }

    @Test
    void testAuthenticationFlow() throws IOException, InterruptedException {
        // Setup mock for authentication response
        ObjectNode tokenResponse = objectMapper.createObjectNode();
        tokenResponse.put("access_token", "mock_access_token");
        tokenResponse.put("expires_in", 1800);
        
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(tokenResponse.toString());
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);

        // Call a method that requires authentication
        Map<String, String> params = Map.of(
            "originLocationCode", "AMS",
            "destinationLocationCode", "NYC",
            "departureDate", "2023-12-01",
            "returnDate", "2023-12-10",
            "adults", "1"
        );

        // Setup mock for search response
        ObjectNode searchResponse = objectMapper.createObjectNode();
        searchResponse.set("data", objectMapper.createArrayNode());
        
        when(httpResponse.body()).thenReturn(searchResponse.toString());
        
        // Execute the method that should trigger authentication
        amadeusClient.searchFlightOffersWithParams(params);

        // Verify authentication request was made
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, atLeastOnce()).send(requestCaptor.capture(), any());
        
        // Verify the first call is to the auth endpoint
        HttpRequest firstRequest = requestCaptor.getAllValues().get(0);
        assertTrue(firstRequest.uri().toString().contains("/security/oauth2/token"));
    }

    @Test
    void testSearchFlights() throws IOException, InterruptedException {
        // Setup authentication success
        setupSuccessfulAuthentication();
        
        // Setup search response
        String mockFlightResponse = createMockFlightResponse();
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(mockFlightResponse);
        
        // Execute search
        Map<String, Object> result = amadeusClient.searchFlights("AMS", "NYC", "2023-12-01", "2023-12-10");
        
        // Verify result contains expected data
        assertNotNull(result);
        assertTrue(result.containsKey("data"));
        
        // Verify the request was sent
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, atLeast(2)).send(requestCaptor.capture(), any());
        
        // Verify the second request is to the flight search endpoint
        HttpRequest searchRequest = requestCaptor.getAllValues().get(1);
        assertTrue(searchRequest.uri().toString().contains("/shopping/flight-offers"));
        assertTrue(searchRequest.uri().toString().contains("originLocationCode=AMS"));
        assertTrue(searchRequest.uri().toString().contains("destinationLocationCode=NYC"));
    }

    @Test
    void testSearchFlightOffersWithParams() throws IOException, InterruptedException {
        // Setup authentication success
        setupSuccessfulAuthentication();
        
        // Setup search response
        String mockFlightResponse = createMockFlightResponse();
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(mockFlightResponse);
        
        // Execute search with params
        Map<String, String> params = Map.of(
            "originLocationCode", "AMS",
            "destinationLocationCode", "NYC",
            "departureDate", "2023-12-01",
            "returnDate", "2023-12-10",
            "adults", "1",
            "max", "5"
        );
        
        Map<String, Object> result = amadeusClient.searchFlightOffersWithParams(params);
        
        // Verify result
        assertNotNull(result);
        assertTrue(result.containsKey("data"));
        
        // Verify request
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, atLeast(2)).send(requestCaptor.capture(), any());
        
        // Verify the second request contains all parameters
        HttpRequest searchRequest = requestCaptor.getAllValues().get(1);
        String uri = searchRequest.uri().toString();
        assertTrue(uri.contains("max=5"));
    }

    @Test
    void testSearchWithQueryString() throws IOException, InterruptedException {
        // Setup authentication success
        setupSuccessfulAuthentication();
        
        // Setup search response
        String mockFlightResponse = createMockFlightResponse();
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(mockFlightResponse);
        
        // Execute search with query string
        String queryString = "originLocationCode=AMS&destinationLocationCode=NYC&departureDate=2023-12-01";
        Map<String, Object> result = amadeusClient.searchFlightOffersWithQueryString(queryString);
        
        // Verify result
        assertNotNull(result);
        assertTrue(result.containsKey("data"));
        
        // Verify request
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, atLeast(2)).send(requestCaptor.capture(), any());
        
        // Verify the second request has the query string
        HttpRequest searchRequest = requestCaptor.getAllValues().get(1);
        assertTrue(searchRequest.uri().toString().endsWith(queryString));
    }

    @Test
    void testHandleAuthenticationErrorResponse() throws IOException, InterruptedException {
        // Setup failed authentication response
        when(httpResponse.statusCode()).thenReturn(400);
        when(httpResponse.body()).thenReturn("{\"errors\":[{\"code\":\"invalid_parameter\",\"title\":\"Invalid parameter\"}]}");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);

        // Execute search that should trigger authentication and fail
        try {
            amadeusClient.searchFlights("AMS", "NYC", "2023-12-01", "2023-12-10");
            fail("Expected IOException to be thrown");
        } catch (IOException e) {
            // Verify the error message contains expected information
            assertTrue(e.getMessage().contains("Failed to get access token"));
            assertTrue(e.getMessage().contains("400"));
            assertTrue(e.getMessage().contains("invalid_parameter"));
        }
    }

    private void setupSuccessfulAuthentication() throws IOException, InterruptedException {
        ObjectNode tokenResponse = objectMapper.createObjectNode();
        tokenResponse.put("access_token", "mock_access_token");
        tokenResponse.put("expires_in", 1800);
        
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(tokenResponse.toString());
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
    }

    private String createMockFlightResponse() {
        ObjectNode response = objectMapper.createObjectNode();
        ObjectNode meta = objectMapper.createObjectNode();
        meta.put("count", 2);
        response.set("meta", meta);
        
        // Create a sample flight offer
        ObjectNode flight = objectMapper.createObjectNode();
        flight.put("id", "1");
        flight.put("source", "GDS");
        
        ObjectNode price = objectMapper.createObjectNode();
        price.put("currency", "EUR");
        price.put("total", "125.45");
        price.put("grandTotal", "125.45");
        flight.set("price", price);
        
        // Create itinerary
        ObjectNode itinerary = objectMapper.createObjectNode();
        itinerary.put("duration", "PT3H20M");
        
        ObjectNode segment = objectMapper.createObjectNode();
        segment.put("id", "1");
        segment.put("duration", "PT3H20M");
        segment.put("number", "KL123");
        segment.put("carrierCode", "KL");
        
        ObjectNode departure = objectMapper.createObjectNode();
        departure.put("iataCode", "AMS");
        departure.put("terminal", "2");
        departure.put("at", "2023-12-01T09:00:00");
        segment.set("departure", departure);
        
        ObjectNode arrival = objectMapper.createObjectNode();
        arrival.put("iataCode", "NYC");
        arrival.put("terminal", "4");
        arrival.put("at", "2023-12-01T12:20:00");
        segment.set("arrival", arrival);
        
        // Add segment to segments array
        ObjectNode segments = objectMapper.createObjectNode();
        segments.putArray("segments").add(segment);
        
        // Add segments to itinerary
        flight.putArray("itineraries").add(itinerary);
        
        // Add flight to data array
        response.putArray("data").add(flight);
        
        // Add dictionaries
        ObjectNode dictionaries = objectMapper.createObjectNode();
        ObjectNode carriers = objectMapper.createObjectNode();
        carriers.put("KL", "KLM Royal Dutch Airlines");
        dictionaries.set("carriers", carriers);
        response.set("dictionaries", dictionaries);
        
        return response.toString();
    }
}
