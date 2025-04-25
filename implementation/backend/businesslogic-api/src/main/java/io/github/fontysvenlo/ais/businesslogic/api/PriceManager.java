package io.github.fontysvenlo.ais.businesslogic.api;

import io.github.fontysvenlo.ais.datarecords.PricePerKmData;

public interface PriceManager {

    int getPrice();
    void setPrice(PricePerKmData newPrice);
    long getPriceVersion();
}
