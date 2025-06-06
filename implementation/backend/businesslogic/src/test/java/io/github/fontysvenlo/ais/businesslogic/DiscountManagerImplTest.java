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
    void testGetAllDiscounts() {
        List<DiscountData> discounts = List.of(
                new DiscountData(1, "Discount 1", 10.0, "type1", 1, 5),
                new DiscountData(2, "Discount 2", 20.0, "type2", 2, 10));

        when(discountRepository.getAll()).thenReturn(discounts);

        List<DiscountData> result = discountManager.getAllDiscounts();

        assertEquals(2, result.size());
        assertEquals("Discount 1", result.get(0).name());
        assertEquals("Discount 2", result.get(1).name());
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
    void testCalculateDiscountedPriceWithSingleApplicableDiscount() {
        double basePrice = 100.0;
        OffsetDateTime departureDate = OffsetDateTime.now().plusDays(25); // 25 days in future

        List<DiscountData> discounts = List.of(new DiscountData(1, "Early Bird", 15.0, "early_bird", 1, 30), // Applies (25 <= 30)
                new DiscountData(2, "Super Early", 25.0, "early_bird", 1, 20)  // Doesn't apply (25 > 20)
        );

        when(discountRepository.getAll()).thenReturn(discounts);

        double result = discountManager.calculateDiscountedPrice(basePrice, departureDate);

        double expectedPrice = 100.0 - (100.0 * 0.15); // 15% off
        assertEquals(expectedPrice, result, 0.01);
    }

    @Test
    void testCalculateDiscountedPriceWithMultipleApplicableDiscounts() {
        double basePrice = 100.0;
        OffsetDateTime departureDate = OffsetDateTime.now().plusDays(15); // 15 days in future

        List<DiscountData> discounts = List.of(new DiscountData(1, "Early Bird", 15.0, "early_bird", 1, 30), // Applies (15 <= 30)
                new DiscountData(2, "Super Early", 25.0, "early_bird", 1, 20), // Applies (15 <= 20)
                new DiscountData(3, "Last Minute", 10.0, "last_minute", 1, 10) // Applies (15 <= 10 is false, so doesn't apply)
        );

        when(discountRepository.getAll()).thenReturn(discounts);

        double result = discountManager.calculateDiscountedPrice(basePrice, departureDate);

        // Should apply the highest discount (25%)
        double expectedPrice = 100.0 - (100.0 * 0.25);
        assertEquals(expectedPrice, result, 0.01);
    }

    @Test
    void testCalculateDiscountedPriceWithLastMinuteDiscount() {
        double basePrice = 100.0;
        OffsetDateTime departureDate = OffsetDateTime.now().plusDays(5); // 5 days in future

        List<DiscountData> discounts = List.of(new DiscountData(1, "Early Bird", 15.0, "early_bird", 1, 30), // Applies
                new DiscountData(2, "Super Early", 25.0, "early_bird", 1, 20), // Applies
                new DiscountData(3, "Last Minute", 35.0, "last_minute", 1, 7)  // Applies (5 <= 7)
        );

        when(discountRepository.getAll()).thenReturn(discounts);

        double result = discountManager.calculateDiscountedPrice(basePrice, departureDate);

        // Should apply the highest discount (35% last minute)
        double expectedPrice = 100.0 - (100.0 * 0.35);
        assertEquals(expectedPrice, result, 0.01);
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

    @Test
    void testDeleteDiscount() {
        Integer discountId = 1;

        discountManager.deleteDiscount(discountId);

        verify(discountRepository).delete(discountId);
    }

}
