package io.github.fontysvenlo.ais.restapi;

import java.util.ArrayList;
import java.util.List;  // Add this import for List
import java.util.Map;  // Add this import for ArrayList

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
        amadeusClient.setPriceManager(businessLogic.getPriceManager()); // Set PriceManager for AmadeusClient
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
                crud("employees/{employee-id}", new EmployeeResource(businessLogic.getEmployeeManager()));

                FlightResource flightResource = new FlightResource(
                        businessLogic.getFlightManager(),
                        aviationStackClient,
                        amadeusClient
                );

                PriceResource priceResource = new PriceResource(
                        businessLogic.getPriceManager(),
                        businessLogic.getFlightManager()
                );

                // Add custom endpoint to refresh flight data
                // Replace automatic CRUD with explicit path definitions
                // Define flight paths
                path("flights", () -> {
                    // GET operations
                    get("/", flightResource::getAll);
                    // Add search endpoint
                    get("/search", flightResource::searchFlights);
                    // Add a endpoint to clear the flight data
                    delete("/cache", flightResource::clearCache);

                    path("/price", () -> {
                        get("/", priceResource::getAll);
                        post("/create", priceResource::create);
                    });
                });
              
                post("login", ctx -> {
                    LoginRequest loginRequest = ctx.bodyAsClass(LoginRequest.class);
                    boolean success = businessLogic.getLoginService().login(loginRequest.email(), loginRequest.password());
                    if (success) {
                        ctx.status(200).json(Map.of(
                                "message", "Login successful"));
                    }
                    else {
                        ctx.status(401).json(Map.of(
                                "error", "Invalid email or password"));
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
                
                // Directly define the booking creation route
                path("bookings", () -> {

                    get("/check-customer", ctx -> {
                        BookingResource resource = new BookingResource(businessLogic.getBookingManager());
                        resource.checkCustomerEmail(ctx);
                    });

                    post("/", ctx -> {
                        try {
                            // Extract booking details from the request body as a Map
                            Map<String, Object> bookingMap = ctx.bodyAsClass(Map.class);
                            
                            // Log the received booking data with explicit checking for return flight
                            System.out.println("Received booking data with structure: " + bookingMap.keySet());
                            
                            // Check specifically for return flight
                            boolean hasReturnFlight = bookingMap.containsKey("returnFlight");
                            System.out.println("Has returnFlight property: " + hasReturnFlight);
                            
                            if (hasReturnFlight) {
                                System.out.println("Return flight data: " + bookingMap.get("returnFlight"));
                            }
                            
                            // Check main flights array
                            if (bookingMap.containsKey("mainFlights")) {
                                List<Map<String, Object>> mainFlights = (List<Map<String, Object>>) bookingMap.get("mainFlights");
                                System.out.println("mainFlights array size: " + mainFlights.size());
                                for (int i = 0; i < mainFlights.size(); i++) {
                                    System.out.println("Flight " + i + " ID: " + mainFlights.get(i).get("id"));
                                }
                            }
                            
                            // More comprehensive validation for booking data
                            if (bookingMap != null) {
                                boolean hasValidFlightData = false;
                                
                                // Check all possible flight data sources
                                if (bookingMap.containsKey("outboundFlight")) {
                                    Map<String, Object> outbound = (Map<String, Object>) bookingMap.get("outboundFlight");
                                    if (hasValidFlightIdentifiers(outbound)) {
                                        hasValidFlightData = true;
                                        System.out.println("Found valid outbound flight");
                                    }
                                }
                                
                                if (bookingMap.containsKey("returnFlight")) {
                                    Map<String, Object> returnFlight = (Map<String, Object>) bookingMap.get("returnFlight");
                                    if (hasValidFlightIdentifiers(returnFlight)) {
                                        hasValidFlightData = true;
                                        System.out.println("Found valid return flight");
                                    }
                                }
                                
                                if (bookingMap.containsKey("flight")) {
                                    Map<String, Object> flight = (Map<String, Object>) bookingMap.get("flight");
                                    if (hasValidFlightIdentifiers(flight)) {
                                        hasValidFlightData = true;
                                        System.out.println("Found valid main flight");
                                    }
                                }
                                
                                if (bookingMap.containsKey("mainFlights")) {
                                    List<Map<String, Object>> mainFlights = (List<Map<String, Object>>) bookingMap.get("mainFlights");
                                    if (mainFlights != null && !mainFlights.isEmpty()) {
                                        for (Map<String, Object> flight : mainFlights) {
                                            if (hasValidFlightIdentifiers(flight)) {
                                                hasValidFlightData = true;
                                                System.out.println("Found valid flight in mainFlights array");
                                                break;
                                            }
                                        }
                                    }
                                }
                                
                                // Process the booking if we have valid flight data
                                if (hasValidFlightData) {
                                    // Ensure we have a mainFlights array for consistency
                                    if (!bookingMap.containsKey("mainFlights")) {
                                        List<Map<String, Object>> mainFlights = new ArrayList<>();
                                        
                                        // Process outbound flight if present
                                        if (bookingMap.containsKey("outboundFlight")) {
                                            Map<String, Object> outbound = (Map<String, Object>) bookingMap.get("outboundFlight");
                                            if (outbound != null) {
                                                ensureFlightId(outbound);
                                                mainFlights.add(outbound);
                                            }
                                        }
                                        
                                        // Process return flight if present
                                        if (bookingMap.containsKey("returnFlight")) {
                                            Map<String, Object> returnFlight = (Map<String, Object>) bookingMap.get("returnFlight");
                                            if (returnFlight != null) {
                                                ensureFlightId(returnFlight);
                                                mainFlights.add(returnFlight);
                                            }
                                        }
                                        
                                        // Use regular flight if no outbound/return
                                        if (mainFlights.isEmpty() && bookingMap.containsKey("flight")) {
                                            Map<String, Object> flight = (Map<String, Object>) bookingMap.get("flight");
                                            if (flight != null) {
                                                ensureFlightId(flight);
                                                mainFlights.add(flight);
                                            }
                                        }
                                        
                                        // Add the list to the booking map
                                        if (!mainFlights.isEmpty()) {
                                            bookingMap.put("mainFlights", mainFlights);
                                        }
                                    }
                                    
                                    // Create the booking
                                    Map<String, Object> result = businessLogic.getBookingManager().addSimple(bookingMap);
                                    ctx.status(201).json(result);
                                } else {
                                    ctx.status(400).json(Map.of("error", "Invalid booking details - valid flight data is required"));
                                }
                            } else {
                                ctx.status(400).json(Map.of("error", "Invalid booking details - request body is null"));
                            }
                        } catch (Exception e) {
                            // Log the error and respond with an error message
                            System.err.println("Error processing booking request: " + e.getMessage());
                            e.printStackTrace();
                            ctx.status(500).json(Map.of("error", "Failed to process booking: " + e.getMessage()));
                        }
                    });
                });
            });
        });
        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            ctx.status(422).json(Map.of("error", e.getMessage()));
        });

        app.start(configuration.port());
    }

    // Helper method to check if flight has valid identifiers
    private boolean hasValidFlightIdentifiers(Map<String, Object> flight) {
        if (flight == null) return false;
        
        // Check for ID directly
        if (flight.containsKey("id") && flight.get("id") != null) {
            return true;
        }
        
        // Check if we can build an ID from IATA codes
        if (flight.containsKey("departureAirportShort") && flight.containsKey("arrivalAirportShort") &&
            flight.get("departureAirportShort") != null && flight.get("arrivalAirportShort") != null) {
            return true;
        }
        
        return false;
    }

    // Helper method to ensure flight has an ID
    private void ensureFlightId(Map<String, Object> flight) {
        if (flight == null) return;
        
        // If no ID but we have IATA codes, generate one
        if (!flight.containsKey("id") || flight.get("id") == null) {
            if (flight.containsKey("departureAirportShort") && flight.containsKey("arrivalAirportShort")) {
                String depCode = (String) flight.get("departureAirportShort");
                String arrCode = (String) flight.get("arrivalAirportShort");
                if (depCode != null && arrCode != null) {
                    flight.put("id", depCode + "-" + arrCode);
                    System.out.println("Generated flight ID: " + depCode + "-" + arrCode);
                }
            }
        }
    }
}
