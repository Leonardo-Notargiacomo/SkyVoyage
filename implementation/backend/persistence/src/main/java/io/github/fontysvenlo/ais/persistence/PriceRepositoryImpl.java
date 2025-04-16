package io.github.fontysvenlo.ais.persistence;

import io.github.fontysvenlo.ais.datarecords.CustomerData;
import io.github.fontysvenlo.ais.datarecords.PricePerKmData;
import io.github.fontysvenlo.ais.persistence.api.PriceRepository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

public class PriceRepositoryImpl implements PriceRepository {

    private final DataSource db;
    private PricePerKmData pricePerKmData;

    public PriceRepositoryImpl(DBConfig config) {
        this.db = DBProvider.getDataSource(config);
        this.pricePerKmData = new PricePerKmData(15);
    }

    @Override
    public int get() {
        return pricePerKmData.price();
    }

    @Override
    public void change(PricePerKmData newPrice) {
        this.pricePerKmData = newPrice;
    }
}
