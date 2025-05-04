package io.github.fontysvenlo.ais.businesslogic.api;

import io.github.fontysvenlo.ais.datarecords.DiscountData;
import java.util.List;
import java.util.Optional;

public interface DiscountManager {

    void addDiscount(DiscountData discountData);

    void updateDiscount(DiscountData discountData);

    void deleteDiscount(Integer id);

    List<DiscountData> getAllDiscounts();

    List<DiscountData> getDiscountsByType(String type);

    Optional<DiscountData> getDiscountById(Integer id);

    Boolean validateDiscount(DiscountData discountData);
}
