package io.github.fontysvenlo.ais.restapi;

import io.github.fontysvenlo.ais.businesslogic.api.FlightManager;
import io.github.fontysvenlo.ais.businesslogic.api.PriceManager;
import io.github.fontysvenlo.ais.datarecords.FlightData;
import io.github.fontysvenlo.ais.datarecords.PricePerKmData;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class PriceResource implements CrudHandler {

    private final PriceManager priceManager;

    public PriceResource(PriceManager priceManager, FlightManager flightManager) {
        this.priceManager = priceManager;
        initializePrice();
    }

    /**
     * Initializes the price from the manager or sets a default price if none exists
     */
    private void initializePrice() {
        try {
            int currentPrice = priceManager.getPrice();
        } catch (Exception e) {
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

            context.status(201).json(priceData);
        } catch (NumberFormatException e) {
            context.status(400).json(Map.of("error", "Invalid price format"));
        } catch (Exception e) {
            context.status(500).json(Map.of("error", "Could not set price"));
        }
    }

    @Override
    public void getAll(@NotNull Context context) {
        try {
            int currentPrice = priceManager.getPrice();
            context.status(200).json(Map.of("price", currentPrice));
        } catch (Exception e) {
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
