package io.github.fontysvenlo.ais.restapi;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        try {
            // Get query parameters for search
            String airline = context.queryParam("airline");
            String flightNumber = context.queryParam("flight_number");
            String departureIata = context.queryParam("departure");
            String arrivalIata = context.queryParam("arrival");

            List<FlightData> flights;

            // If search parameters are provided, use search method
            if (airline != null || flightNumber != null || departureIata != null || arrivalIata != null) {
                flights = flightManager.search(airline, flightNumber, departureIata, arrivalIata);
            } else {
                // Otherwise, get all flights
                flights = flightManager.list();

                // If no flights in database, fetch from API
                if (flights.isEmpty()) {
                    logger.info("No flights in database, fetching from AviationStack API");
                    flights = aviationStackClient.getAllFlights();

                    // Store flights in database
                    for (FlightData flight : flights) {
                        flightManager.add(flight);
                    }
                }
            }

            context.status(200);
            context.json(flights);
        } catch (Exception e) {
            logger.error("Error retrieving flights", e);
            context.status(500);
            context.json(Map.of("error", "Failed to retrieve flights: " + e.getMessage()));
        }
    }

    /**
     * Gets a specific flight by ID.
     */
    @Override
    public void getOne(Context context, String flightId) {
        try {
            FlightData flight = flightManager.getOne(flightId);
            if (flight != null) {
                context.status(200);
                context.json(flight);
            } else {
                context.status(404);
                context.json(Map.of("error", "Flight not found"));
            }
        } catch (Exception e) {
            logger.error("Error retrieving flight {}", flightId, e);
            context.status(500);
            context.json(Map.of("error", "Failed to retrieve flight: " + e.getMessage()));
        }
    }

    /**
     * Adds a flight to the system.
     */
    @Override
    public void create(Context context) {
        try {
            FlightData flightData = context.bodyAsClass(FlightData.class);
            if (flightData == null) {
                context.status(400);
                logger.error("Received null flight data");
                context.json(Map.of("error", "Invalid flight data format"));
                return;
            }

            FlightData addedFlight = flightManager.add(flightData);
            context.status(201);
            context.json(addedFlight);
        } catch (Exception e) {
            logger.error("Error adding flight", e);
            context.status(500);
            context.json(Map.of("error", "Failed to add flight: " + e.getMessage()));
        }
    }

    /**
     * Updates a flight in the system. Not implemented for this resource since
     * it uses external API data.
     */
    @Override
    public void update(Context context, String flightId) {
        context.status(405); // Method Not Allowed
        context.json(Map.of("error", "Updating flights is not supported"));
    }

    /**
     * Deletes a flight from the system. Not implemented for this resource since
     * it uses external API data.
     */
    @Override
    public void delete(Context context, String flightId) {
        context.status(405); // Method Not Allowed
        context.json(Map.of("error", "Deleting flights is not supported"));
    }

    /**
     * Refreshes flight data from the AviationStack API. This is a custom
     * endpoint that doesn't fit in the CRUD pattern.
     */
    public void refreshFlights(Context context) {
        try {
            List<FlightData> flights = aviationStackClient.getAllFlights();

            if (flights.isEmpty()) {
                context.status(404);
                context.json(Map.of("message", "No flights found from API"));
                return;
            }

            // Store flights in database
            for (FlightData flight : flights) {
                flightManager.add(flight);
            }

            context.status(200);
            context.json(Map.of(
                    "message", "Successfully refreshed flights from API",
                    "count", flights.size()
            ));
        } catch (Exception e) {
            logger.error("Error refreshing flights", e);
            context.status(500);
            context.json(Map.of("error", "Failed to refresh flights: " + e.getMessage()));
        }
    }
}
