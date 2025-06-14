package io.github.fontysvenlo.ais.persistence.api;

import io.github.fontysvenlo.ais.datarecords.DiscountData;

import java.util.List;

public interface DiscountRepository {

    DiscountData add(DiscountData discountData);

    DiscountData delete(Integer id);

    DiscountData getOne(Integer id);

    List<DiscountData> getAll();
}
