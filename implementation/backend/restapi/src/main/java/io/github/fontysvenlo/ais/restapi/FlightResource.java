package io.github.fontysvenlo.ais.restapi;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.fontysvenlo.ais.businesslogic.api.FlightManager;
import io.github.fontysvenlo.ais.datarecords.FlightData;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;

/**
 * This class is responsible for handling the requests for the flight resource.
 */
class FlightResource implements CrudHandler {

    private static final Logger logger = LoggerFactory.getLogger(FlightResource.class);
    private final FlightManager flightManager;
    private final AviationStackClient aviationStackClient;
    private int pricePerKm = 15;

    /**
     * Initializes the controller with the business logic and AviationStack
     * client.
     */
    FlightResource(FlightManager flightManager, AviationStackClient aviationStackClient) {
        this.flightManager = flightManager;
        this.aviationStackClient = aviationStackClient;
    }

    /**
     * Retrieves all flights from the system. The status is set to 200 (OK) and
     * the list of flights is returned as JSON.
     */
    @Override
    public void getAll(Context context) {
        // First, try to get flights from the repository
        List<FlightData> flights = flightManager.list();

        // If we have flights in the repository, return them
        if (flights != null && !flights.isEmpty()) {
            logger.info("Returning {} flights from repository", flights.size());
            // Convert FlightData objects to the expected JSON format
            List<Map<String, Object>> formattedFlights = flights.stream()
                    .map(this::convertFlightDataToJson)
                    .toList();
            context.status(200).json(formattedFlights);
            return;
        }

        // If no flights in repository, fetch from API
        try {
            JsonNode apiFlights = aviationStackClient.getAllFlightsHome();

            if (apiFlights == null) {
                context.status(500).json(Map.of("error", "Failed to retrieve flights from AviationStack"));
                return;
            }

            // Store flights in the repository for future use
            List<FlightData> newFlights = new ArrayList<>();
            for (JsonNode flightNode : apiFlights) {
                FlightData flight = convertJsonToFlightData(flightNode);
                if (flight != null) {
                    flightManager.add(flight);
                    newFlights.add(flight);
                }
            }

            
            List<Map<String, Object>> formattedFlights = newFlights.stream()
            .map(this::convertFlightDataToJson)
            .toList();
            context.status(200).json(formattedFlights);

        } catch (Exception e) {
            logger.error("Error retrieving flights", e);
            context.status(500).json(Map.of(
                    "error", "Failed to retrieve flights",
                    "details", e.getMessage()
            ));
        }
    }

    /**
     * Retrieves the current price per kilometer for flights.
     * <p>
     * This method returns the current pricePerKm variable in the response as a JSON object.
     * </p>
     *
     * @param context the Javalin context containing the request and response
     */

    public void getPrice(Context context) {
        context.json(Map.of("price", pricePerKm));
    }

    /**
     * Updates the price per kilometer for flights.
     * <p>
     * This method expects a JSON body with a key "price" containing the new price in cents.
     * It updates the internal pricePerKm variable and returns the updated price in the response.
     * </p>
     *
     * @param context the Javalin context containing the request and response
     */

     public void updatePrice(Context context) {
        try {
            var body = context.bodyAsClass(Map.class);
            if (!body.containsKey("price")) {
                context.status(400).json(Map.of("error", "Invalid price format"));
                return;
            }
    
            int newPrice = ((Number) body.get("price")).intValue();
            if (newPrice < 0) {
                context.status(400).json(Map.of("error", "Price per kilometer cannot be negative"));
                return;
            }
    
            pricePerKm = newPrice;
            aviationStackClient.updatePrice(pricePerKm);
            context.status(200).json(Map.of("price", pricePerKm));
        } catch (Exception e) {
            context.status(400).json(Map.of("error", "Invalid price format"));
        }
    }

    /**
     * Converts a FlightData object to a Map with the structure expected by the
     * frontend
     */
    private Map<String, Object> convertFlightDataToJson(FlightData flight) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", flight.id());

        // Create departure object
        Map<String, Object> departure = new HashMap<>();
        departure.put("airport", flight.departureAirport());
        departure.put("iata", flight.departureAirportShort());
        departure.put("terminal", flight.departureTerminal());
        departure.put("gate", flight.departureGate());
        departure.put("scheduled", flight.departureScheduledTime().toString());
        departure.put("delay", flight.departureDelay());
        result.put("departure", departure);

        // Create arrival object
        Map<String, Object> arrival = new HashMap<>();
        arrival.put("airport", flight.arrivalAirport());
        arrival.put("iata", flight.arrivalAirportShort());
        arrival.put("terminal", flight.arrivalTerminal());
        arrival.put("gate", flight.arrivalGate());
        arrival.put("scheduled", flight.arrivalScheduledTime().toString());
        arrival.put("delay", flight.arrivalDelay());
        result.put("arrival", arrival);

        // Add other flight details
        result.put("duration", flight.duration().toString());
        result.put("status", "scheduled"); // Default status
        result.put("airline", "Unknown Airline"); // Placeholder

        int price = (flight.duration() * 15 * pricePerKm) / 100;
        result.put("price", price);
        return result;
    }

    /**
     * Converts a JsonNode flight from the API to a FlightData object
     */
    private FlightData convertJsonToFlightData(JsonNode flightNode) {
        try {
            String id = flightNode.path("id").asText();

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
            logger.error("Error converting flight JSON to FlightData", e);
            return null;
        }
    }

    @Override
    public void getOne(Context context, String flightId) {
        // Simplified to just return not implemented since we're not using this endpoint
        context.status(501);
        context.json(Map.of("error", "Getting individual flights is not implemented"));
    }

    @Override
    public void create(Context context) {
        context.status(501);
        context.json(Map.of("error", "Creating flights is not supported"));
    }

    @Override
    public void update(Context context, String flightId) {
        context.status(501);
        context.json(Map.of("error", "Updating flights is not supported"));
    }

    @Override
    public void delete(Context context, String flightId) {
        context.status(405); // Method Not Allowed
        context.json(Map.of("error", "Deleting flights is not supported"));
    }

    /**
     * Clears the flight data from repository
     */
    public void clearCache(Context context) {
        try {
            // Get all flights
            List<FlightData> flights = flightManager.list();

            // Delete each flight
            for (FlightData flight : flights) {
                flightManager.delete(flight.id());
            }

            context.status(200).json(Map.of("message", "Flight data cleared successfully"));
        } catch (Exception e) {
            logger.error("Error clearing flight data", e);
            context.status(500).json(Map.of("error", "Failed to clear flight data: " + e.getMessage()));
        }
    }
}
