package io.github.fontysvenlo.ais.datarecords;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for DiscountData record to verify null validation.
 */
public class DiscountDataTest {

    @Test
    void testConstructorWithNullName() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            new DiscountData(1, null, 10.0, "regular", 5, 30);
        });
        assertEquals("name cannot be null", exception.getMessage());
    }

    @Test
    void testConstructorWithNullAmount() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            new DiscountData(1, "Test Discount", null, "regular", 5, 30);
        });
        assertEquals("amount cannot be null", exception.getMessage());
    }

    @Test
    void testConstructorWithNullType() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            new DiscountData(1, "Test Discount", 10.0, null, 5, 30);
        });
        assertEquals("type cannot be null", exception.getMessage());
    }

    @Test
    void testConstructorWithNullEmployeeID() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            new DiscountData(1, "Test Discount", 10.0, "regular", null, 30);
        });
        assertEquals("employeeID cannot be null", exception.getMessage());
    }

    @Test
    void testConstructorWithNullDays() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            new DiscountData(1, "Test Discount", 10.0, "regular", 5, null);
        });
        assertEquals("days cannot be null", exception.getMessage());
    }
}