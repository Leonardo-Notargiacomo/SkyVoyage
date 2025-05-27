package io.github.fontysvenlo.ais.businesslogic;

import io.github.fontysvenlo.ais.businesslogic.api.DiscountManager;
import io.github.fontysvenlo.ais.datarecords.DiscountData;
import io.github.fontysvenlo.ais.persistence.api.DiscountRepository;

import java.util.List;
import java.util.Optional;

public class DiscountManagerImpl implements DiscountManager {

    private final DiscountRepository discountRepository;
    private static DiscountManager instance;

    DiscountManagerImpl(DiscountRepository discountRepository){
        this.discountRepository = discountRepository;
    }

    public static synchronized DiscountManager getInstance(DiscountRepository discountRepository) {
        if (instance == null) {
            instance = new DiscountManagerImpl(discountRepository);
        }
        return instance;
    }

    @Override
    public void addDiscount(DiscountData discountData) {
        if (!validateDiscount(discountData)) {
            throw new IllegalArgumentException("Invalid discount percentage. Please enter a value between 0 and 100.");
        }
        discountRepository.add(discountData);
    }

    @Override
    public void deleteDiscount(Integer id) {
        discountRepository.delete(id);
    }

    public List<DiscountData> getAllDiscounts() {
        return discountRepository.getAll();
    }

    @Override
    public Boolean validateDiscount(DiscountData discountData) {
        return discountData.amount() >= 0 && discountData.amount() <= 100 && discountData.days() > 0;
    }
}
