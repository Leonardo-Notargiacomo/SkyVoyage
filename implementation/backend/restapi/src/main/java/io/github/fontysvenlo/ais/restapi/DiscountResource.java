package io.github.fontysvenlo.ais.restapi;

import io.github.fontysvenlo.ais.businesslogic.api.DiscountManager;
import io.github.fontysvenlo.ais.datarecords.DiscountData;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for handling the requests for the discount resource.
 */
public class DiscountResource implements CrudHandler {

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
    @Override
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
        } catch (IllegalArgumentException e) {
            logger.error("Error creating discount: {}", e.getMessage());
            context.status(422).json(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error creating discount", e);
            context.status(500).json(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * Retrieves all discounts from the storage.
     * The status is set to 200 (OK) and the list of discounts is returned as JSON.
     */
    @Override
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
    @Override
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
     * Retrieves a single discount by ID.
     */
    @Override
    public void getOne(Context context, String discountId) {
        logger.info("Getting discount with ID: {}", discountId);
        try {
            Integer id = Integer.parseInt(discountId);
            var discount = discountManager.getDiscountById(id);
            
            if (discount.isPresent()) {
                context.status(200);
                context.json(discount.get());
                logger.info("Retrieved discount: {}", discount.get());
            } else {
                logger.warn("Discount not found: {}", discountId);
                context.status(404).json(Map.of("error", "Discount not found"));
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid discount ID format: {}", discountId);
            context.status(400).json(Map.of("error", "Invalid discount ID format"));
        } catch (Exception e) {
            logger.error("Error getting discount", e);
            context.status(500).json(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * Updates a discount with the given ID.
     */
    @Override
    public void update(Context context, String discountId) {
        logger.info("Updating discount with ID: {}", discountId);
        try {
            Integer id = Integer.parseInt(discountId);
            DiscountData updatedDiscount = context.bodyAsClass(DiscountData.class);
            
            if (updatedDiscount == null) {
                logger.warn("Update data is null");
                context.status(400);
                return;
            }
            
            // Ensure the ID in the path matches the discount
            if (!id.equals(updatedDiscount.id())) {
                logger.warn("ID mismatch: path={}, body={}", id, updatedDiscount.id());
                context.status(400).json(Map.of("error", "ID in path doesn't match discount ID"));
                return;
            }
            
            discountManager.updateDiscount(updatedDiscount);
            context.status(200);
            context.json(updatedDiscount);
            logger.info("Discount updated successfully: {}", updatedDiscount);
        } catch (NumberFormatException e) {
            logger.error("Invalid discount ID format: {}", discountId);
            context.status(400).json(Map.of("error", "Invalid discount ID format"));
        } catch (IllegalArgumentException e) {
            logger.error("Error updating discount: {}", e.getMessage());
            context.status(422).json(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating discount", e);
            context.status(500).json(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * Gets discounts by type.
     */
    public void getByType(Context context) {
        String type = context.queryParam("type");
        logger.info("Getting discounts by type: {}", type);
        
        if (type == null || type.isEmpty()) {
            logger.warn("Missing type parameter");
            context.status(400).json(Map.of("error", "Type parameter is required"));
            return;
        }
        
        try {
            List<DiscountData> discounts = discountManager.getDiscountsByType(type);
            context.status(200);
            context.json(discounts);
            logger.info("Retrieved {} discounts of type {}", discounts.size(), type);
        } catch (Exception e) {
            logger.error("Error getting discounts by type", e);
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