# Set Price Per Kilometer - Sequence Diagram

The following sequence diagram shows how the price per kilometer is set in the system.

```mermaid
sequenceDiagram
    actor Sales Manager
    participant PriceResource as PriceResource
    participant PriceManager as PriceManager
    participant PriceRepository as PriceRepository
    participant Database as Database
    participant DisplayFlightAPIClient as DisplayFlightAPIClient
    participant SearchFlightAPIClient as SearchFlightAPIClient

    Sales Manager ->> PriceResource: POST /api/v1/flights/price/create?price=15
    activate PriceResource

    PriceResource ->> PriceResource: Parse price parameter
    PriceResource ->> PriceResource: Validate price (non-negative)

    alt Invalid price
        PriceResource -->> Sales Manager: Return 400 Bad Request with error
    else Valid price
        PriceResource ->> PriceManager: setPrice(PricePerKmData(15))
        activate PriceManager

        PriceManager ->> PriceRepository: change(PricePerKmData(15))
        activate PriceRepository
        PriceRepository ->> Database: INSERT INTO priceperkm (price) VALUES (15)
        Database -->> PriceRepository: Confirm insert
        PriceRepository -->> PriceManager: Return updated price data
        deactivate PriceRepository

        PriceManager -->> PriceResource: Return updated price
        deactivate PriceManager

        PriceResource -->> Sales Manager: Return 201 Created with priceData
    end
    deactivate PriceResource

    Note over DisplayFlightAPIClient, SearchFlightAPIClient: Later, when calculating flight prices...

    par DisplayFlightAPIClient calculating prices
        DisplayFlightAPIClient ->> PriceManager: getPrice()
        activate PriceManager
        PriceManager ->> PriceRepository: get()
        activate PriceRepository
        PriceRepository ->> Database: SELECT price FROM priceperkm ORDER BY id DESC LIMIT 1
        Database -->> PriceRepository: Return current price (15)
        PriceRepository -->> PriceManager: Return price (15)
        deactivate PriceRepository
        PriceManager -->> DisplayFlightAPIClient: Return price (15)
        deactivate PriceManager
        DisplayFlightAPIClient ->> DisplayFlightAPIClient: Calculate flight price using<br>distance * price per km
    and SearchFlightAPIClient calculating prices
        SearchFlightAPIClient ->> PriceManager: getPrice()
        activate PriceManager
        PriceManager ->> PriceRepository: get()
        activate PriceRepository
        PriceRepository ->> Database: SELECT price FROM priceperkm ORDER BY id DESC LIMIT 1
        Database -->> PriceRepository: Return current price (15)
        PriceRepository -->> PriceManager: Return price (15)
        deactivate PriceRepository
        PriceManager -->> SearchFlightAPIClient: Return price (15)
        deactivate PriceManager
        SearchFlightAPIClient ->> SearchFlightAPIClient: Calculate flight price using<br>distance * price per km
    end
```

This diagram shows the flow for setting and using the price per km:

1. A Sales Manager sends a POST request to `/prices` with a price parameter
2. The PriceResource handles the request, parses and validates the price
3. The PriceResource calls setPrice() on the PriceManager with a new PricePerKmData
4. The PriceManagerImpl updates the price in the PriceRepository
5. The PriceRepositoryImpl stores the new price value in the database
6. A success response is returned to the Sales Manager

Later, when calculating flight prices:
1. The SearchFlightAPIClient calls getPrice() on the PriceManager
2. The PriceManagerImpl retrieves the current price from the PriceRepository
3. The PriceRepositoryImpl gets the most recent price from the database
4. The SearchFlightAPIClient uses the price to calculate the flight cost using the formula: duration * 15 * price / 100 
