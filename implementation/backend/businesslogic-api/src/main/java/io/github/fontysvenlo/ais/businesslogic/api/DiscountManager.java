package io.github.fontysvenlo.ais.businesslogic.api;

import java.util.List;

import io.github.fontysvenlo.ais.datarecords.DiscountData;

public interface DiscountManager {

    void addDiscount(DiscountData discountData);

    void deleteDiscount(Integer id);

    List<DiscountData> getAllDiscounts();
}
