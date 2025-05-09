package io.github.fontysvenlo.ais.persistence;

import io.github.fontysvenlo.ais.datarecords.DiscountData;
import io.github.fontysvenlo.ais.persistence.api.DiscountRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DiscountRepositoryImpl implements DiscountRepository {
    private final DataSource dataSource;
    private static final Logger LOGGER = Logger.getLogger(DiscountRepositoryImpl.class.getName());
    private String discountTableName = "public.discount"; // Default table name

    public DiscountRepositoryImpl(DBConfig config) {
        this.dataSource = DBProvider.getDataSource(config);
        LOGGER.info("DiscountRepositoryImpl initialized with config: " + config);
        
        // Run a diagnostic check to see what tables exist
        try (Connection conn = dataSource.getConnection()) {
            LOGGER.info("Got database connection for diagnostics");
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                LOGGER.info("Checking all tables in database:");
                boolean discountTableFound = false;
                
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    String schema = rs.getString("TABLE_SCHEM");
                    LOGGER.info("Found table: " + schema + "." + tableName);
                    
                    // If this is the discount table, try a simple SELECT
                    if (tableName.equalsIgnoreCase("discount")) {
                        discountTableFound = true;
                        discountTableName = schema + "." + tableName;
                        LOGGER.info("Found discount table: " + discountTableName);
                        
                        try (ResultSet countRs = conn.createStatement().executeQuery(
                                "SELECT COUNT(*) FROM " + discountTableName)) {
                            if (countRs.next()) {
                                int count = countRs.getInt(1);
                                LOGGER.info("  - Discount table contains " + count + " rows");
                            }
                        } catch (SQLException e) {
                            LOGGER.warning("Could not count rows in " + discountTableName + ": " + e.getMessage());
                        }
                    }
                }
                
                if (!discountTableFound) {
                    LOGGER.warning("No discount table found in the database!");
                    // Let's check if there's a capitalized Discount table
                    try (ResultSet rs2 = metaData.getTables(null, null, "Discount", new String[]{"TABLE"})) {
                        if (rs2.next()) {
                            String schema = rs2.getString("TABLE_SCHEM");
                            discountTableName = schema + ".\"Discount\"";
                            LOGGER.info("Found capitalized Discount table: " + discountTableName);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.warning("Error during diagnostic check: " + e.getMessage());
        }
        
        LOGGER.info("Will use table name: " + discountTableName + " for all operations");
    }

    private DiscountData mapResultSetToDiscount(ResultSet rs) throws SQLException {
        // Check if employeeid is NULL in the result set
        int employeeId = rs.getInt("employeeid");
        Integer employeeIdObj = rs.wasNull() ? null : employeeId;
        
        return new DiscountData(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getDouble("amount"),
            rs.getString("type"),
            employeeIdObj,
            rs.getInt("days")
        );
    }

    @Override
    public List<DiscountData> getAll() {
        LOGGER.info("Fetching all discounts from " + discountTableName);
        List<DiscountData> discounts = new ArrayList<>();
        
        String query = "SELECT * FROM " + discountTableName;
        LOGGER.info("Executing query: " + query);
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            LOGGER.info("Query executed successfully");
            int count = 0;
            while (rs.next()) {
                DiscountData discount = mapResultSetToDiscount(rs);
                LOGGER.info("Mapped discount: " + discount);
                discounts.add(discount);
                count++;
            }
            LOGGER.info("Total discounts found: " + count);
        } catch (SQLException e) {
            LOGGER.severe("Error getting all discounts: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error getting all discounts", e);
        }
        
        return discounts;
    }

    @Override
    public DiscountData add(DiscountData discount) {
        LOGGER.info("Adding discount: " + discount);
        
        String query = "INSERT INTO " + discountTableName + 
            " (name, amount, type, employeeid, days) VALUES (?, ?, ?, ?, ?) RETURNING *";
        LOGGER.info("Executing query: " + query);
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, discount.name());
            stmt.setDouble(2, discount.amount());
            stmt.setString(3, discount.type());
            
            // Handle null employeeID
            if (discount.employeeID() == null) {
                stmt.setNull(4, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(4, discount.employeeID());
            }
            
            stmt.setInt(5, discount.days());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    DiscountData added = mapResultSetToDiscount(rs);
                    LOGGER.info("Added discount successfully: " + added);
                    return added;
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error adding discount: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error adding discount", e);
        }
        return null;
    }

    @Override
    public DiscountData update(DiscountData discount) {
        String query = "UPDATE " + discountTableName + 
            " SET name = ?, amount = ?, type = ?, employeeid = ?, days = ? WHERE id = ? RETURNING *";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, discount.name());
            stmt.setDouble(2, discount.amount());
            stmt.setString(3, discount.type());
            
            // Handle null employeeID
            if (discount.employeeID() == null) {
                stmt.setNull(4, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(4, discount.employeeID());
            }
            
            stmt.setInt(5, discount.days());
            stmt.setInt(6, discount.id());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDiscount(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error updating discount: " + e.getMessage());
            throw new RuntimeException("Error updating discount", e);
        }
        return null;
    }

    @Override
    public DiscountData delete(Integer id) {
        String query = "DELETE FROM " + discountTableName + " WHERE id = ? RETURNING *";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDiscount(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error deleting discount: " + e.getMessage());
            throw new RuntimeException("Error deleting discount", e);
        }
        return null;
    }

    @Override
    public List<DiscountData> getByType(String type) {
        List<DiscountData> discounts = new ArrayList<>();
        String query = "SELECT * FROM " + discountTableName + " WHERE type = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, type);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    discounts.add(mapResultSetToDiscount(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error getting discounts by type: " + e.getMessage());
            throw new RuntimeException("Error getting discounts by type", e);
        }
        return discounts;
    }

    @Override
    public DiscountData getOne(Integer id) {
        String query = "SELECT * FROM " + discountTableName + " WHERE id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDiscount(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error getting discount by id: " + e.getMessage());
            throw new RuntimeException("Error getting discount by id", e);
        }
        return null;
    }
} 