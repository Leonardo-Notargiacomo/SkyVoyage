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
        // Given the repository returns 0 (no price set)
        when(priceRepository.get()).thenReturn(0);

        // When getting the price
        int price = priceManager.getPrice();

        // Then we should get the default price (11)
        assertThat(price).isEqualTo(11);
    }

    @Test
    public void testGetStoredPrice() {
        // Given the repository returns a specific price
        int expectedPrice = 15;
        when(priceRepository.get()).thenReturn(expectedPrice);

        // When getting the price
        int actualPrice = priceManager.getPrice();

        // Then we should get the stored price
        assertThat(actualPrice).isEqualTo(expectedPrice);
    }

    @Test
    public void testSetPrice() {
        // Given a new price to set
        PricePerKmData newPrice = new PricePerKmData(20);

        // When setting the price
        priceManager.setPrice(newPrice);

        // Then the repository should be updated
        verify(priceRepository).change(newPrice);

        // And the price version should be incremented
        assertThat(priceManager.getPriceVersion()).isGreaterThan(0);
    }

    @Test
    public void testHandleRepositoryException() {
        // Given the repository throws an exception
        when(priceRepository.get()).thenThrow(new RuntimeException("Database error"));

        // When getting the price
        int price = priceManager.getPrice();

        // Then we should get the default price (11)
        assertThat(price).isEqualTo(11);
    }

    @Test
    public void testPriceVersionIncrements() {
        // Given the initial price version
        long initialVersion = priceManager.getPriceVersion();

        // When setting the price multiple times
        priceManager.setPrice(new PricePerKmData(20));
        priceManager.setPrice(new PricePerKmData(25));
        priceManager.setPrice(new PricePerKmData(30));

        // Then the price version should be incremented each time
        assertThat(priceManager.getPriceVersion()).isEqualTo(initialVersion + 3);
    }

    @Test
    public void testExtremelyHighPrice() {
        // Given a very high price
        PricePerKmData highPrice = new PricePerKmData(Integer.MAX_VALUE);
        
        // When setting the extreme price
        priceManager.setPrice(highPrice);
        
        // Then the repository should be updated with the extreme price
        verify(priceRepository).change(highPrice);
    }

    @Test
    public void testZeroPrice() {
        // Given a zero price
        PricePerKmData zeroPrice = new PricePerKmData(0);
        
        // When setting a zero price
        priceManager.setPrice(zeroPrice);
        
        // Then the repository should be updated with the zero price
        verify(priceRepository).change(zeroPrice);
    }
    
    @Test
    public void testPricePersistsAcrossInstances() {
        // Given a price is set in the repository
        int expectedPrice = 25;
        when(priceRepository.get()).thenReturn(expectedPrice);
        
        // When getting a new instance of the manager with the same repository
        PriceManager anotherInstance = PriceManagerImpl.getInstance(priceRepository);
        
        // Then the price should be the same
        assertThat(anotherInstance.getPrice()).isEqualTo(expectedPrice);
        
        // And they should be the same instance
        assertThat(anotherInstance).isSameAs(priceManager);
    }
} 