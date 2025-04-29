package io.github.fontysvenlo.ais.businesslogic;

import io.github.fontysvenlo.ais.businesslogic.api.PriceManager;
import io.github.fontysvenlo.ais.datarecords.PricePerKmData;
import io.github.fontysvenlo.ais.persistence.api.PriceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PriceManagerImplTest {

    private PriceRepository priceRepository;
    private PriceManager priceManager;

    @BeforeEach
    public void setup() {

        priceRepository = mock(PriceRepository.class);

        priceManager = PriceManagerImpl.getInstance(priceRepository);
    }

    @Test
    public void testGetDefaultPrice() {
        when(priceRepository.get()).thenReturn(0);

        int price = priceManager.getPrice();

        assertThat(price).isEqualTo(11);
    }

    @Test
    public void testHandleRepositoryException() {
        when(priceRepository.get()).thenThrow(new RuntimeException("Database error"));

        int price = priceManager.getPrice();

        assertThat(price).isEqualTo(11);
    }

    @Test
    public void testPriceVersionIncrements() {
        long initialVersion = priceManager.getPriceVersion();

        priceManager.setPrice(new PricePerKmData(20));
        priceManager.setPrice(new PricePerKmData(25));
        priceManager.setPrice(new PricePerKmData(30));

        assertThat(priceManager.getPriceVersion()).isEqualTo(initialVersion + 3);
    }

    @Test
    public void testGetActualPrice() {
        when(priceRepository.get()).thenReturn(50);

        int price = priceManager.getPrice();

        assertThat(price).isEqualTo(50);
    }

    @Test
    public void testSetPriceCallsRepositoryChange() {

        PricePerKmData newPrice = new PricePerKmData(15);

        priceManager.setPrice(newPrice);

        verify(priceRepository, times(1)).change(newPrice);
    }

    @Test
    public void testInitialPriceVersion() {

        assertThat(priceManager.getPriceVersion()).isEqualTo(0);

    }
} 