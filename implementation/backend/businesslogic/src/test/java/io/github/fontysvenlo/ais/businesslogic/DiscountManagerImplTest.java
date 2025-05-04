package io.github.fontysvenlo.ais.businesslogic;

import io.github.fontysvenlo.ais.businesslogic.api.DiscountManager;
import io.github.fontysvenlo.ais.datarecords.DiscountData;
import io.github.fontysvenlo.ais.persistence.api.DiscountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

}
