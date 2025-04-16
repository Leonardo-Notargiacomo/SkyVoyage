package io.github.fontysvenlo.ais.restapi;

import io.github.fontysvenlo.ais.businesslogic.api.PriceManager;
import io.github.fontysvenlo.ais.datarecords.PricePerKmData;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class PriceResource implements CrudHandler {

    private static final Logger logger = LoggerFactory.getLogger(PriceResource.class);
    private final PriceManager priceManager;
    private final AviationStackClient aviationStackClient;
    private final AmadeusClient amadeusClient;

    public PriceResource(PriceManager priceManager, AviationStackClient aviationStackClient, AmadeusClient amadeusClient) {
        this.priceManager = priceManager;
        this.aviationStackClient = aviationStackClient;
        this.amadeusClient = amadeusClient;
        
        // Initialize price from manager if available
        initializePrice();
    }

    /**
     * Initializes the price from the manager or sets a default price if none exists
     */
    private void initializePrice() {
        try {
            PricePerKmData currentPrice = priceManager.getPrice();
            if (currentPrice == null) {
                // Set default price
                PricePerKmData defaultPrice = new PricePerKmData(15);
                priceManager.setPrice(defaultPrice);
                logger.info("Initialized default price: {}", defaultPrice.price());
                
                // Update both clients
                aviationStackClient.updatePrice(defaultPrice.price());
                amadeusClient.updatePrice(defaultPrice.price());
            } else {
                // Update both clients with existing price
                aviationStackClient.updatePrice(currentPrice.price());
                amadeusClient.updatePrice(currentPrice.price());
                logger.info("Using existing price: {}", currentPrice.price());
            }
        } catch (Exception e) {
            logger.error("Error initializing price: {}", e.getMessage());
        }
    }

    @Override
    public void create(@NotNull Context context) {
        try {
            String priceStr = context.queryParam("price");
            if (priceStr == null) {
                context.status(400).json(Map.of("error", "Price parameter is required"));
                return;
            }

            int price = Integer.parseInt(priceStr);
            if (price < 0) {
                context.status(400).json(Map.of("error", "Price cannot be negative"));
                return;
            }

            PricePerKmData priceData = new PricePerKmData(price);
            priceManager.setPrice(priceData);
            
            // Update both clients with the new price
            aviationStackClient.updatePrice(price);
            amadeusClient.updatePrice(price);
            logger.info("Price updated to: {}", price);
            
            context.status(201).json(priceData);
        } catch (NumberFormatException e) {
            context.status(400).json(Map.of("error", "Invalid price format"));
        } catch (Exception e) {
            logger.error("Error setting price: {}", e.getMessage());
            context.status(500).json(Map.of("error", "Could not set price"));
        }
    }

    @Override
    public void getAll(@NotNull Context context) {
        try {
            PricePerKmData currentPrice = priceManager.getPrice();
            context.status(200).json(currentPrice);
        } catch (Exception e) {
            logger.error("Error getting price: {}", e.getMessage());
            context.status(500).json(Map.of("error", "Could not retrieve price"));
        }
    }

    @Override
    public void getOne(@NotNull Context context, @NotNull String s) {
        context.status(405).json(Map.of("error", "Method not allowed"));
    }

    @Override
    public void update(@NotNull Context context, @NotNull String id) {
        context.status(405).json(Map.of("error", "Method not allowed"));
    }

    @Override
    public void delete(@NotNull Context context, @NotNull String id) {
        context.status(405).json(Map.of("error", "Method not allowed"));
    }
}
