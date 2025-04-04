package io.github.fontysvenlo.ais.restapi;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.fontysvenlo.ais.businesslogic.api.FlightManager;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;

/**
 * This class is responsible for handling the requests for the flight resource.
 */
class FlightResource implements CrudHandler {

    private static final Logger logger = LoggerFactory.getLogger(FlightResource.class);
    private final FlightManager flightManager;
    private final AviationStackClient aviationStackClient;

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
        JsonNode apiFlights;

        try {
            apiFlights = aviationStackClient.getAllFlightsHome();
        } catch (Exception e) {
            logger.error("Error retrieving flights", e);
            context.status(500).json(Map.of(
                "error", "Failed to retrieve flights",
                "details", e.getMessage()
            ));
            return;
        }

        //in case of apiFlights being null and no exception town, return an error
        if (apiFlights == null) {
            context.status(500).json(Map.of("error", "Failed to retrieve flights from AviationStack"));
            return;
        }

        context.status(200).json(apiFlights);
    }

    @Override
    public void getOne(Context context, String flightId) {
        context.status(501); // Not Implemented
        context.json(Map.of("error", "Getting a specific flight is not implemented"));
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
     * Clears the flight data cache
     */
    public void clearCache(Context context) {
        try {
            aviationStackClient.clearCache();
            context.status(200).json(Map.of("message", "Flight cache cleared successfully"));
        } catch (Exception e) {
            logger.error("Error clearing flight cache", e);
            context.status(500).json(Map.of("error", "Failed to clear flight cache: " + e.getMessage()));
        }
    }
}
