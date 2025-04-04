package io.github.fontysvenlo.ais.persistence.api;

import io.github.fontysvenlo.ais.datarecords.FlightData;

import java.util.List;

public interface FlightRepository {
    FlightData add(FlightData flightData);

    FlightData delete(String id);

    FlightData getOne(String id);

    List<FlightData> getAll();
}
