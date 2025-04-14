package io.github.fontysvenlo.ais.restapi;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.fontysvenlo.ais.datarecords.PricePerKmData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
    private final AmadeusClient amadeusClient;

    /**
     * Initializes the controller with the business logic, AviationStack client
     * and Amadeus client.
     */
    FlightResource(FlightManager flightManager, AviationStackClient aviationStackClient, AmadeusClient amadeusClient) {
        this.flightManager = flightManager;
        this.aviationStackClient = aviationStackClient;
        this.amadeusClient = amadeusClient;
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
                    .map(aviationStackClient::convertFlightDataToJson)
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

            logger.info("Retrieved {} flights from AviationStack API", apiFlights.size());

            // Store flights in the repository for future use
            List<FlightData> newFlights = new ArrayList<>();
            
            for (JsonNode flightNode : apiFlights) {
                // Ensure the flight node has an ID
                String flightId = flightNode.path("id").asText("");
                if (flightId.isEmpty()) {
                    // Generate an ID if missing
                    String depCode = flightNode.path("departure").path("iata").asText("XXX");
                    String arrCode = flightNode.path("arrival").path("iata").asText("YYY");
                    flightId = depCode + "-" + arrCode + "-" + System.currentTimeMillis();
                    ((ObjectNode) flightNode).put("id", flightId);
                }
                
                FlightData flight = aviationStackClient.convertJsonToFlightData(flightNode);
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

    /**
     * Searches for flights using the Amadeus API based on query parameters.
     * Expected query parameters: - originLocationCode: Origin airport code
     * (e.g., AMS) - destinationLocationCode: Destination airport code (e.g.,
     * JFK) - departureDate: Departure date in YYYY-MM-DD format - returnDate:
     * Return date in YYYY-MM-DD format (optional for one-way flights)
     * Additional parameters supported: - adults: Number of adult travelers
     * (default: 1) - children: Number of child travelers - infants: Number of
     * infant travelers - travelClass: Cabin class (ECONOMY, PREMIUM_ECONOMY,
     * BUSINESS, FIRST) - includedAirlineCodes: Airlines to include in search
     * (comma-separated IATA codes) - excludedAirlineCodes: Airlines to exclude
     * from search (comma-separated IATA codes) - nonStop: If true, only
     * non-stop flights are returned - currencyCode: Preferred currency (ISO
     * 4217 format) - maxPrice: Maximum price per traveler - max: Maximum number
     * of flight offers to return
     */
    public void searchFlights(Context context) {
        try {
            // Extract required query parameters using Amadeus parameter names directly
            String originLocationCode = context.queryParam("originLocationCode");
            String destinationLocationCode = context.queryParam("destinationLocationCode");
            String departureDate = context.queryParam("departureDate");
            String returnDate = context.queryParam("returnDate");

            // Validate required parameters
            if (originLocationCode == null || destinationLocationCode == null || departureDate == null) {
                context.status(400).json(Map.of("error", "Missing required parameters: originLocationCode, destinationLocationCode, and departureDate are required"));
                return;
            }

            // Create parameters map - now parameter names already match Amadeus API
            Map<String, String> params = new HashMap<>();
            params.put("originLocationCode", originLocationCode);
            params.put("destinationLocationCode", destinationLocationCode);
            params.put("departureDate", departureDate);

            // Add return date if provided (for round trips)
            if (returnDate != null && !returnDate.isEmpty()) {
                params.put("returnDate", returnDate);
                logger.info("Searching for round-trip flight: {} to {} and back, departing {} and returning {}",
                        originLocationCode, destinationLocationCode, departureDate, returnDate);
            } else {
                logger.info("Searching for one-way flight: {} to {}, departing {}",
                        originLocationCode, destinationLocationCode, departureDate);
            }

            // Add optional parameters if provided - using Amadeus parameter names directly
            addParamIfPresent(context, params, "adults", "adults", "1");
            addParamIfPresent(context, params, "travelClass", "travelClass", "ECONOMY");
            addParamIfPresent(context, params, "max", "max", "10");
            addParamIfPresent(context, params, "nonStop", "nonStop");
            addParamIfPresent(context, params, "currencyCode", "currencyCode");
            addParamIfPresent(context, params, "maxPrice", "maxPrice");
            addParamIfPresent(context, params, "children", "children");
            addParamIfPresent(context, params, "infants", "infants");

            // Call Amadeus API with all parameters
            Map<String, Object> processedResults = amadeusClient.searchFlightOffersWithParams(params);

            // Return processed results
            context.status(200).json(processedResults);

        } catch (Exception e) {
            logger.error("Error searching flights with Amadeus API", e);
            context.status(500).json(Map.of(
                    "error", "Failed to search flights",
                    "details", e.getMessage()
            ));
        }
    }

    /**
     * Helper method to add a parameter to the params map if it exists in the
     * context
     */
    private void addParamIfPresent(Context context, Map<String, String> params, String contextKey, String paramKey) {
        String value = context.queryParam(contextKey);
        if (value != null) {
            params.put(paramKey, value);
        }
    }

    /**
     * Helper method to add a parameter to the params map if it exists in the
     * context, with a default value
     */
    private void addParamIfPresent(Context context, Map<String, String> params, String contextKey, String paramKey, String defaultValue) {
        String value = context.queryParam(contextKey);
        params.put(paramKey, value != null ? value : defaultValue);
    }
}
