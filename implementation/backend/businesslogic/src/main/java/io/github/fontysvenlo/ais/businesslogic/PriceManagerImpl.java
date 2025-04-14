package io.github.fontysvenlo.ais.businesslogic;

import io.github.fontysvenlo.ais.businesslogic.api.PriceManager;
import io.github.fontysvenlo.ais.datarecords.PricePerKmData;
import io.github.fontysvenlo.ais.persistence.api.PriceRepository;

import javax.naming.Context;
import java.util.Map;

public class PriceManagerImpl implements PriceManager {

    private final PriceRepository priceRepository;

    public PriceManagerImpl(PriceRepository priceRepository){
        this.priceRepository = priceRepository;
    }

    @Override
    public PricePerKmData getPrice() { return priceRepository.get(); }

    @Override
    public void setPrice(PricePerKmData newPrice) {
        priceRepository.change(newPrice);
    }

}
