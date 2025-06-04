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
    private static DiscountManager instance;

    DiscountManagerImpl(DiscountRepository discountRepository) {
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

        Validator validator = new Validator();

        if (!(validator.isValidDiscount(discountData))) {
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