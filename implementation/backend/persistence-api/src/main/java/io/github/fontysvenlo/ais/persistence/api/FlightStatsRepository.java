package io.github.fontysvenlo.ais.persistence.api;

import io.github.fontysvenlo.ais.datarecords.FlightStats;

public interface FlightStatsRepository {
    FlightStats getStatsForFlight();
}
