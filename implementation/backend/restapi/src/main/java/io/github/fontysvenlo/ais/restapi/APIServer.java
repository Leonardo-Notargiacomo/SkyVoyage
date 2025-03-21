package io.github.fontysvenlo.ais.restapi;

import java.util.Map;

import io.github.fontysvenlo.ais.businesslogic.api.BusinessLogic;
import io.javalin.Javalin;
import static io.javalin.apibuilder.ApiBuilder.crud;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;

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

                // Add custom endpoint to refresh flight data
                // Replace automatic CRUD with explicit path definitions
                path("flights", () -> {
                    // GET operations
                    get("/", flightResource::getAll);
                    get("/{flight-id}", ctx -> flightResource.getOne(ctx, ctx.pathParam("flight-id")));

                    // POST operation
                    post("/", flightResource::create);

                    // PUT operation
                    put("/{flight-id}", ctx -> flightResource.update(ctx, ctx.pathParam("flight-id")));

                    // DELETE operation
                    delete("/{flight-id}", ctx -> flightResource.delete(ctx, ctx.pathParam("flight-id")));

                    // Custom refresh endpoint
                    get("/refresh", flightResource::refreshFlights);
                });
            });
        });

        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            ctx.status(422).json(Map.of("error", e.getMessage()));
        });

        app.start(configuration.port());
    }
}
