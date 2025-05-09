**# Discount System - Sequence Diagram**

The following sequence diagram illustrates how discounts are applied to flight prices in the system.

```mermaid
sequenceDiagram
    actor Admin
    participant PriceResource as PriceResource
    participant PriceManagerImpl as PriceManagerImpl
    participant PriceRepositoryImpl as PriceRepositoryImpl
    participant AmadeusClient as AmadeusClient
    
    Admin ->> PriceResource: POST /prices?price=15
    activate PriceResource
    
    PriceResource ->> PriceResource: Parse price parameter
    PriceResource ->> PriceResource: Validate price (non-negative)
    
    PriceResource ->> PriceManagerImpl: setPrice(PricePerKmData(15))
    activate PriceManagerImpl
    
    PriceManagerImpl ->> PriceRepositoryImpl: change(PricePerKmData(15))
    activate PriceRepositoryImpl
    
    PriceRepositoryImpl ->> PriceRepositoryImpl: Update pricePerKmData
    PriceRepositoryImpl -->> PriceManagerImpl: 
    deactivate PriceRepositoryImpl

    PriceManagerImpl ->> PriceManagerImpl: Increment priceVersion
    PriceManagerImpl -->> PriceResource: 
    deactivate PriceManagerImpl

    PriceResource ->> PriceResource: Log price update
    PriceResource -->> Admin: Return 201 Created with priceData
    deactivate PriceResource

    Note over AmadeusClient, PriceManagerImpl: Later, when calculating flight prices...

    AmadeusClient ->> PriceManagerImpl: getPrice()
    activate PriceManagerImpl

    PriceManagerImpl ->> PriceRepositoryImpl: get()
    activate PriceRepositoryImpl

    PriceRepositoryImpl -->> PriceManagerImpl: Return price (15)
    deactivate PriceRepositoryImpl

    PriceManagerImpl -->> AmadeusClient: Return price (15)
    deactivate PriceManagerImpl

    AmadeusClient ->> AmadeusClient: Calculate flight price using<br>duration * 15 * price / 100
```

This diagram shows the flow for setting and using the price per km:

1. An Admin sends a POST request to `/prices` with a price parameter
2. The PriceResource handles the request, parses and validates the price
3. The PriceResource calls setPrice() on the PriceManager with a new PricePerKmData
4. The PriceManagerImpl updates the price in the PriceRepository and increments its version
5. The PriceRepositoryImpl stores the new price value
6. A success response is returned to the Admin

Later, when calculating flight prices:
1. The AmadeusClient calls getPrice() on the PriceManager
2. The PriceManagerImpl retrieves the current price from the PriceRepository
3. The AmadeusClient uses the price to calculate the flight cost using the formula: duration * 15 * price / 100 
