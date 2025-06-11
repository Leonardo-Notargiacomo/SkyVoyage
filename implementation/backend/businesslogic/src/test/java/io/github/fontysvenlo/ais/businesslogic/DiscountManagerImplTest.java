package io.github.fontysvenlo.ais.businesslogic;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.fontysvenlo.ais.businesslogic.api.DiscountManager;
import io.github.fontysvenlo.ais.datarecords.DiscountData;
import io.github.fontysvenlo.ais.persistence.api.DiscountRepository;

public class DiscountManagerImplTest {

    private DiscountRepository discountRepository;
    private DiscountManager discountManager;

    @BeforeEach
    void setUp() {
        this.discountRepository = mock(DiscountRepository.class);
        this.discountManager = DiscountManagerImpl.getInstance(discountRepository);
    }

    @Test
    void testAddDiscountThrowsExceptionForInvalidInput() {
        DiscountData invalidDiscount = new DiscountData(6, "Invalid", 110.0, "regular", 6, 10);

        assertThrows(IllegalArgumentException.class, () -> {
            discountManager.addDiscount(invalidDiscount);
        });

        verify(discountRepository, never()).add(any());
    }

    @Test
    void testCalculateDiscountedPriceWithNoApplicableDiscount() {
        double basePrice = 100.0;
        OffsetDateTime departureDate = OffsetDateTime.now().plusDays(50); // 50 days in future

        // Mock discounts that don't apply (require booking within fewer days)
        List<DiscountData> discounts = List.of(new DiscountData(1, "Early Bird", 15.0, "early_bird", 1, 30), // Requires booking within 30 days
                new DiscountData(2, "Super Early", 25.0, "early_bird", 1, 20)  // Requires booking within 20 days
        );

        when(discountRepository.getAll()).thenReturn(discounts);

        double result = discountManager.calculateDiscountedPrice(basePrice, departureDate);

        assertEquals(basePrice, result, 0.01); // Should return original price
    }

    @Test
    void testCalculateDiscountedPriceWithEmptyDiscountList() {
        double basePrice = 100.0;
        OffsetDateTime departureDate = OffsetDateTime.now().plusDays(15);

        when(discountRepository.getAll()).thenReturn(List.of());

        double result = discountManager.calculateDiscountedPrice(basePrice, departureDate);

        assertEquals(basePrice, result, 0.01); // Should return original price
    }

    @Test
    void testCalculateDiscountedPriceWithZeroDiscount() {
        double basePrice = 100.0;
        OffsetDateTime departureDate = OffsetDateTime.now().plusDays(15);

        List<DiscountData> discounts = List.of(new DiscountData(1, "No Discount", 0.0, "special", 1, 30));

        when(discountRepository.getAll()).thenReturn(discounts);

        double result = discountManager.calculateDiscountedPrice(basePrice, departureDate);

        assertEquals(basePrice, result, 0.01); // Should return original price (0% discount)
    }

}
