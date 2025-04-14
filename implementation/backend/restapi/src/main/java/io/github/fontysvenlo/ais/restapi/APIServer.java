package io.github.fontysvenlo.ais.restapi;

import java.util.Map;

import io.github.fontysvenlo.ais.businesslogic.PriceManagerImpl;
import io.github.fontysvenlo.ais.businesslogic.api.BusinessLogic;
import io.github.fontysvenlo.ais.businesslogic.api.PriceManager;
import io.github.fontysvenlo.ais.datarecords.PricePerKmData;
import io.javalin.Javalin;
import static io.javalin.apibuilder.ApiBuilder.crud;
import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

/**
 * This class is responsible for starting the REST server and defining the
 * routes.
 */
public class APIServer {

    private final BusinessLogic businessLogic;
    private AviationStackClient aviationStackClient;

    /**
     * Initializes the REST API server
     *
     * @param businessLogic the business logic implementation to communicate
     * with
     * @param apiKey the API key for the AviationStack API
     */
    public APIServer(BusinessLogic businessLogic, String apiKey) {
        this.businessLogic = businessLogic;
        this.aviationStackClient = new AviationStackClient(apiKey);
    }

    /**
     * Starts the REST API server
     *
     * @param configuration the configuration of the server
     */
    public void start(ServerConfig configuration) {
        // No need for preloading - flights will be fetched and stored on first request

        var app = Javalin.create(config -> {
            config.router.contextPath = "/api/v1";
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.allowHost("http://localhost:" + configuration.cors(), "127.0.0.1:" + configuration.cors());
                });
            });
            config.router.apiBuilder(() -> {
                crud("customers/{customer-id}", new CustomerResource(businessLogic.getCustomerManager()));
                crud("employees/{employee-id}", new EmployeeResource(businessLogic.getEmployeeManager()));

                // Add flight routes
                FlightResource flightResource = new FlightResource(
                        businessLogic.getFlightManager(),
                        aviationStackClient
                );

                PriceResource priceResource = new PriceResource(businessLogic.getPriceManager());

                // Add custom endpoint to refresh flight data
                // Replace automatic CRUD with explicit path definitions
                path("flights", () -> {
                    // GET operations
                    get("/", flightResource::getAll);
                    // Add a new endpoint to clear the flight data
                    delete("/cache", flightResource::clearCache);
                });

                path("price", () -> {
                    get("/", priceResource::getAll);
                    post("/create", priceResource::create);
                });
            });
        });
        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            ctx.status(422).json(Map.of("error", e.getMessage()));
        });

        app.start(configuration.port());
    }
}
