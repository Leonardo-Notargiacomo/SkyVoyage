package io.github.fontysvenlo.ais.businesslogic;

import io.github.fontysvenlo.ais.businesslogic.api.FlightManager;
import io.github.fontysvenlo.ais.datarecords.FlightData;
import io.github.fontysvenlo.ais.persistence.api.FlightRepository;

import java.util.List;

public class FlightManagerImpl implements FlightManager {
    private final FlightRepository flightRepository;

    public FlightManagerImpl(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    @Override
    public FlightData add(FlightData flightData) {
        return flightRepository.add(flightData);
    }

    @Override
    public List<FlightData> list() {
        return flightRepository.getAll();
    }

    @Override
    public FlightData delete(String id) {
        return flightRepository.delete(id);
    }


    @Override
    public FlightData getOne(String id) {
        return flightRepository.getOne(id);
    }

    @Override
    public List<FlightData> search(String airline, String flightNumber, String departureIata, String arrivalIata) {
        return List.of();
    }
}
