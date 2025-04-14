package io.github.fontysvenlo.ais.persistence.api;

import io.github.fontysvenlo.ais.datarecords.PricePerKmData;

public interface PriceRepository {
    PricePerKmData get();
    void change(PricePerKmData newPrice);
}
