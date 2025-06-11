package io.github.fontysvenlo.ais.businesslogic;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import io.github.fontysvenlo.ais.businesslogic.api.DiscountManager;
import io.github.fontysvenlo.ais.datarecords.DiscountData;
import io.github.fontysvenlo.ais.persistence.api.DiscountRepository;

public class DiscountManagerImpl implements DiscountManager {

    private final DiscountRepository discountRepository;

    public DiscountManagerImpl(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    @Override
    public void addDiscount(DiscountData discountData) {

        Validator validator = new Validator();

        if (!validator.isDiscountExisting(discountData)){
            throw new NullPointerException("The discount is not initialized");
        }

        if (!validator.isDiscountMoreThanZero(discountData.amount())){
            throw new IllegalArgumentException("Discount amount must be greater than 0");
        }

        if (!validator.isDiscountLessThanHundred(discountData.amount())){
            throw new IllegalArgumentException("Discount amount cannot exceed 100%");
        }

        if (!validator.areDiscountDaysValid(discountData.days())){
            throw new IllegalArgumentException("Days until departure must be greater than 0");
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
    public double calculateDiscountedPrice(double basePrice, OffsetDateTime departureDate) {

        if (departureDate == null) {
            throw new NullPointerException("Departure date cannot be null");
        }

        DiscountData bestDiscount = findBestDiscount(departureDate);

        if (bestDiscount != null) {
            double discountAmount = basePrice * (bestDiscount.amount() / 100.0);
            double finalPrice = basePrice - discountAmount;
            return finalPrice;
        } else {
            return basePrice;
        }
    }

    private DiscountData findBestDiscount(OffsetDateTime departureDate) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime departure = departureDate.toLocalDateTime();
        long daysUntilDeparture = ChronoUnit.DAYS.between(now, departure);

        List<DiscountData> allDiscounts = getAllDiscounts();
        DiscountData bestDiscount = null;
        double bestDiscountAmount = 0.0;

        for (DiscountData discount : allDiscounts) {
            if (daysUntilDeparture <= discount.days()) {
                if (discount.amount() > bestDiscountAmount) {
                    bestDiscountAmount = discount.amount();
                    bestDiscount = discount;
                }
            }
        }

        return bestDiscount;

    }
}

