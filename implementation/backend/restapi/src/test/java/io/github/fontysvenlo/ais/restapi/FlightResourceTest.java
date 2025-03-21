package io.github.fontysvenlo.ais.restapi;

import io.github.fontysvenlo.ais.businesslogic.api.BusinessLogic;
import io.github.fontysvenlo.ais.businesslogic.api.FlightManager;
import io.github.fontysvenlo.ais.datarecords.FlightData;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class FlightResourceTest {

    private final Context context = mock(Context.class);

    private BusinessLogic businessLogic;
    private FlightManager flightManager;
    private AviationStackClient aviationStackClient;
    private FlightResource flightResource;

    @BeforeEach
    public void setup() {
        // Setup the mocks
        businessLogic = mock(BusinessLogic.class);
        flightManager = mock(FlightManager.class);
        aviationStackClient = mock(AviationStackClient.class);
        when(businessLogic.getFlightManager()).thenReturn(flightManager);

        // Create the resource with the mocks
        flightResource = new FlightResource(flightManager, aviationStackClient);
    }

    @Test
    public void testGetAllFlights() {
        // Given we have flights in the database
        LocalDateTime departureTime = LocalDateTime.now();
        LocalDateTime arrivalTime = departureTime.plusHours(2);

        FlightData flight1 = new FlightData("AB123", "Berlin Airport", "BER", "1", "A1",
                departureTime, 0, "London Heathrow", "LHR", "2", "B2",
                arrivalTime, 0, 120);
        FlightData flight2 = new FlightData("CD456", "Paris Charles de Gaulle", "CDG", "3", "C3",
                departureTime, 10, "Amsterdam Schiphol", "AMS", "4", "D4",
                arrivalTime, 5, 90);

        List<FlightData> mockFlights = Arrays.asList(flight1, flight2);
        when(flightManager.list()).thenReturn(mockFlights);

        // When we call getAll
        flightResource.getAll(context);

        // Then the response should contain the flights
        verify(context).status(200);
        verify(context).json(mockFlights);
        // Verify we don't try to fetch from API
        verify(aviationStackClient, never()).getAllFlights();
    }

    @Test
    public void testGetAllFlightsEmptyFetchFromAPI() {
        // Given we have no flights in the database but API has flights
        when(flightManager.list()).thenReturn(new ArrayList<>());

        LocalDateTime departureTime = LocalDateTime.now();
        LocalDateTime arrivalTime = departureTime.plusHours(2);

        FlightData apiFlight = new FlightData("AB123", "Berlin Airport", "BER", "1", "A1",
                departureTime, 0, "London Heathrow", "LHR", "2", "B2",
                arrivalTime, 0, 120);

        List<FlightData> apiFlights = List.of(apiFlight);
        when(aviationStackClient.getAllFlights()).thenReturn(apiFlights);

        // When we call getAll
        flightResource.getAll(context);

        // Then we should fetch from API and save to database
        verify(aviationStackClient).getAllFlights();
        verify(flightManager).add(apiFlight);
        verify(context).status(200);
        verify(context).json(apiFlights);
    }

    @Test
    public void testGetAllFlightsWithSearchParams() {
        // Given we have search parameters
        String airline = "LH";
        String flightNumber = "123";
        String departureIata = "FRA";
        String arrivalIata = "JFK";

        // Mock the request parameters
        when(context.queryParam("airline")).thenReturn(airline);
        when(context.queryParam("flight_number")).thenReturn(flightNumber);
        when(context.queryParam("departure")).thenReturn(departureIata);
        when(context.queryParam("arrival")).thenReturn(arrivalIata);

        // Mock the search results
        FlightData searchResult = new FlightData("LH123", "Frankfurt Airport", "FRA", "1", "A1",
                LocalDateTime.now(), 0, "John F. Kennedy", "JFK", "2", "B2",
                LocalDateTime.now().plusHours(9), 0, 540);

        List<FlightData> searchResults = List.of(searchResult);
        when(flightManager.search(airline, flightNumber, departureIata, arrivalIata))
                .thenReturn(searchResults);

        // When we call getAll
        flightResource.getAll(context);

        // Then we should search and return results
        verify(flightManager).search(airline, flightNumber, departureIata, arrivalIata);
        verify(aviationStackClient, never()).getAllFlights();
        verify(context).status(200);
        verify(context).json(searchResults);
    }

    @Test
    public void testGetOneExistingFlight() {
        // Given we have a flight with the specified ID
        String flightId = "AB123";
        FlightData flight = new FlightData(flightId, "Berlin Airport", "BER", "1", "A1",
                LocalDateTime.now(), 0, "London Heathrow", "LHR", "2", "B2",
                LocalDateTime.now().plusHours(2), 0, 120);

        when(flightManager.getOne(flightId)).thenReturn(flight);

        // When we call getOne
        flightResource.getOne(context, flightId);

        // Then we should get the flight
        verify(context).status(200);
        verify(context).json(flight);
    }

    @Test
    public void testGetOneNonExistingFlight() {
        // Given we don't have a flight with the specified ID
        String flightId = "NONEXISTENT";
        when(flightManager.getOne(flightId)).thenReturn(null);

        // When we call getOne
        flightResource.getOne(context, flightId);

        // Then we should get a 404
        verify(context).status(404);

        // Capture the error response
        ArgumentCaptor<Map<String, String>> jsonCaptor = ArgumentCaptor.forClass(Map.class);
        verify(context).json(jsonCaptor.capture());

        // Verify the error message
        Map<String, String> response = jsonCaptor.getValue();
        assertThat(response).containsKey("error");
        assertThat(response.get("error")).isEqualTo("Flight not found");
    }

    @Test
    public void testCreateFlight() {
        // Given we have valid flight data
        FlightData flightData = new FlightData("AB123", "Berlin Airport", "BER", "1", "A1",
                LocalDateTime.now(), 0, "London Heathrow", "LHR", "2", "B2",
                LocalDateTime.now().plusHours(2), 0, 120);

        when(context.bodyAsClass(FlightData.class)).thenReturn(flightData);
        when(flightManager.add(flightData)).thenReturn(flightData);

        // When we call create
        flightResource.create(context);

        // Then the flight should be added
        verify(flightManager).add(flightData);
        verify(context).status(201);
        verify(context).json(flightData);
    }

    @Test
    public void testCreateFlightNull() {
        // Given we have null flight data
        when(context.bodyAsClass(FlightData.class)).thenReturn(null);

        // When we call create
        flightResource.create(context);

        // Then we should get a 400
        verify(context).status(400);

        // Capture the error response
        ArgumentCaptor<Map<String, String>> jsonCaptor = ArgumentCaptor.forClass(Map.class);
        verify(context).json(jsonCaptor.capture());

        // Verify the error message
        Map<String, String> response = jsonCaptor.getValue();
        assertThat(response).containsKey("error");
        assertThat(response.get("error")).isEqualTo("Invalid flight data format");
    }

    @Test
    public void testRefreshFlights() {
        // Given the API returns flights
        LocalDateTime departureTime = LocalDateTime.now();
        LocalDateTime arrivalTime = departureTime.plusHours(2);

        FlightData flight1 = new FlightData("AB123", "Berlin Airport", "BER", "1", "A1",
                departureTime, 0, "London Heathrow", "LHR", "2", "B2",
                arrivalTime, 0, 120);
        FlightData flight2 = new FlightData("CD456", "Paris Charles de Gaulle", "CDG", "3", "C3",
                departureTime, 10, "Amsterdam Schiphol", "AMS", "4", "D4",
                arrivalTime, 5, 90);

        List<FlightData> apiFlights = Arrays.asList(flight1, flight2);
        when(aviationStackClient.getAllFlights()).thenReturn(apiFlights);

        // When we call refreshFlights
        flightResource.refreshFlights(context);

        // Then flights should be fetched from API and saved
        verify(aviationStackClient).getAllFlights();
        verify(flightManager).add(flight1);
        verify(flightManager).add(flight2);
        verify(context).status(200);

        // Capture the response
        ArgumentCaptor<Map<String, Object>> jsonCaptor = ArgumentCaptor.forClass(Map.class);
        verify(context).json(jsonCaptor.capture());

        // Verify the response
        Map<String, Object> response = jsonCaptor.getValue();
        assertThat(response).containsKey("message");
        assertThat(response).containsKey("count");
        assertThat(response.get("count")).isEqualTo(2);
    }

    @Test
    public void testRefreshFlightsNoFlightsFound() {
        // Given the API returns no flights
        when(aviationStackClient.getAllFlights()).thenReturn(new ArrayList<>());

        // When we call refreshFlights
        flightResource.refreshFlights(context);

        // Then we should get a 404
        verify(context).status(404);

        // Capture the response
        ArgumentCaptor<Map<String, String>> jsonCaptor = ArgumentCaptor.forClass(Map.class);
        verify(context).json(jsonCaptor.capture());

        // Verify the response
        Map<String, String> response = jsonCaptor.getValue();
        assertThat(response).containsKey("message");
        assertThat(response.get("message")).isEqualTo("No flights found from API");
    }

    @Test
    public void testUpdateFlightMethodNotAllowed() {
        // When we call update
        flightResource.update(context, "AB123");

        // Then we should get a 405 (Method Not Allowed)
        verify(context).status(405);

        // Capture the error response
        ArgumentCaptor<Map<String, String>> jsonCaptor = ArgumentCaptor.forClass(Map.class);
        verify(context).json(jsonCaptor.capture());

        // Verify the error message
        Map<String, String> response = jsonCaptor.getValue();
        assertThat(response).containsKey("error");
        assertThat(response.get("error")).isEqualTo("Updating flights is not supported");
    }

    @Test
    public void testDeleteFlightMethodNotAllowed() {
        // When we call delete
        flightResource.delete(context, "AB123");

        // Then we should get a 405 (Method Not Allowed)
        verify(context).status(405);

        // Capture the error response
        ArgumentCaptor<Map<String, String>> jsonCaptor = ArgumentCaptor.forClass(Map.class);
        verify(context).json(jsonCaptor.capture());

        // Verify the error message
        Map<String, String> response = jsonCaptor.getValue();
        assertThat(response).containsKey("error");
        assertThat(response.get("error")).isEqualTo("Deleting flights is not supported");
    }
}
