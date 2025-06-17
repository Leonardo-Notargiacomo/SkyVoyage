package io.github.fontysvenlo.ais.restapi;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.fontysvenlo.ais.businesslogic.api.DiscountManager;
import io.github.fontysvenlo.ais.businesslogic.api.PriceManager;
import io.github.fontysvenlo.ais.datarecords.FlightData;

/**
 * Client for interacting with the AviationStack API.
 */
public class AviationStackClient {

    private static final String API_BASE_URL = "http://api.aviationstack.com/v1";

    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private PriceManager priceManager;
    private DiscountManager discountManager;

    /**
     * Creates a new AviationStackClient.
     *
     * @param apiKey The API key for the AviationStack API
     */
    public AviationStackClient(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Sets the PriceManager for this client.
     *
     * @param priceManager The PriceManager to use
     */
    public void setPriceManager(PriceManager priceManager) {
        this.priceManager = priceManager;
    }

    /**
     * Sets the DiscountManager for this client.
     *
     * @param discountManager The DiscountManager to use
     */
    public void setDiscountManager(DiscountManager discountManager) {
        this.discountManager = discountManager;
    }

    /**
     * Calculate a flight price based on duration.
     * 
     * @param duration The duration in minutes
     * @param departure The departure date and time
     * @return The calculated price
     */
    private int flightPrice(int duration, OffsetDateTime departure) {
        if (priceManager == null) {
            throw new IllegalStateException("PriceManager is not set");
        }
        
        // Calculate base price
        int basePrice = priceManager.calculateBasePrice(duration);

        // Apply discount if available and departure time is provided
        if (departure != null && discountManager != null) {
            double discountedPrice = discountManager.calculateDiscountedPrice(basePrice, departure);
            return (int) Math.floor(discountedPrice);
        }
        
        return basePrice;
    }

    /**
     * Gets all flights currently available from the API.
     *
     * @return A JsonNode containing flight data
     */
    public JsonNode getAllFlights() {
        try {
            String url = API_BASE_URL + "/flights?access_key=" + apiKey;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return objectMapper.createArrayNode();
            }

            return parseFlightResponse(response.body());
        } catch (IOException | InterruptedException e) {
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
            String url = API_BASE_URL + "/flights?access_key=" + apiKey + "&dep_iata=AMS&flight_status=scheduled";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
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
                return objectMapper.createArrayNode();
            }

            return parseFlightResponse(response.body());
        } catch (IOException | InterruptedException e) {
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
            return objectMapper.createArrayNode();
        }

        ArrayNode formattedFlights = objectMapper.createArrayNode();

        for (JsonNode flightNode : dataNode) {
            ObjectNode formattedFlight = objectMapper.createObjectNode();

            // Extract and add basic flight information
            String flightIata = flightNode.path("flight").path("iata").asText("");
            String flightNumber = flightNode.path("flight").path("number").asText("");
            String depIata = flightNode.path("departure").path("iata").asText("");
            String arrIata = flightNode.path("arrival").path("iata").asText("");
            
            // Generate a unique ID if flight IATA is empty
            String id = flightIata;
            if (id == null || id.isEmpty()) {
                // Create a unique ID using flight number, departure, arrival, and timestamp
                String timestamp = String.valueOf(System.currentTimeMillis()).substring(6);
                id = flightNumber + "_" + depIata + "_" + arrIata + "_" + timestamp;
            }
            
            formattedFlight.put("id", id);

            // Add departure information
            ObjectNode departure = objectMapper.createObjectNode();
            JsonNode departureNode = flightNode.path("departure");
            departure.put("airport", departureNode.path("airport").asText());
            departure.put("iata", departureNode.path("iata").asText());
            departure.put("terminal", departureNode.path("terminal").asText());
            departure.put("gate", departureNode.path("gate").asText());
            departure.put("scheduled", departureNode.path("scheduled").asText());
            departure.put("timezone", departureNode.path("timezone").asText());
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
            arrival.put("timezone", arrivalNode.path("timezone").asText());
            arrival.put("delay", arrivalNode.path("delay").isNull() ? 0 : arrivalNode.path("delay").asInt());
            formattedFlight.set("arrival", arrival);

            // Add additional flight information
            formattedFlight.put("airline", flightNode.path("airline").path("name").asText());
            formattedFlight.put("status", flightNode.path("flight_status").asText());

            // Add duration of flight according to the scheduled time
            String scheduledDeparture = departureNode.path("scheduled").asText();
            String scheduledArrival = arrivalNode.path("scheduled").asText();
            String departureTimezone = departureNode.path("timezone").asText();
            String arrivalTimezone = arrivalNode.path("timezone").asText();

            int calculatedDuration = calculateDuration(scheduledDeparture, scheduledArrival,
                    departureTimezone, arrivalTimezone);

            // Skip flights with zero duration
            if (calculatedDuration <= 0) {
                continue;
            }

            String Duration = String.valueOf(calculatedDuration);
            formattedFlight.put("duration", Duration);

            //add price of flight according to the duration
            int price = flightPrice(Integer.parseInt(Duration), OffsetDateTime.parse(scheduledDeparture));
            formattedFlight.put("price", price);

            formattedFlights.add(formattedFlight);
        }

        return formattedFlights;
    }

    /**
     * Calculates the duration of a flight in minutes, taking into account the
     * actual timezone difference between departure and arrival airports.
     *
     * @param scheduledDeparture The scheduled departure time in UTC
     * @param scheduledArrival The scheduled arrival time in UTC
     * @param departureTimezone The timezone of the departure airport
     * @param arrivalTimezone The timezone of the arrival airport
     * @return The flight duration in minutes
     */
    private int calculateDuration(String scheduledDeparture, String scheduledArrival,
            String departureTimezone, String arrivalTimezone) {
        try {
            OffsetDateTime departureTimeUTC = OffsetDateTime.parse(scheduledDeparture);
            OffsetDateTime arrivalTimeUTC = OffsetDateTime.parse(scheduledArrival);

            if (departureTimezone != null && !departureTimezone.isEmpty()
                    && arrivalTimezone != null && !arrivalTimezone.isEmpty()) {

                    java.time.ZoneId depZone = java.time.ZoneId.of(departureTimezone);
                    java.time.ZoneId arrZone = java.time.ZoneId.of(arrivalTimezone);

                    java.time.Instant depInstant = departureTimeUTC.toInstant();
                    java.time.Instant arrInstant = arrivalTimeUTC.toInstant();

                    java.time.ZoneOffset depOffset = depZone.getRules().getOffset(depInstant);
                    java.time.ZoneOffset arrOffset = arrZone.getRules().getOffset(arrInstant);

                    int offsetDiffMinutes = (depOffset.getTotalSeconds() - arrOffset.getTotalSeconds()) / 60;

                    long durationMinutes = ChronoUnit.MINUTES.between(departureTimeUTC, arrivalTimeUTC);

                    durationMinutes -= offsetDiffMinutes;

                    if (durationMinutes < 30) {

                        durationMinutes = Math.max(45, durationMinutes);

                        if (!departureTimezone.equals(arrivalTimezone)) {
                            durationMinutes = Math.max(60, durationMinutes);
                        }
                    }

                    return (int) durationMinutes;
            }
            long durationMinutes = ChronoUnit.MINUTES.between(departureTimeUTC, arrivalTimeUTC);

            if (durationMinutes < 30) {
                return 45;
            }

            return (int) durationMinutes;
        } catch (Exception e) {
            return 90;
        }
    }

    /**
     * Converts a JSON node from the AviationStack API to a FlightData object
     * that can be stored in the repository.
     */
    public FlightData jsonToFlightData(JsonNode flightNode) {
        try {
            // Extract the necessary fields from the JSON
            String id = flightNode.path("id").asText();
            
            // If ID is empty, generate one
            if (id == null || id.isEmpty()) {
                String depIata = flightNode.path("departure").path("iata").asText("");
                String arrIata = flightNode.path("arrival").path("iata").asText("");
                String timestamp = String.valueOf(System.currentTimeMillis()).substring(6);
                id = depIata + "_" + arrIata + "_" + timestamp;
            }

            // Departure information
            String departureAirport = flightNode.path("departure").path("airport").asText();
            String departureAirportShort = flightNode.path("departure").path("iata").asText();
            String departureTerminal = flightNode.path("departure").path("terminal").asText();
            String departureGate = flightNode.path("departure").path("gate").asText();

            // Arrival information
            String arrivalAirport = flightNode.path("arrival").path("airport").asText();
            String arrivalAirportShort = flightNode.path("arrival").path("iata").asText();
            String arrivalTerminal = flightNode.path("arrival").path("terminal").asText();
            String arrivalGate = flightNode.path("arrival").path("gate").asText();

            // Convert scheduled times to OffsetDateTime
            OffsetDateTime departureScheduledTime = null;
            if (flightNode.path("departure").has("scheduled")) {
                String departureTimeStr = flightNode.path("departure").path("scheduled").asText();
                departureScheduledTime = OffsetDateTime.parse(departureTimeStr);
            } else {
                // Use current time as fallback
                departureScheduledTime = OffsetDateTime.now();
            }

            OffsetDateTime arrivalScheduledTime = null;
            if (flightNode.path("arrival").has("scheduled")) {
                String arrivalTimeStr = flightNode.path("arrival").path("scheduled").asText();
                arrivalScheduledTime = OffsetDateTime.parse(arrivalTimeStr);
            } else {
                // Use departure time + 2 hours as fallback
                arrivalScheduledTime = departureScheduledTime.plusHours(2);
            }

            // Extract delays (default to 0 if not present)
            int departureDelay = flightNode.path("departure").path("delay").asInt(0);
            int arrivalDelay = flightNode.path("arrival").path("delay").asInt(0);

            // Calculate duration in minutes between departure and arrival time
            int duration = (int) java.time.Duration.between(
                    departureScheduledTime, arrivalScheduledTime).toMinutes();

            // Create and return the FlightData object - after checking the constructor signature
            // The FlightData constructor appears to expect OffsetDateTime but something in the 
            // FlightData class may be attempting to convert it to LocalDateTime
            return new FlightData(
                    id,
                    departureAirport,
                    departureAirportShort,
                    departureTerminal,
                    departureGate,
                    departureScheduledTime.toLocalDateTime(),
                    departureDelay,
                    arrivalAirport,
                    arrivalAirportShort,
                    arrivalTerminal,
                    arrivalGate,
                    arrivalScheduledTime.toLocalDateTime(),
                    arrivalDelay,
                    duration
            );
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Converts a FlightData object to a JsonNode with the structure expected by the frontend
     */
    public JsonNode flightDataToFrontendFormat(FlightData flight) {
        ObjectNode formattedFlight = objectMapper.createObjectNode();

        // Extract and add basic flight information
        formattedFlight.put("id", flight.id());

        // Add departure information
        ObjectNode departure = objectMapper.createObjectNode();
        departure.put("airport", flight.departureAirport());
        departure.put("iata", flight.departureAirportShort());
        departure.put("terminal", flight.departureTerminal());
        departure.put("gate", flight.departureGate());
        departure.put("scheduled", flight.departureScheduledTime().toString());
        departure.put("timezone", ""); // Not available in FlightData
        departure.put("delay", flight.departureDelay());
        formattedFlight.set("departure", departure);

        // Add arrival information
        ObjectNode arrival = objectMapper.createObjectNode();
        arrival.put("airport", flight.arrivalAirport());
        arrival.put("iata", flight.arrivalAirportShort());
        arrival.put("terminal", flight.arrivalTerminal());
        arrival.put("gate", flight.arrivalGate());
        arrival.put("scheduled", flight.arrivalScheduledTime().toString());
        arrival.put("timezone", ""); // Not available in FlightData
        arrival.put("delay", flight.arrivalDelay());
        formattedFlight.set("arrival", arrival);

        // Add additional flight information
        formattedFlight.put("airline", "Unknown Airline");
        formattedFlight.put("status", "scheduled");

        // Add duration of flight
        formattedFlight.put("duration", flight.duration().toString());

        // Add price of flight according to the duration
        // Convert LocalDateTime to OffsetDateTime with system default offset
        OffsetDateTime departureTime = flight.departureScheduledTime().atOffset(java.time.ZoneOffset.systemDefault().getRules().getOffset(flight.departureScheduledTime()));
        int price = flightPrice(Integer.parseInt(flight.duration().toString()), departureTime);
        formattedFlight.put("price", price);

        return formattedFlight;
    }

    /**
     * Converts a FlightData object to a Map with the structure expected by the
     * frontend
     */
    public Map<String, Object> convertFlightDataToJson(FlightData flight) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", flight.id());

        // Create departure object
        Map<String, Object> departure = new HashMap<>();
        departure.put("airport", flight.departureAirport());
        departure.put("iata", flight.departureAirportShort());
        departure.put("terminal", flight.departureTerminal());
        departure.put("gate", flight.departureGate());
        departure.put("scheduled", flight.departureScheduledTime().toString());
        departure.put("timezone", ""); // Not available in FlightData
        departure.put("delay", flight.departureDelay());
        result.put("departure", departure);

        // Create arrival object
        Map<String, Object> arrival = new HashMap<>();
        arrival.put("airport", flight.arrivalAirport());
        arrival.put("iata", flight.arrivalAirportShort());
        arrival.put("terminal", flight.arrivalTerminal());
        arrival.put("gate", flight.arrivalGate());
        arrival.put("scheduled", flight.arrivalScheduledTime().toString());
        arrival.put("timezone", ""); // Not available in FlightData
        arrival.put("delay", flight.arrivalDelay());
        result.put("arrival", arrival);

        // Add other flight details
        result.put("duration", flight.duration().toString());
        result.put("status", "scheduled"); // Default status
        result.put("airline", "Unknown Airline"); // Placeholder

        // Calculate a price based on duration (similar to AviationStackClient logic)
        // Convert LocalDateTime to OffsetDateTime with system default offset
        OffsetDateTime departureTime = flight.departureScheduledTime().atOffset(java.time.ZoneOffset.systemDefault().getRules().getOffset(flight.departureScheduledTime()));
        int price = flightPrice(Integer.parseInt(flight.duration().toString()), departureTime);
        result.put("price", price);

        return result;
    }

    /**
     * Converts a JsonNode flight from the API to a FlightData object
     */
    public FlightData convertJsonToFlightData(JsonNode flightNode) {
        try {
            String id = flightNode.path("id").asText();
            
            // Generate ID if missing
            if (id == null || id.isEmpty()) {
                String depIata = flightNode.path("departure").path("iata").asText("");
                String arrIata = flightNode.path("arrival").path("iata").asText("");
                id = depIata + "-" + arrIata + "-" + System.currentTimeMillis();
            }

            // Departure info
            String depAirport = flightNode.path("departure").path("airport").asText();
            String depIata = flightNode.path("departure").path("iata").asText();
            String depTerminal = flightNode.path("departure").path("terminal").asText();
            String depGate = flightNode.path("departure").path("gate").asText();
            String depScheduledStr = flightNode.path("departure").path("scheduled").asText();
            OffsetDateTime depScheduled = OffsetDateTime.parse(depScheduledStr);
            int depDelay = flightNode.path("departure").path("delay").asInt();

            // Arrival info
            String arrAirport = flightNode.path("arrival").path("airport").asText();
            String arrIata = flightNode.path("arrival").path("iata").asText();
            String arrTerminal = flightNode.path("arrival").path("terminal").asText();
            String arrGate = flightNode.path("arrival").path("gate").asText();
            String arrScheduledStr = flightNode.path("arrival").path("scheduled").asText();
            OffsetDateTime arrScheduled = OffsetDateTime.parse(arrScheduledStr);
            int arrDelay = flightNode.path("arrival").path("delay").asInt();

            // Duration
            int duration = Integer.parseInt(flightNode.path("duration").asText());

            return new FlightData(
                    id,
                    depAirport, depIata, depTerminal, depGate,
                    depScheduled.toLocalDateTime(), depDelay,
                    arrAirport, arrIata, arrTerminal, arrGate,
                    arrScheduled.toLocalDateTime(), arrDelay,
                    duration
            );
        } catch (Exception e) {
            return null;
        }
    }
}
