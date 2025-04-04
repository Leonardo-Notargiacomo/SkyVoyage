package io.github.fontysvenlo.ais.restapi;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Client for interacting with the AviationStack API.
 */
public class AviationStackClient {

    private static final Logger logger = LoggerFactory.getLogger(AviationStackClient.class);
    private static final String API_BASE_URL = "http://api.aviationstack.com/v1";

    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final int pricePerKm = 11;

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
     * @return A JsonNode containing flight data
     */
    public JsonNode getAllFlights() {
        try {
            String url = API_BASE_URL + "/flights?access_key=" + apiKey;
            logger.info("Fetching flights from URL: {}", url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("API response status: {}", Optional.of(response.statusCode()));

            if (response.statusCode() != 200) {
                logger.error("Failed to get flights from API: {}", response.body());
                return objectMapper.createArrayNode();
            }

            return parseFlightResponse(response.body());
        } catch (IOException | InterruptedException e) {
            logger.error("Error while fetching flights", e);
            return objectMapper.createArrayNode();
        }
    }

    /**
     * Gets flights from Amsterdam to unique destinations (only one flight per
     * destination) to display on the home screen.
     *
     * @return A JsonNode containing flight data with unique destinations
     */
    public JsonNode getAllFlightsHome() {
        try {
            String url = API_BASE_URL + "/flights?access_key=" + apiKey + "&dep_iata=AMS";
            logger.info("Fetching flights from URL: {}", url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("API response status: {}", Optional.of(response.statusCode()));

            if (response.statusCode() != 200) {
                logger.error("Failed to get flights from API: {}", response.body());
                return objectMapper.createArrayNode();
            }

            JsonNode allFlights = parseFlightResponse(response.body());

            // Filter to keep only one flight per destination
            ArrayNode uniqueDestinationFlights = objectMapper.createArrayNode();

            // Track destinations we've already included
            java.util.Set<String> includedDestinations = new java.util.HashSet<>();
            int count = 0;

            // Add one flight per unique destination, up to 10 flights
            for (JsonNode flight : allFlights) {
                String destination = flight.path("arrival").path("iata").asText();

                if (!includedDestinations.contains(destination) && !destination.isEmpty()) {
                    uniqueDestinationFlights.add(flight);
                    includedDestinations.add(destination);
                    count++;

                    // Stop after adding 10 unique destination flights
                    if (count >= 10) {
                        break;
                    }
                }
            }

            return uniqueDestinationFlights;
        } catch (IOException | InterruptedException e) {
            logger.error("Error while fetching flights", e);
            return objectMapper.createArrayNode();
        }
    }

    /**
     * Searches for flights based on various criteria.
     *
     * @param airline IATA airline code
     * @param flightNumber Flight number
     * @param departureIata IATA code of departure airport
     * @param arrivalIata IATA code of arrival airport
     * @return A JsonNode containing flight data matching the criteria
     */
    public JsonNode searchFlights(String airline, String flightNumber, String departureIata, String arrivalIata) {
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
                return objectMapper.createArrayNode();
            }

            return parseFlightResponse(response.body());
        } catch (IOException | InterruptedException e) {
            logger.error("Error while searching flights", e);
            return objectMapper.createArrayNode();
        }
    }

    /**
     * Parses the JSON response from the AviationStack API into a formatted
     * JsonNode. This method cleans up the API response and returns a properly
     * formatted JSON.
     *
     * @param responseBody The JSON response body
     * @return A JsonNode containing the formatted flight data
     * @throws IOException If there is an error parsing the JSON
     */
    private JsonNode parseFlightResponse(String responseBody) throws IOException {
        JsonNode rootNode = objectMapper.readTree(responseBody);
        JsonNode dataNode = rootNode.get("data");

        if (dataNode == null || !dataNode.isArray()) {
            logger.error("Invalid response format from API");
            return objectMapper.createArrayNode();
        }

        ArrayNode formattedFlights = objectMapper.createArrayNode();

        for (JsonNode flightNode : dataNode) {
            ObjectNode formattedFlight = objectMapper.createObjectNode();

            // Extract and add basic flight information
            formattedFlight.put("id", flightNode.path("flight").path("iata").asText());

            // Add departure information
            ObjectNode departure = objectMapper.createObjectNode();
            JsonNode departureNode = flightNode.path("departure");
            departure.put("airport", departureNode.path("airport").asText());
            departure.put("iata", departureNode.path("iata").asText());
            departure.put("terminal", departureNode.path("terminal").asText());
            departure.put("gate", departureNode.path("gate").asText());
            departure.put("scheduled", departureNode.path("scheduled").asText());
            departure.put("delay", departureNode.path("delay").isNull() ? 0 : departureNode.path("delay").asInt());
            formattedFlight.set("departure", departure);

            // Add arrival information
            ObjectNode arrival = objectMapper.createObjectNode();
            JsonNode arrivalNode = flightNode.path("arrival");
            arrival.put("airport", arrivalNode.path("airport").asText());
            arrival.put("iata", arrivalNode.path("iata").asText());
            arrival.put("terminal", arrivalNode.path("terminal").asText());
            arrival.put("gate", arrivalNode.path("gate").asText());
            arrival.put("scheduled", arrivalNode.path("scheduled").asText());
            arrival.put("delay", arrivalNode.path("delay").isNull() ? 0 : arrivalNode.path("delay").asInt());
            formattedFlight.set("arrival", arrival);

            // Add additional flight information
            formattedFlight.put("airline", flightNode.path("airline").path("name").asText());
            formattedFlight.put("status", flightNode.path("flight_status").asText());

            //add duration of flight according to the scheduled time
            String scheduledDeparture = departureNode.path("scheduled").asText();
            String scheduledArrival = arrivalNode.path("scheduled").asText();
            int calculatedDuration = calculateDuration(scheduledDeparture, scheduledArrival);

            // Skip flights with zero duration
            if (calculatedDuration <= 0) {
//                logger.warn("Skipping flight {} with invalid duration: {}",
//                        flightNode.path("flight").path("iata").asText(), calculatedDuration);
                continue;
            }

            String Duration = String.valueOf(calculatedDuration);
            formattedFlight.put("duration", Duration);

            //add price of flight according to the duration
            int price = flightPrice(Integer.parseInt(Duration)) / 100;
            formattedFlight.put("price", price);

            formattedFlights.add(formattedFlight);
        }

        return formattedFlights;
    }

    //calculate km using duration * 15 is the km per minute
    //price per km is 11 dummy value for now.
    private int flightPrice(int duration) {
        return (duration * 15) * pricePerKm;
    }

    private int calculateDuration(String scheduledDeparture, String scheduledArrival) {
        try {
            // Parse the ISO-8601 formatted timestamps into OffsetDateTime objects
            OffsetDateTime departureTime = OffsetDateTime.parse(scheduledDeparture);
            OffsetDateTime arrivalTime = OffsetDateTime.parse(scheduledArrival);

            // Calculate duration in minutes between the two times
            long durationMinutes = ChronoUnit.MINUTES.between(departureTime, arrivalTime);

            // Ensure the duration is positive
            if (durationMinutes < 0) {
                logger.warn("Calculated negative duration between {} and {}, possible data error",
                        scheduledDeparture, scheduledArrival);
                // Fallback: use absolute value as a safety measure
                durationMinutes = Math.abs(durationMinutes);
            }

            return (int) durationMinutes;
        } catch (Exception e) {
            logger.error("Error calculating flight duration: ", e);
            // Fallback to a reasonable duration if parsing fails (5 hours)
            return 300;
        }
    }
}
