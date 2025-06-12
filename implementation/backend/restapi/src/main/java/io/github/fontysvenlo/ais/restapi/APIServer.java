package io.github.fontysvenlo.ais.restapi;

import java.util.Map;

import io.github.fontysvenlo.ais.businesslogic.api.BusinessLogic;
import io.github.fontysvenlo.ais.datarecords.EmployeeData;
import io.github.fontysvenlo.ais.datarecords.LoginRequest;
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
    private AmadeusClient amadeusClient;

    /**
     * Initializes the REST API server
     *
     * @param businessLogic the business logic implementation to communicate
     *                      with
     * @param apiKey        the API key for the AviationStack API
     */
    public APIServer(BusinessLogic businessLogic, String apiKey) {
        this.businessLogic = businessLogic;
        this.aviationStackClient = new AviationStackClient(apiKey);
        aviationStackClient.setPriceManager(businessLogic.getPriceManager());

        // Read Amadeus credentials from environment variables
        String amadeusClientId = System.getenv("AMADEUS_API_KEY");
        String amadeusClientSecret = System.getenv("AMADEUS_API_SECRET");

        // Print debug information
        System.out.println("========= EXTERNAL API CREDENTIALS =========");
        System.out.println("AviationStack API Key: " + (apiKey != null ? apiKey.substring(0, 5) + "..." : "null"));
        System.out.println("Amadeus API Key:       " + (amadeusClientId != null ? amadeusClientId.substring(0, 5) + "..." : "null"));
        System.out.println("Amadeus API Secret:    " + (amadeusClientSecret != null ? (amadeusClientSecret.length() > 5 ? amadeusClientSecret.substring(0, 5) + "..." : amadeusClientSecret) : "null"));
        System.out.println("============================================");

        if (amadeusClientId == null || amadeusClientSecret == null) {
            System.err.println("WARNING: Amadeus API credentials not found in environment variables. "
                    + "Set AMADEUS_API_KEY and AMADEUS_API_SECRET to use the flight search functionality.");
            // Default to empty strings to avoid null pointer exceptions
            amadeusClientId = "";
            amadeusClientSecret = "";
        }

        this.amadeusClient = new AmadeusClient(amadeusClientId, amadeusClientSecret);
        amadeusClient.setPriceManager(businessLogic.getPriceManager());
    }

    /**
     * Initializes the REST API server with Amadeus credentials
     *
     * @param businessLogic the business logic implementation
     * @param aviationStackApiKey the API key for AviationStack
     * @param amadeusClientId the client ID for Amadeus API
     * @param amadeusClientSecret the client secret for Amadeus API
     */
    public APIServer(BusinessLogic businessLogic, String aviationStackApiKey,
            String amadeusClientId, String amadeusClientSecret) {
        this.businessLogic = businessLogic;
        this.aviationStackClient = new AviationStackClient(aviationStackApiKey);
        
        this.amadeusClient = new AmadeusClient(amadeusClientId, amadeusClientSecret);
        amadeusClient.setPriceManager(businessLogic.getPriceManager());
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
                // Employee routes
                crud("employees/{employee-id}", new EmployeeResource(businessLogic.getEmployeeManager()));

                // Flight routes
                FlightResource flightResource = new FlightResource(
                        businessLogic.getFlightManager(),
                        aviationStackClient,
                        amadeusClient
                );

                PriceResource priceResource = new PriceResource(
                        businessLogic.getPriceManager(),
                        businessLogic.getFlightManager()
                );

                path("flights", () -> {
                    get("/", flightResource::getAll);
                    get("/search", flightResource::searchFlights);
                    delete("/cache", flightResource::clearCache);

                    path("/price", () -> {
                        get("/", priceResource::getAll);
                        post("/create", priceResource::create);
                    });
                });
                
                // Authentication routes
                post("login", ctx -> {
                    LoginRequest loginRequest = ctx.bodyAsClass(LoginRequest.class);
                    boolean success = businessLogic.getLoginService().login(loginRequest.email(), loginRequest.password());
                    if (success) {
                        ctx.status(200).json(Map.of("message", "Login successful"));
                    }
                    else {
                        ctx.status(401).json(Map.of("error", "Invalid email or password"));
                    }
                });

                get("getLoginUser", ctx -> {
                    String email = ctx.queryParam("email");
                    EmployeeData employeeData = businessLogic.getEmployeeManager().getByEmail(email);
                    if (employeeData != null) {
                        ctx.status(200).json(employeeData);
                    } else {
                        ctx.status(404).json(Map.of("error", "User not found"));
                    }
                });
                
                // Booking routes
                BookingResource bookingResource = new BookingResource(businessLogic.getBookingManager());
                path("bookings", () -> {
                    get("/check-customer", bookingResource::checkCustomerEmail);
                    post("/", bookingResource::create);
                    get("/", bookingResource::list);
                    get("/{id}", bookingResource::getOne);
                    get("/customer/{customerId}", bookingResource::getByCustomerId);
                });
            });
        });
        
        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            ctx.status(422).json(Map.of("error", e.getMessage()));
        });

        app.start(configuration.port());
    }
}
