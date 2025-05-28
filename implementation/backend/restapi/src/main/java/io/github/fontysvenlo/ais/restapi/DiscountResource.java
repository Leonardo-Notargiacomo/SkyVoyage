package io.github.fontysvenlo.ais.restapi;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.fontysvenlo.ais.businesslogic.api.DiscountManager;
import io.github.fontysvenlo.ais.datarecords.DiscountData;
import io.javalin.http.Context;

/**
 * This class is responsible for handling the requests for the discount resource.
 */
public class DiscountResource {

    private static final Logger logger = LoggerFactory.getLogger(DiscountResource.class);
    private final DiscountManager discountManager;

    /**
     * Initializes the controller with the business logic.
     */
    public DiscountResource(DiscountManager discountManager) {
        this.discountManager = discountManager;
        logger.info("DiscountResource initialized");
    }

    /**
     * Adds a discount to the storage.
     * If the discount data is null, the status is set to 400 (Bad Request).
     * Otherwise, the status is set to 201 (Created).
     */
    public void create(Context context) {
        logger.info("Creating new discount");
        try {
            DiscountData discountData = context.bodyAsClass(DiscountData.class);
            if (discountData == null) {
                logger.warn("Discount data is null");
                context.status(400);
                return;
            }
            
            discountManager.addDiscount(discountData);
            context.status(201);
            context.json(discountData);
            logger.info("Discount created successfully: {}", discountData);
        } catch (Exception e) {
            logger.error("Unexpected error creating discount", e);
            context.status(500).json(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * Retrieves all discounts from the storage.
     * The status is set to 200 (OK) and the list of discounts is returned as JSON.
     */
    public void getAll(Context context) {
        logger.info("Getting all discounts");
        try {
            List<DiscountData> discounts = discountManager.getAllDiscounts();
            context.status(200);
            context.json(discounts);
            logger.info("Retrieved {} discounts", discounts.size());
        } catch (Exception e) {
            logger.error("Error getting all discounts", e);
            context.status(500).json(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * Deletes a discount with the given ID from storage.
     */
    public void delete(Context context, String discountId) {
        logger.info("Deleting discount with ID: {}", discountId);
        try {
            Integer id = Integer.parseInt(discountId);
            discountManager.deleteDiscount(id);
            context.status(204);
            logger.info("Discount deleted successfully: {}", discountId);
        } catch (NumberFormatException e) {
            logger.error("Invalid discount ID format: {}", discountId);
            context.status(400).json(Map.of("error", "Invalid discount ID format"));
        } catch (Exception e) {
            logger.error("Error deleting discount", e);
            context.status(500).json(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * Calculates the applicable discount for a specific number of days before departure.
     */
    public void calculateApplicableDiscount(Context context) {
        String daysStr = context.queryParam("days");
        logger.info("Calculating applicable discount for days: {}", daysStr);
        
        if (daysStr == null || daysStr.isEmpty()) {
            logger.warn("Missing days parameter");
            context.status(400).json(Map.of("error", "Days parameter is required"));
            return;
        }
        
        try {
            int daysUntilDeparture = Integer.parseInt(daysStr);
            List<DiscountData> allDiscounts = discountManager.getAllDiscounts();
            
            // Find the best applicable discount
            double bestDiscountPercentage = 0.0;
            DiscountData bestDiscount = null;
            
            for (DiscountData discount : allDiscounts) {
                if (daysUntilDeparture <= discount.days() && discount.amount() > bestDiscountPercentage) {
                    bestDiscountPercentage = discount.amount();
                    bestDiscount = discount;
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            if (bestDiscount != null) {
                response.put("discount", bestDiscount);
                response.put("percentage", bestDiscountPercentage);
                response.put("found", true);
                logger.info("Found applicable discount: {}", bestDiscount);
            } else {
                response.put("found", false);
                response.put("percentage", 0.0);
                logger.info("No applicable discount found");
            }
            
            context.status(200);
            context.json(response);
        } catch (NumberFormatException e) {
            logger.error("Invalid days format: {}", daysStr);
            context.status(400).json(Map.of("error", "Invalid days format"));
        } catch (Exception e) {
            logger.error("Error calculating applicable discount", e);
            context.status(500).json(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * Applies a discount to a price.
     */
    public void applyDiscount(Context context) {
        String priceStr = context.queryParam("price");
        String departureDateStr = context.queryParam("departureDate");
        
        logger.info("Applying discount to price: {} with departure date: {}", priceStr, departureDateStr);
        
        if (priceStr == null || priceStr.isEmpty()) {
            logger.warn("Missing price parameter");
            context.status(400).json(Map.of("error", "Price parameter is required"));
            return;
        }
        
        if (departureDateStr == null || departureDateStr.isEmpty()) {
            logger.warn("Missing departureDate parameter");
            context.status(400).json(Map.of("error", "DepartureDate parameter is required"));
            return;
        }
        
        try {
            double price = Double.parseDouble(priceStr);
            LocalDateTime departureDate = LocalDateTime.parse(departureDateStr);
            LocalDateTime now = LocalDateTime.now();
            long daysUntilDeparture = ChronoUnit.DAYS.between(now, departureDate);
            
            List<DiscountData> allDiscounts = discountManager.getAllDiscounts();
            double bestDiscountPercentage = 0.0;
            DiscountData bestDiscount = null;
            
            for (DiscountData discount : allDiscounts) {
                if (daysUntilDeparture <= discount.days() && discount.amount() > bestDiscountPercentage) {
                    bestDiscountPercentage = discount.amount();
                    bestDiscount = discount;
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            double finalPrice = price;
            
            if (bestDiscount != null) {
                double discountAmount = price * (bestDiscountPercentage / 100.0);
                finalPrice = price - discountAmount;
                
                response.put("originalPrice", price);
                response.put("discount", bestDiscount);
                response.put("discountAmount", discountAmount);
                response.put("finalPrice", finalPrice);
                
                logger.info("Applied discount: {}%, original price: {}, final price: {}", 
                    bestDiscountPercentage, price, finalPrice);
            } else {
                response.put("originalPrice", price);
                response.put("finalPrice", price);
                response.put("message", "No applicable discount found");
                
                logger.info("No applicable discount found for price: {}", price);
            }
            
            context.status(200);
            context.json(response);
        } catch (NumberFormatException e) {
            logger.error("Invalid price format: {}", priceStr);
            context.status(400).json(Map.of("error", "Invalid price format"));
        } catch (Exception e) {
            logger.error("Error applying discount", e);
            context.status(500).json(Map.of("error", "An unexpected error occurred"));
        }
    }
} 