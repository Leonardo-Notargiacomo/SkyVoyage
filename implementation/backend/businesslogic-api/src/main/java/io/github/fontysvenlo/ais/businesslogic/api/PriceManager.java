package io.github.fontysvenlo.ais.businesslogic.api;

import io.github.fontysvenlo.ais.datarecords.PricePerKmData;

public interface PriceManager {

    PricePerKmData getPrice();
    void setPrice(PricePerKmData newPrice);
}
