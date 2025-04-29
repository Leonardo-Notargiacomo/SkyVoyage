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
        discountRepository = mock(DiscountRepository.class);
    }

    @Test
    void testConfigureEarlyBirdDiscount() {
        DiscountData earlyBird = new DiscountData(1, "Early Bird Discount", 10.0, "early_bird", 1);
        when(discountRepository.add(any(DiscountData.class))).thenReturn(earlyBird);
        when(discountRepository.getOne(1)).thenReturn(earlyBird);

        DiscountData created = discountManager.createDiscount(earlyBird);
        assertNotNull(created);
        assertEquals("Early Bird Discount", created.name());
        assertEquals(10, created.amount());
        assertEquals("early_bird", created.type());

        DiscountData found = discountManager.getDiscountById(1).orElse(null);
        assertNotNull(found);
        assertEquals(10, found.amount());
    }

    @Test
    void testConfigureLastMinuteDiscount() {
        DiscountData lastMinute = new DiscountData(2, "Last-Minute Discount", 20.0, "last_minute", 2);
        when(discountRepository.add(any(DiscountData.class))).thenReturn(lastMinute);
        when(discountRepository.getOne(2)).thenReturn(lastMinute);

        DiscountData created = discountManager.createDiscount(lastMinute);
        assertNotNull(created);
        assertEquals("Last-Minute Discount", created.name());
        assertEquals(20, created.amount());
        assertEquals("last_minute", created.type());

        DiscountData found = discountManager.getDiscountById(2).orElse(null);
        assertNotNull(found);
        assertEquals(20, found.amount());
    }

}
