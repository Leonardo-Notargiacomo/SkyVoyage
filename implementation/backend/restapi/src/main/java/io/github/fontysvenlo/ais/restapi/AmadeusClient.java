package io.github.fontysvenlo.ais.restapi;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AmadeusClient {

    private static final String BASE_URL = "https://test.api.amadeus.com/v2";
    private static final String AUTH_URL = "https://test.api.amadeus.com/v1/security/oauth2/token";

    private final String clientId;
    private final String clientSecret;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    private String accessToken;
    private long tokenExpiry = 0;
    private int pricePerKm = 15; // Example price per km in cents.

    public AmadeusClient(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.objectMapper = new ObjectMapper();
        this.httpClient = createHttpClient();
    }
    
    // Added for testability - allows mocking the HttpClient in tests
    protected HttpClient createHttpClient() {
        return HttpClient.newHttpClient();
    }

    /**
     * Search for flights with basic parameters
     */
    public Map<String, Object> searchFlights(String origin, String destination, String departureDate, String returnDate) throws IOException, InterruptedException {
        Map<String, String> params = Map.of(
                "originLocationCode", origin,
                "destinationLocationCode", destination,
                "departureDate", departureDate,
                "returnDate", returnDate,
                "adults", "1",
                "max", "10"
        );

        return searchFlightOffersWithParams(params);
    }

    /**
     * Search for flights with a map of parameters
     */
    public Map<String, Object> searchFlightOffersWithParams(Map<String, String> params) throws IOException, InterruptedException {
        ensureValidToken();

        StringBuilder queryBuilder = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                queryBuilder.append("&");
            } else {
                first = false;
            }

            queryBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            queryBuilder.append("=");
            queryBuilder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/shopping/flight-offers?" + queryBuilder))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Process the response
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            JsonNode responseJson = objectMapper.readTree(response.body());
            return processAmadeusResponse(responseJson);
        } else {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "API error: " + response.statusCode() + " - " + response.body());
            return errorResponse;
        }
    }

    /**
     * Search for flights with a query string
     */
    public Map<String, Object> searchFlightOffersWithQueryString(String queryString) throws IOException, InterruptedException {
        ensureValidToken();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/shopping/flight-offers?" + queryString))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Process the response
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            JsonNode responseJson = objectMapper.readTree(response.body());
            return processAmadeusResponse(responseJson);
        } else {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "API error: " + response.statusCode() + " - " + response.body());
            return errorResponse;
        }
    }

    /**
     * Process the API response and extract flight information
     */
    private JsonNode processFlightResults(JsonNode responseJson) {
        ArrayNode results = objectMapper.createArrayNode();

        if (responseJson.has("data") && responseJson.get("data").isArray()) {
            JsonNode offers = responseJson.get("data");

            for (JsonNode offer : offers) {
                if (offer.has("itineraries") && offer.get("itineraries").isArray()) {
                    JsonNode itineraries = offer.get("itineraries");
                    JsonNode priceInfo = offer.path("price");

                    for (JsonNode itinerary : itineraries) {
                        if (itinerary.has("segments") && itinerary.get("segments").isArray()) {
                            for (JsonNode segment : itinerary.get("segments")) {
                                ObjectNode flightInfo = objectMapper.createObjectNode();

                                // Departure info
                                if (segment.has("departure")) {
                                    flightInfo.set("departure", segment.get("departure"));
                                }

                                // Arrival info
                                if (segment.has("arrival")) {
                                    flightInfo.set("arrival", segment.get("arrival"));
                                }

                                // Carrier info
                                if (segment.has("carrierCode")) {
                                    flightInfo.put("airline", segment.get("carrierCode").asText());
                                }

                                // Flight number
                                if (segment.has("number")) {
                                    flightInfo.put("flightNumber", segment.get("number").asText());
                                }

                                // Price
                                // Extract duration from segment and calculate price
                                if (segment.has("duration")) {
                                    String durationStr = segment.get("duration").asText();
                                    // Convert ISO 8601 duration (like PT2H30M) to minutes
                                    int durationMinutes = parseDurationToMinutes(durationStr);
                                    flightInfo.put("price", String.valueOf(flightPrice(durationMinutes)));
                                } else if (priceInfo.has("total")) {
                                    // Fallback to API price if duration not available
                                    flightInfo.put("price", priceInfo.get("total").asText());
                                }

                                // Currency
                                if (priceInfo.has("currency")) {
                                    flightInfo.put("currency", priceInfo.get("currency").asText());
                                }

                                results.add(flightInfo);
                            }
                        }
                    }
                }
            }
        }

        return results;
    }

    /**
     * Processes the Amadeus API response to group flights by trip (outbound and
     * return)
     *
     * @param amadeusResponse The raw response from Amadeus API
     * @return A restructured response with flights grouped by trip
     */
    private Map<String, Object> processAmadeusResponse(JsonNode amadeusResponse) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> processedFlightOffers = new ArrayList<>();

        // Copy meta information if it exists
        if (amadeusResponse.has("meta")) {
            Map<String, Object> meta = new HashMap<>();
            JsonNode metaNode = amadeusResponse.get("meta");
            if (metaNode.has("count")) {
                meta.put("count", metaNode.get("count").asInt());
            }
            result.put("meta", meta);
        }

        // Process each flight offer
        if (amadeusResponse.has("data")) {
            for (JsonNode flightOffer : amadeusResponse.get("data")) {
                Map<String, Object> processedOffer = new HashMap<>();

                // Copy basic flight offer information
                processedOffer.put("id", flightOffer.get("id").asText());
                processedOffer.put("source", flightOffer.get("source").asText());

                // Process price information
                if (flightOffer.has("price")) {
                    JsonNode priceNode = flightOffer.get("price");
                    Map<String, Object> price = new HashMap<>();
                    price.put("currency", priceNode.get("currency").asText());
                    price.put("total", priceNode.get("total").asText());
                    price.put("grandTotal", priceNode.get("grandTotal").asText());
                    processedOffer.put("price", price);
                }

                // Process itineraries (trips)
                List<Map<String, Object>> trips = new ArrayList<>();
                if (flightOffer.has("itineraries")) {
                    JsonNode itineraries = flightOffer.get("itineraries");

                    // First itinerary is always outbound
                    if (itineraries.size() > 0) {
                        Map<String, Object> outboundTrip = processItinerary(itineraries.get(0), "outbound", flightOffer);
                        trips.add(outboundTrip);
                    }

                    // Second itinerary (if exists) is return
                    if (itineraries.size() > 1) {
                        Map<String, Object> returnTrip = processItinerary(itineraries.get(1), "return", flightOffer);
                        trips.add(returnTrip);
                    }
                }

                processedOffer.put("trips", trips);
                processedFlightOffers.add(processedOffer);
            }
        }

        result.put("data", processedFlightOffers);

        // Copy dictionaries for reference data
        if (amadeusResponse.has("dictionaries")) {
            Map<String, Object> dictionaries = new HashMap<>();
            JsonNode dictionariesNode = amadeusResponse.get("dictionaries");

            // Process carriers dictionary
            if (dictionariesNode.has("carriers")) {
                Map<String, String> carriers = new HashMap<>();
                dictionariesNode.get("carriers").fields().forEachRemaining(entry
                        -> carriers.put(entry.getKey(), entry.getValue().asText())
                );
                dictionaries.put("carriers", carriers);
            }

            // Process locations dictionary
            if (dictionariesNode.has("locations")) {
                Map<String, Map<String, String>> locations = new HashMap<>();
                dictionariesNode.get("locations").fields().forEachRemaining(entry -> {
                    Map<String, String> location = new HashMap<>();
                    JsonNode locationNode = entry.getValue();
                    if (locationNode.has("cityCode")) {
                        location.put("cityCode", locationNode.get("cityCode").asText());
                    }
                    if (locationNode.has("countryCode")) {
                        location.put("countryCode", locationNode.get("countryCode").asText());
                    }
                    locations.put(entry.getKey(), location);
                });
                dictionaries.put("locations", locations);
            }

            // Process aircraft dictionary
            if (dictionariesNode.has("aircraft")) {
                Map<String, String> aircraft = new HashMap<>();
                dictionariesNode.get("aircraft").fields().forEachRemaining(entry
                        -> aircraft.put(entry.getKey(), entry.getValue().asText())
                );
                dictionaries.put("aircraft", aircraft);
            }

            result.put("dictionaries", dictionaries);
        }

        return result;
    }

    /**
     * Process a single itinerary (outbound or return trip)
     */
    private Map<String, Object> processItinerary(JsonNode itinerary, String tripType, JsonNode parentOffer) {
        Map<String, Object> trip = new LinkedHashMap<>();
        trip.put("type", tripType);
        trip.put("duration", itinerary.get("duration").asText());

        // Process segments (individual flights in this trip)
        List<Map<String, Object>> flights = new ArrayList<>();
        if (itinerary.has("segments")) {
            for (JsonNode segment : itinerary.get("segments")) {
                Map<String, Object> flight = new LinkedHashMap<>();
                flight.put("id", segment.get("id").asText());
                flight.put("duration", segment.get("duration").asText());
                flight.put("number", segment.get("number").asText());
                flight.put("carrierCode", segment.get("carrierCode").asText());

                // Process departure first
                JsonNode departureNode = segment.get("departure");
                Map<String, Object> departure = new LinkedHashMap<>();
                departure.put("iata", departureNode.get("iataCode").asText());
                if (departureNode.has("terminal")) {
                    departure.put("terminal", departureNode.get("terminal").asText());
                }
                departure.put("scheduled", departureNode.get("at").asText());
                flight.put("departure", departure);

                // Process arrival second
                JsonNode arrivalNode = segment.get("arrival");
                Map<String, Object> arrival = new LinkedHashMap<>();
                arrival.put("iata", arrivalNode.get("iataCode").asText());
                if (arrivalNode.has("terminal")) {
                    arrival.put("terminal", arrivalNode.get("terminal").asText());
                }
                arrival.put("scheduled", arrivalNode.get("at").asText());
                flight.put("arrival", arrival);

                flights.add(flight);
            }
        }

        trip.put("flights", flights);
        return trip;
    }

    /**
     * Calculate flight price based on duration
     */
    private int flightPrice(int duration) {
        return (duration * 15) * pricePerKm;
    }

    /**
     * Parse ISO 8601 duration format (PT2H30M) to minutes
     */
    private int parseDurationToMinutes(String duration) {
        // Remove PT prefix
        String time = duration.substring(2);
        int minutes = 0;

        // Find hours
        int hIndex = time.indexOf('H');
        if (hIndex > 0) {
            minutes += Integer.parseInt(time.substring(0, hIndex)) * 60;
            time = time.substring(hIndex + 1);
        }

        // Find minutes
        int mIndex = time.indexOf('M');
        if (mIndex > 0) {
            minutes += Integer.parseInt(time.substring(0, mIndex));
        }

        return minutes;
    }

    /**
     * Ensure we have a valid access token
     */
    private void ensureValidToken() throws IOException, InterruptedException {
        long now = System.currentTimeMillis();

        // If token is expired or will expire soon, get a new one
        if (accessToken == null || now >= tokenExpiry - 60000) { // 1 minute buffer
            // Check if credentials are provided
            if (clientId == null || clientId.isEmpty() || clientSecret == null || clientSecret.isEmpty()) {
                throw new IOException("Amadeus API credentials are not configured. "
                        + "Please check your environment variables AMADEUS_API_KEY and AMADEUS_API_SECRET.");
            }

            // Build the token request
            String authData = "grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(AUTH_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(authData))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode tokenResponse = objectMapper.readTree(response.body());
                accessToken = tokenResponse.path("access_token").asText();
                int expiresIn = tokenResponse.path("expires_in").asInt(1800); // Default 30 minutes
                tokenExpiry = now + (expiresIn * 1000);
            } else {
                throw new IOException("Failed to get access token: " + response.statusCode() + " - " + response.body());
            }
        }
    }
}
