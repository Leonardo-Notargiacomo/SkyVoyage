package io.github.fontysvenlo.ais.businesslogic;

import io.github.fontysvenlo.ais.businesslogic.api.FlightStatsManager;
import io.github.fontysvenlo.ais.datarecords.FlightStats;
import io.github.fontysvenlo.ais.persistence.api.FlightStatsRepository;

public class FlightStatsManagerImpl implements FlightStatsManager {
    private final FlightStatsRepository flightStatsRepository;

    public FlightStatsManagerImpl(FlightStatsRepository flightStatsRepository) {
        this.flightStatsRepository = flightStatsRepository;
    }

    public FlightStats getStatsForFlight() {
        return flightStatsRepository.getStatsForFlight();
    }
}
