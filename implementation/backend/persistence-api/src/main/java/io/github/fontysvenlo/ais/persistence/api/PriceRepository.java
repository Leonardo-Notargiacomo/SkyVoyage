package io.github.fontysvenlo.ais.persistence.api;

import io.github.fontysvenlo.ais.datarecords.PricePerKmData;

public interface PriceRepository {
    int get();
    void change(PricePerKmData newPrice);
}
