package io.github.fontysvenlo.ais.persistence;

import io.github.fontysvenlo.ais.datarecords.CustomerData;
import io.github.fontysvenlo.ais.datarecords.PricePerKmData;
import io.github.fontysvenlo.ais.persistence.api.PriceRepository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

public class PriceRepositoryImpl implements PriceRepository {

    private PricePerKmData pricePerKmData;

    private final DataSource db;

    public PriceRepositoryImpl(DBConfig config) {
        this.db = DBProvider.getDataSource(config);
    }

    @Override
    public PricePerKmData get() {
        return pricePerKmData;
    }

    @Override
    public void change(PricePerKmData newPrice) {
        this.pricePerKmData = newPrice;
    }
}
