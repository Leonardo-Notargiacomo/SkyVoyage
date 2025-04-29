package io.github.fontysvenlo.ais.businesslogic.api;

import io.github.fontysvenlo.ais.datarecords.DiscountData;
import java.util.List;
import java.util.Optional;

public interface DiscountManager {

    DiscountData createDiscount(DiscountData discountData);

    DiscountData updateDiscount(DiscountData discountData);

    DiscountData deleteDiscount(Integer id);

    List<DiscountData> getAllDiscounts();

    List<DiscountData> getDiscountsByType(String type);

    Optional<DiscountData> getDiscountById(Integer id);
}
