package io.github.fontysvenlo.ais.restapi;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.fontysvenlo.ais.datarecords.FlightData;

/**
 * Client for interacting with the AviationStack API.
 */
public class AviationStackClient {

    private static final Logger logger = LoggerFactory.getLogger(AviationStackClient.class);
    private static final String API_BASE_URL = "http://api.aviationstack.com/v1";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new AviationStackClient.
     *
     * @param apiKey The API key for the AviationStack API
     */
    public AviationStackClient(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();

        logger.info("AviationStackClient initialized with API key: {}", apiKey);
    }

    /**
     * Gets all flights currently available from the API.
     *
     * @return A list of flight data
     */
    public List<FlightData> getAllFlights() {
        try {
            String url = API_BASE_URL + "/flights?access_key=" + apiKey;
            logger.info("Fetching flights from URL: {}", url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("API response status: {}", response.statusCode());

            if (response.statusCode() != 200) {
                logger.error("Failed to get flights from API: {}", response.body());
                return Collections.emptyList();
            }

            return parseFlightResponse(response.body());
        } catch (IOException | InterruptedException e) {
            logger.error("Error while fetching flights", e);
            return Collections.emptyList();
        }
    }

    /**
     * Searches for flights based on various criteria.
     *
     * @param airline IATA airline code
     * @param flightNumber Flight number
     * @param departureIata IATA code of departure airport
     * @param arrivalIata IATA code of arrival airport
     * @return A list of flight data matching the criteria
     */
    public List<FlightData> searchFlights(String airline, String flightNumber, String departureIata, String arrivalIata) {
        StringBuilder queryBuilder = new StringBuilder(API_BASE_URL + "/flights?access_key=" + apiKey);

        if (airline != null && !airline.isEmpty()) {
            queryBuilder.append("&airline_iata=").append(airline);
        }

        if (flightNumber != null && !flightNumber.isEmpty()) {
            queryBuilder.append("&flight_number=").append(flightNumber);
        }

        if (departureIata != null && !departureIata.isEmpty()) {
            queryBuilder.append("&dep_iata=").append(departureIata);
        }

        if (arrivalIata != null && !arrivalIata.isEmpty()) {
            queryBuilder.append("&arr_iata=").append(arrivalIata);
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(queryBuilder.toString()))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                logger.error("Failed to search flights from API: {}", response.body());
                return Collections.emptyList();
            }

            return parseFlightResponse(response.body());
        } catch (IOException | InterruptedException e) {
            logger.error("Error while searching flights", e);
            return Collections.emptyList();
        }
    }

    /**
     * Parses the JSON response from the AviationStack API into FlightData
     * objects.
     *
     * @param responseBody The JSON response body
     * @return A list of flight data
     * @throws IOException If there is an error parsing the JSON
     */
    private List<FlightData> parseFlightResponse(String responseBody) throws IOException {
        JsonNode rootNode = objectMapper.readTree(responseBody);
        JsonNode dataNode = rootNode.get("data");

        if (dataNode == null || !dataNode.isArray()) {
            logger.error("Invalid response format from API");
            return Collections.emptyList();
        }

        List<FlightData> flights = new ArrayList<>();

        for (JsonNode flightNode : dataNode) {
            String flightIata = flightNode.path("flight").path("iata").asText();

            JsonNode departureNode = flightNode.path("departure");
            String departureAirport = departureNode.path("airport").asText();
            String departureAirportShort = departureNode.path("iata").asText();
            String departureTerminal = departureNode.path("terminal").asText();
            String departureGate = departureNode.path("gate").asText();
            LocalDateTime departureScheduledTime = parseDateTime(departureNode.path("scheduled").asText());
            Integer departureDelay = departureNode.path("delay").isNull() ? null : departureNode.path("delay").asInt();

            JsonNode arrivalNode = flightNode.path("arrival");
            String arrivalAirport = arrivalNode.path("airport").asText();
            String arrivalAirportShort = arrivalNode.path("iata").asText();
            String arrivalTerminal = arrivalNode.path("terminal").asText();
            String arrivalGate = arrivalNode.path("gate").asText();
            LocalDateTime arrivalScheduledTime = parseDateTime(arrivalNode.path("scheduled").asText());
            Integer arrivalDelay = arrivalNode.path("delay").isNull() ? null : arrivalNode.path("delay").asInt();

            // Flight duration might need calculation if not directly provided
            Integer duration = calculateDuration(departureScheduledTime, arrivalScheduledTime);

            FlightData flightData = new FlightData(
                    flightIata,
                    departureAirport,
                    departureAirportShort,
                    departureTerminal,
                    departureGate,
                    departureScheduledTime,
                    departureDelay,
                    arrivalAirport,
                    arrivalAirportShort,
                    arrivalTerminal,
                    arrivalGate,
                    arrivalScheduledTime,
                    arrivalDelay,
                    duration
            );

            flights.add(flightData);
        }

        return flights;
    }

    /**
     * Parses a date-time string into a LocalDateTime object.
     *
     * @param dateTimeStr The date-time string
     * @return The parsed LocalDateTime, or null if the string is empty or
     * invalid
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }

        try {
            return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
        } catch (Exception e) {
            logger.warn("Failed to parse date-time: {}", dateTimeStr);
            return null;
        }
    }

    /**
     * Calculates the duration between departure and arrival in minutes.
     *
     * @param departure The departure time
     * @param arrival The arrival time
     * @return The duration in minutes, or null if either time is null
     */
    private Integer calculateDuration(LocalDateTime departure, LocalDateTime arrival) {
        if (departure == null || arrival == null) {
            return null;
        }

        // Calculate the duration in minutes
        return (int) java.time.Duration.between(departure, arrival).toMinutes();
    }
}
