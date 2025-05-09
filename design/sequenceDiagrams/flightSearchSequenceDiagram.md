**# Flight Search - Sequence Diagram**

The following sequence diagram illustrates the process of searching for flights using the AmadeusClient.

```mermaid
sequenceDiagram
    actor User
    participant WebClient as Web Client
    participant FlightResource as FlightResource
    participant AmadeusClient as AmadeusClient
    participant PriceManager as PriceManager
    participant DiscountManager as DiscountManager
    participant AmadeusAPI as Amadeus API
    
    User ->> WebClient: Search for flights<br>(origin, destination, dates)
    WebClient ->> FlightResource: GET /flights/search?originLocationCode=AMS&<br>destinationLocationCode=JFK&departureDate=2023-06-15
    activate FlightResource
    
    FlightResource ->> FlightResource: Extract and validate parameters
    
    FlightResource ->> FlightResource: Create params Map<br>(origin, destination, dates, adults, etc.)
    
    FlightResource ->> AmadeusClient: searchFlightOffersWithParams(params)
    activate AmadeusClient
    
    AmadeusClient ->> AmadeusClient: ensureValidToken()
    activate AmadeusClient
    
    alt Token expired or missing
        AmadeusClient ->> AmadeusAPI: POST /security/oauth2/token
        activate AmadeusAPI
        AmadeusAPI -->> AmadeusClient: Access token
        deactivate AmadeusAPI
        AmadeusClient ->> AmadeusClient: Store token and expiry
    end
    
    deactivate AmadeusClient
    
    AmadeusClient ->> AmadeusClient: Build query string from params
    
    AmadeusClient ->> AmadeusAPI: GET /shopping/flight-offers?{queryString}
    activate AmadeusAPI
    
    AmadeusAPI -->> AmadeusClient: Raw flight offers JSON response
    deactivate AmadeusAPI
    
    AmadeusClient ->> AmadeusClient: processAmadeusResponse()
    activate AmadeusClient
    
    loop For each flight offer
        AmadeusClient ->> AmadeusClient: Process itineraries (outbound/return)
        
        loop For each itinerary
            AmadeusClient ->> AmadeusClient: Extract flights, durations
            
            AmadeusClient ->> PriceManager: getPrice()
            activate PriceManager
            PriceManager -->> AmadeusClient: Return current price per km
            deactivate PriceManager
            
            Note over AmadeusClient: Calculate base price:<br>duration * 15 * price / 100
            
            opt If departure date available
                AmadeusClient ->> DiscountManager: getAllDiscounts()
                activate DiscountManager
                DiscountManager -->> AmadeusClient: Available discounts
                deactivate DiscountManager
                
                AmadeusClient ->> AmadeusClient: calculateDiscountedPrice()<br>Find best applicable discount<br>based on days until departure
            end
        end
    end
    
    deactivate AmadeusClient
    
    AmadeusClient -->> FlightResource: Return processed flight offers
    deactivate AmadeusClient
    
    FlightResource -->> WebClient: Return 200 OK with flight offers JSON
    deactivate FlightResource
    
    WebClient -->> User: Display flight search results
```

This diagram illustrates the complete flow for searching flights:

1. The User initiates a flight search via the Web Client, providing origin, destination, and dates
2. The Web Client sends a GET request to the `/flights/search` endpoint with query parameters
3. The FlightResource controller:
   - Extracts and validates the required parameters
   - Creates a parameter map for the search
   - Calls the AmadeusClient to perform the search
4. The AmadeusClient:
   - Ensures it has a valid authentication token
   - Builds a query string from the parameters
   - Calls the Amadeus API to search for flight offers
   - Processes the response to extract relevant information
   - For each flight, calculates prices based on duration using the PriceManager
   - Applies discounts if applicable using the DiscountManager
   - Returns the processed results to the controller
5. The FlightResource returns the search results to the Web Client
6. The Web Client displays the results to the User

The diagram shows how the system integrates with the external Amadeus API while applying internal pricing and discount rules to the search results. 
