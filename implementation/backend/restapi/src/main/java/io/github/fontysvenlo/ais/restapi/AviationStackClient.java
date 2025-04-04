package io.github.fontysvenlo.ais.restapi;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Client for interacting with the AviationStack API.
 */
public class AviationStackClient {

    private static final Logger logger = LoggerFactory.getLogger(AviationStackClient.class);
    private static final String API_BASE_URL = "http://api.aviationstack.com/v1";

    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final int pricePerKm = 11;
    
    // Cache for storing flight data
    private Map<String, CachedData> flightCache = new ConcurrentHashMap<>();
    // Default cache duration (24 hours in milliseconds)
    private static final long CACHE_DURATION_MS = 24 * 60 * 60 * 1000;
    
    // Locks to prevent multiple simultaneous requests for the same data
    private final Map<String, ReentrantLock> cacheLocks = new ConcurrentHashMap<>();

    /**
     * Creates a new AviationStackClient.
     *
     * @param apiKey The API key for the AviationStack API
     */
    public AviationStackClient(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();

        logger.info("AviationStackClient initialized with API key: {}", apiKey);
        
        // Asynchronously preload the cache when the client is created
        preloadCache();
    }
    
    /**
     * Preloads the cache asynchronously to improve first request performance
     */
    public void preloadCache() {
        logger.info("Starting cache preload...");
        CompletableFuture.runAsync(() -> {
            try {
                logger.info("Preloading home flights cache...");
                getAllFlightsHome(); // This will populate the cache
                
                logger.info("Preloading all flights cache...");
                getAllFlights(); // This will populate the cache
            } catch (Exception e) {
                logger.error("Error during cache preloading", e);
            }
        });
    }

    /**
     * Gets all flights currently available from the API.
     *
     * @return A JsonNode containing flight data
     */
    public JsonNode getAllFlights() {
        String cacheKey = "all_flights";
        
        // Check if we have valid cached data
        if (hasCachedData(cacheKey)) {
            logger.info("Returning cached flight data");
            return getCachedData(cacheKey);
        }
        
        // If not in cache, get or create a lock for this cache key
        ReentrantLock lock = cacheLocks.computeIfAbsent(cacheKey, k -> new ReentrantLock());
        
        // If the lock is already held by another thread, it means the data is being fetched
        // Wait for it to finish and then check the cache again
        if (lock.isLocked() && !lock.isHeldByCurrentThread()) {
            logger.info("Another thread is fetching flight data, waiting...");
            // Wait a bit for the other thread to finish
            try {
                Thread.sleep(100);
                // Then recursively call this method to try again
                return getAllFlights();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Thread interrupted while waiting for flight data", e);
            }
        }
        
        // Try to acquire the lock (non-blocking)
        if (lock.tryLock()) {
            try {
                // Check cache again in case another thread populated it while we were waiting
                if (hasCachedData(cacheKey)) {
                    return getCachedData(cacheKey);
                }
                
                // If still not in cache, fetch the data
                String url = API_BASE_URL + "/flights?access_key=" + apiKey;
                logger.info("Fetching flights from URL: {}", url);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                logger.info("API response status: {}", Optional.of(response.statusCode()));

                if (response.statusCode() != 200) {
                    logger.error("Failed to get flights from API: {}", response.body());
                    return objectMapper.createArrayNode();
                }

                JsonNode responseData = parseFlightResponse(response.body());
                // Cache the response
                cacheData(cacheKey, responseData);
                
                return responseData;
            } catch (IOException | InterruptedException e) {
                logger.error("Error while fetching flights", e);
                return objectMapper.createArrayNode();
            } finally {
                lock.unlock();
                // Clean up the lock if needed
                cacheLocks.remove(cacheKey);
            }
        } else {
            // If we couldn't get the lock, another thread is already fetching
            // Wait a bit and try again
            try {
                Thread.sleep(100);
                return getAllFlights();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Thread interrupted while waiting for flight data", e);
                return objectMapper.createArrayNode();
            }
        }
    }

    /**
     * Gets flights from Amsterdam to unique destinations (only one flight per
     * destination) to display on the home screen.
     *
     * @return A JsonNode containing flight data with unique destinations
     */
    public JsonNode getAllFlightsHome() {
        String cacheKey = "home_flights";
        
        // Check if we have valid cached data
        if (hasCachedData(cacheKey)) {
            logger.info("Returning cached home flight data");
            return getCachedData(cacheKey);
        }
        
        // If not in cache, get or create a lock for this cache key
        ReentrantLock lock = cacheLocks.computeIfAbsent(cacheKey, k -> new ReentrantLock());
        
        // If the lock is already held by another thread, it means the data is being fetched
        // Wait for it to finish and then check the cache again
        if (lock.isLocked() && !lock.isHeldByCurrentThread()) {
            logger.info("Another thread is fetching home flight data, waiting...");
            // Wait a bit for the other thread to finish
            try {
                Thread.sleep(100);
                // Then recursively call this method to try again
                return getAllFlightsHome();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Thread interrupted while waiting for home flight data", e);
            }
        }
        
        // Try to acquire the lock (non-blocking)
        if (lock.tryLock()) {
            try {
                // Check cache again in case another thread populated it while we were waiting
                if (hasCachedData(cacheKey)) {
                    return getCachedData(cacheKey);
                }
                
                // If still not in cache, fetch the data
                String url = API_BASE_URL + "/flights?access_key=" + apiKey + "&dep_iata=AMS";
                logger.info("Fetching flights from URL: {}", url);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                logger.info("API response status: {}", Optional.of(response.statusCode()));

                if (response.statusCode() != 200) {
                    logger.error("Failed to get flights from API: {}", response.body());
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
                
                // Cache the filtered results
                cacheData(cacheKey, uniqueDestinationFlights);

                return uniqueDestinationFlights;
            } catch (IOException | InterruptedException e) {
                logger.error("Error while fetching flights", e);
                return objectMapper.createArrayNode();
            } finally {
                lock.unlock();
                // Clean up the lock if needed
                cacheLocks.remove(cacheKey);
            }
        } else {
            // If we couldn't get the lock, another thread is already fetching
            // Wait a bit and try again
            try {
                Thread.sleep(100);
                return getAllFlightsHome();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Thread interrupted while waiting for home flight data", e);
                return objectMapper.createArrayNode();
            }
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
        // No caching for search flights as requested
        
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
                logger.error("Failed to search flights from API: {}", response.body());
                return objectMapper.createArrayNode();
            }

            return parseFlightResponse(response.body());
        } catch (IOException | InterruptedException e) {
            logger.error("Error while searching flights", e);
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
            logger.error("Invalid response format from API");
            return objectMapper.createArrayNode();
        }

        ArrayNode formattedFlights = objectMapper.createArrayNode();

        for (JsonNode flightNode : dataNode) {
            ObjectNode formattedFlight = objectMapper.createObjectNode();

            // Extract and add basic flight information
            formattedFlight.put("id", flightNode.path("flight").path("iata").asText());

            // Add departure information
            ObjectNode departure = objectMapper.createObjectNode();
            JsonNode departureNode = flightNode.path("departure");
            departure.put("airport", departureNode.path("airport").asText());
            departure.put("iata", departureNode.path("iata").asText());
            departure.put("terminal", departureNode.path("terminal").asText());
            departure.put("gate", departureNode.path("gate").asText());
            departure.put("scheduled", departureNode.path("scheduled").asText());
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
            arrival.put("delay", arrivalNode.path("delay").isNull() ? 0 : arrivalNode.path("delay").asInt());
            formattedFlight.set("arrival", arrival);

            // Add additional flight information
            formattedFlight.put("airline", flightNode.path("airline").path("name").asText());
            formattedFlight.put("status", flightNode.path("flight_status").asText());

            //add duration of flight according to the scheduled time
            String scheduledDeparture = departureNode.path("scheduled").asText();
            String scheduledArrival = arrivalNode.path("scheduled").asText();
            int calculatedDuration = calculateDuration(scheduledDeparture, scheduledArrival);

            // Skip flights with zero duration
            if (calculatedDuration <= 0) {
//                logger.warn("Skipping flight {} with invalid duration: {}",
//                        flightNode.path("flight").path("iata").asText(), calculatedDuration);
                continue;
            }

            String Duration = String.valueOf(calculatedDuration);
            formattedFlight.put("duration", Duration);

            //add price of flight according to the duration
            int price = flightPrice(Integer.parseInt(Duration)) / 100;
            formattedFlight.put("price", price);

            formattedFlights.add(formattedFlight);
        }

        return formattedFlights;
    }

    //calculate km using duration * 15 is the km per minute
    //price per km is 11 dummy value for now.
    private int flightPrice(int duration) {
        return (duration * 15) * pricePerKm;
    }

    private int calculateDuration(String scheduledDeparture, String scheduledArrival) {
        try {
            // Parse the ISO-8601 formatted timestamps into OffsetDateTime objects
            OffsetDateTime departureTime = OffsetDateTime.parse(scheduledDeparture);
            OffsetDateTime arrivalTime = OffsetDateTime.parse(scheduledArrival);

            // Calculate duration in minutes between the two times
            long durationMinutes = ChronoUnit.MINUTES.between(departureTime, arrivalTime);

            // Ensure the duration is positive
            if (durationMinutes < 0) {
                logger.warn("Calculated negative duration between {} and {}, possible data error",
                        scheduledDeparture, scheduledArrival);
                // Fallback: use absolute value as a safety measure
                durationMinutes = Math.abs(durationMinutes);
            }

            return (int) durationMinutes;
        } catch (Exception e) {
            logger.error("Error calculating flight duration: ", e);
            // Fallback to a reasonable duration if parsing fails (5 hours)
            return 300;
        }
    }

    /**
     * Inner class to store cached data with timestamp
     */
    private static class CachedData {
        private final JsonNode data;
        private final long timestamp;
        
        public CachedData(JsonNode data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
        
        public JsonNode getData() {
            return data;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION_MS;
        }
    }
    
    /**
     * Checks if valid cached data exists for the given key
     * 
     * @param key The cache key
     * @return true if valid cache data exists, false otherwise
     */
    private boolean hasCachedData(String key) {
        if (flightCache.containsKey(key)) {
            CachedData cachedData = flightCache.get(key);
            if (!cachedData.isExpired()) {
                return true;
            } else {
                // Remove expired data
                flightCache.remove(key);
            }
        }
        return false;
    }
    
    /**
     * Gets cached data for the given key
     * 
     * @param key The cache key
     * @return The cached data, or null if not found
     */
    private JsonNode getCachedData(String key) {
        CachedData cachedData = flightCache.get(key);
        return cachedData != null ? cachedData.getData() : null;
    }
    
    /**
     * Caches data with the given key
     * 
     * @param key The cache key
     * @param data The data to cache
     */
    private void cacheData(String key, JsonNode data) {
        flightCache.put(key, new CachedData(data));
        logger.info("Cached flight data for key: {}", key);
    }
    
    /**
     * Clears all cached data
     */
    public void clearCache() {
        flightCache.clear();
        logger.info("Flight cache cleared");
    }
}
