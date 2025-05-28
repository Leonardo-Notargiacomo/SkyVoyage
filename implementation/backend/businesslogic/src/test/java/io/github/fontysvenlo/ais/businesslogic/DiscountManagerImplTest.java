package io.github.fontysvenlo.ais.businesslogic;

import io.github.fontysvenlo.ais.businesslogic.api.DiscountManager;
import io.github.fontysvenlo.ais.datarecords.DiscountData;
import io.github.fontysvenlo.ais.persistence.api.DiscountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DiscountManagerImplTest {

    private DiscountRepository discountRepository;
    private DiscountManager discountManager;

    @BeforeEach
    void setUp() {
        this.discountRepository = mock(DiscountRepository.class);
        this.discountManager = DiscountManagerImpl.getInstance(discountRepository);
    }


    @Test
    void testValidateDiscountWithInvalidPercentage() {
        DiscountData negativeDiscount = new DiscountData(2, "Negative", -10.0, "regular", 2, 10);
        DiscountData tooHighDiscount = new DiscountData(3, "Too High", 110.0, "regular", 3, 10);

        assertFalse(discountManager.validateDiscount(negativeDiscount));
        assertFalse(discountManager.validateDiscount(tooHighDiscount));
    }

    @Test
    void testValidateDiscountWithValidInput() {
        DiscountData validDiscount = new DiscountData(1, "Valid Discount", 50.0, "regular", 1, 10);
        assertTrue(discountManager.validateDiscount(validDiscount));
    }

    @Test
    void testValidateDiscountWithInvalidDays() {
        DiscountData zeroDaysDiscount = new DiscountData(4, "Zero Days", 20.0, "regular", 4, 0);
        DiscountData negativeDaysDiscount = new DiscountData(5, "Negative Days", 20.0, "regular", 5, -5);

        assertFalse(discountManager.validateDiscount(zeroDaysDiscount));
        assertFalse(discountManager.validateDiscount(negativeDaysDiscount));
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
                new DiscountData(2, "Discount 2", 20.0, "type2", 2, 10)
        );

        when(discountRepository.getAll()).thenReturn(discounts);

        List<DiscountData> result = discountManager.getAllDiscounts();

        assertEquals(2, result.size());
        assertEquals("Discount 1", result.get(0).name());
        assertEquals("Discount 2", result.get(1).name());
    }

}
