package io.github.fontysvenlo.ais.businesslogic.api;

import java.util.List;

import io.github.fontysvenlo.ais.datarecords.FlightData;

/**
 * Interface for managing flight data.
 */
public interface FlightManager {
    
    /**
     * Gets all available flights.
     * 
     * @return A list of all flights
     */
    List<FlightData> list();

    FlightData delete(String id);

    /**
     * Gets a flight by its ID.
     * 
     * @param id The flight ID (IATA code)
     * @return The flight data, or null if not found
     */
    FlightData getOne(String id);
    
    /**
     * Searches for flights based on various criteria.
     * 
     * @param airline IATA airline code
     * @param flightNumber Flight number
     * @param departureIata IATA code of departure airport
     * @param arrivalIata IATA code of arrival airport
     * @return A list of flights matching the criteria
     */
    List<FlightData> search(String airline, String flightNumber, String departureIata, String arrivalIata);
    
    /**
     * Adds a flight to the system.
     * 
     * @param flightData The flight data to add
     * @return The added flight data
     */
    FlightData add(FlightData flightData);
}
