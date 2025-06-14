package io.github.fontysvenlo.ais.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import io.github.fontysvenlo.ais.datarecords.DiscountData;
import io.github.fontysvenlo.ais.persistence.api.DiscountRepository;

public class DiscountRepositoryImpl implements DiscountRepository {
    private final DataSource dataSource;
    private String discountTableName = "public.discount"; // Default table name

    public DiscountRepositoryImpl(DBConfig config) {
        this.dataSource = DBProvider.getDataSource(config);        
    }

    private DiscountData mapResultSetToDiscount(ResultSet resultSet) throws SQLException {
        // Check if employeeid is NULL in the result set
        int employeeId = resultSet.getInt("employeeid");
        Integer employeeIdObj = resultSet.wasNull() ? null : employeeId;
        
        return new DiscountData(
            resultSet.getInt("id"),
            resultSet.getString("name"),
            resultSet.getDouble("amount"),
            resultSet.getString("type"),
            employeeIdObj,
            resultSet.getInt("days")
        );
    }

    @Override
    public List<DiscountData> getAll() {
        List<DiscountData> discounts = new ArrayList<>();
        
        String query = "SELECT * FROM " + discountTableName;
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet resultSet = stmt.executeQuery()) {
            
            int count = 0;
            while (resultSet.next()) {
                DiscountData discount = mapResultSetToDiscount(resultSet);
                discounts.add(discount);
                count++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error getting all discounts", e);
        }
        
        return discounts;
    }

    @Override
    public DiscountData add(DiscountData discount) {
        
        String query = "INSERT INTO " + discountTableName + 
            " (name, amount, type, employeeid, days) VALUES (?, ?, ?, ?, ?) RETURNING *";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Handle null employeeID
            if (discount.employeeID() == null) {
                throw new IllegalArgumentException("Employee ID cannot be null when adding a discount.");
            }
            
            stmt.setString(1, discount.name());
            stmt.setDouble(2, discount.amount());
            stmt.setString(3, discount.type());
            stmt.setInt(4, discount.employeeID());
            stmt.setInt(5, discount.days());
            
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    DiscountData added = mapResultSetToDiscount(resultSet);
                    return added;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error adding discount", e);
        }
        return null;
    }

    @Override
    public DiscountData delete(Integer id) {
        String query = "DELETE FROM " + discountTableName + " WHERE id = ? RETURNING *";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToDiscount(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting discount", e);
        }
        return null;
    }

    @Override
    public DiscountData getOne(Integer id) {
        String query = "SELECT * FROM " + discountTableName + " WHERE id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToDiscount(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting discount by id", e);
        }
        return null;
    }
} 