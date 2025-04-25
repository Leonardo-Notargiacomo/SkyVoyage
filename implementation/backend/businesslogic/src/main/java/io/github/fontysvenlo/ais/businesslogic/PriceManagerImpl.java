package io.github.fontysvenlo.ais.businesslogic;

import io.github.fontysvenlo.ais.businesslogic.api.PriceManager;
import io.github.fontysvenlo.ais.datarecords.PricePerKmData;
import io.github.fontysvenlo.ais.persistence.api.PriceRepository;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import javax.naming.Context;
import java.util.Map;

public class PriceManagerImpl implements PriceManager {

    private final PriceRepository priceRepository;
    private static PriceManager instance;
    private static final Logger logger = Logger.getLogger(PriceManagerImpl.class.getName());
    private final AtomicLong priceVersion = new AtomicLong(0);

    private int defaultPrice = 11;

    private PriceManagerImpl(PriceRepository priceRepository){
        this.priceRepository = priceRepository;
    }

    public static synchronized PriceManager getInstance(PriceRepository priceRepository) {
        if (instance == null) {
            instance = new PriceManagerImpl(priceRepository);
        }
        return instance;
    }

    @Override
    public int getPrice() {
        try {
            int price = priceRepository.get();
            if (price != 0) {
                return price;
            } else {
                return defaultPrice; // Default value if price data is null
            }
        } catch (Exception e) {
            logger.warning("Error retrieving price: " + e.getMessage());
            return defaultPrice; // Default value if there's an exception
        }
    }

    @Override
    public void setPrice(PricePerKmData newPrice) {
        priceRepository.change(newPrice);
        priceVersion.incrementAndGet();
    }

    @Override
    public long getPriceVersion() {
        return priceVersion.get();
    }


}
