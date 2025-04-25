package io.github.fontysvenlo.ais.persistence;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import io.github.fontysvenlo.ais.businesslogic.api.PriceManager;
import io.github.fontysvenlo.ais.datarecords.FlightData;
import io.github.fontysvenlo.ais.persistence.api.FlightRepository;

/**
 * This class knows everything about storing and retrieving flights from the
 * database. At the moment only stores flights in memory. Normally it will
 * connect to a database and do all the handling.
 */
class FlightRepositoryImpl implements FlightRepository {

    private final DataSource db;
    // Initialize with empty list - will be populated through API
    private final List<FlightData> flights = new ArrayList<>();
    private long cachedPriceVersion = -1;
    private PriceManager priceManager;

    public FlightRepositoryImpl(DBConfig config) {
        this.db = DBProvider.getDataSource(config);
    }

    @Override
    public FlightData add(FlightData flightData) {
        // Check if flight with this ID already exists
        FlightData existingFlight = flights.stream()
                .filter(flight -> flight.id().equals(flightData.id()))
                .findFirst()
                .orElse(null);

        if (existingFlight != null) {
            // Replace existing flight
            flights.remove(existingFlight);
        }

        flights.add(flightData);
        return flightData;
    }

    @Override
    public FlightData delete(String id) {
        FlightData flightToRemove = flights.stream()
                .filter(flight -> flight.id().equals(id))
                .findFirst()
                .orElse(null);

        if (flightToRemove != null) {
            flights.remove(flightToRemove);
            return flightToRemove;
        }
        return null;
    }

    @Override
    public FlightData getOne(String id) {
        return flights.stream()
                .filter(flight -> flight.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<FlightData> getAll() {

        if (priceManager != null) {
            long currentVersion = priceManager.getPriceVersion();
            if (currentVersion != cachedPriceVersion) {
                flights.clear();
                cachedPriceVersion = currentVersion;
            }
        }

        return new ArrayList<>(flights);
    }

    public void setPriceManager(PriceManager priceManager) {
        this.priceManager = priceManager;
    }
}
