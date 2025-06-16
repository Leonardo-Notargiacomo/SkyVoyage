package io.github.fontysvenlo.ais.datarecords;

public record DiscountData(Integer id, String name, Double amount, String type, Integer employeeID, Integer days) {

    public DiscountData {
        if (name == null) {
            throw new NullPointerException("name cannot be null");
        }
        if (amount == null) {
            throw new NullPointerException("amount cannot be null");
        }
        if (type == null) {
            throw new NullPointerException("type cannot be null");
        }
        if (employeeID == null) {
            throw new NullPointerException("employeeID cannot be null");
        }
        if (days == null) {
            throw new NullPointerException("days cannot be null");
        }
    }
}