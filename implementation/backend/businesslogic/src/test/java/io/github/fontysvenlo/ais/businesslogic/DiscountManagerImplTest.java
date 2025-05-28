package io.github.fontysvenlo.ais.businesslogic;

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
                new DiscountData(2, "Discount 2", 20.0, "type2", 2, 10)
        );

        when(discountRepository.getAll()).thenReturn(discounts);

        List<DiscountData> result = discountManager.getAllDiscounts();

        assertEquals(2, result.size());
        assertEquals("Discount 1", result.get(0).name());
        assertEquals("Discount 2", result.get(1).name());
    }

}
