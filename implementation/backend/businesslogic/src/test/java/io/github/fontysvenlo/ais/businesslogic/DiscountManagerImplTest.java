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
    void testConfigureEarlyBirdDiscount() {
        DiscountData earlyBird = new DiscountData(1, "Early Bird Discount", 10.0, "early_bird", 1, 30);
        when(discountRepository.add(any(DiscountData.class))).thenReturn(earlyBird);
        when(discountRepository.getOne(1)).thenReturn(earlyBird);

        discountManager.addDiscount(earlyBird);
        assertNotNull(discountManager.getDiscountById(1));
        assertEquals("Early Bird Discount", discountManager.getDiscountById(1).orElse(null).name());
        assertEquals(10, discountManager.getDiscountById(1).orElse(null).amount());
        assertEquals("early_bird", discountManager.getDiscountById(1).orElse(null).type());
        assertEquals(30, discountManager.getDiscountById(1).orElse(null).days());

        DiscountData found = discountManager.getDiscountById(1).orElse(null);
        assertNotNull(found);
        assertEquals(10, found.amount());
    }

    @Test
    void testConfigureLastMinuteDiscount() {
        DiscountData lastMinute = new DiscountData(2, "Last-Minute Discount", 20.0, "last_minute", 2, 3);
        when(discountRepository.add(any(DiscountData.class))).thenReturn(lastMinute);
        when(discountRepository.getOne(2)).thenReturn(lastMinute);

        discountManager.addDiscount(lastMinute);

        assertNotNull(discountManager.getDiscountById(1));
        assertEquals("Last-Minute Discount", discountManager.getDiscountById(2).orElse(null).name());
        assertEquals(20, discountManager.getDiscountById(2).orElse(null).amount());
        assertEquals("last_minute", discountManager.getDiscountById(2).orElse(null).type());
        assertEquals(3, discountManager.getDiscountById(2).orElse(null).days());

        DiscountData found = discountManager.getDiscountById(2).orElse(null);
        assertNotNull(found);
        assertEquals(20, found.amount());
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

    @Test
    void testGetDiscountsByType() {
        List<DiscountData> earlyBirdDiscounts = List.of(
                new DiscountData(1, "Early Bird 1", 15.0, "early_bird", 1, 30),
                new DiscountData(3, "Early Bird 2", 25.0, "early_bird", 3, 20)
        );

        when(discountRepository.getByType("early_bird")).thenReturn(earlyBirdDiscounts);

        List<DiscountData> result = discountManager.getDiscountsByType("early_bird");

        assertEquals(2, result.size());
        assertEquals("Early Bird 1", result.get(0).name());
        assertEquals("Early Bird 2", result.get(1).name());
    }

    @Test
    void testUpdateDiscount() {
        DiscountData discount = new DiscountData(1, "Original Discount", 10.0, "type1", 1, 5);
        DiscountData updatedDiscount = new DiscountData(1, "Updated Discount", 15.0, "type1", 1, 7);

        when(discountRepository.update(any(DiscountData.class))).thenReturn(updatedDiscount);
        when(discountRepository.getOne(1)).thenReturn(updatedDiscount);

        discountManager.updateDiscount(updatedDiscount);

        verify(discountRepository).update(updatedDiscount);

        Optional<DiscountData> result = discountManager.getDiscountById(1);
        assertTrue(result.isPresent());
        assertEquals("Updated Discount", result.get().name());
        assertEquals(15.0, result.get().amount());
        assertEquals(7, result.get().days());
    }

}
