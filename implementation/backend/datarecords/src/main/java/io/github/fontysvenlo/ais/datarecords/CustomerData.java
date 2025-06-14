package io.github.fontysvenlo.ais.datarecords;

/**
 * Data record to represent a customer within a booking.
 * This is used to store customer information when creating bookings.
 */
public record CustomerData(
    Integer id,
    String firstName,
    String lastName,
    String email,
    String phone,
    String street,
    String houseNumber,
    String city,
    String country,
    boolean isInfant
) {
    /**
     * Constructor with default ID (null).
     */
    public CustomerData(
            String firstName,
            String lastName,
            String email,
            String phone,
            String street,
            String houseNumber,
            String city,
            String country,
            boolean isInfant) {
        this(null, firstName, lastName, email, phone, street, houseNumber, city, country, isInfant);
    }
}
