package io.github.fontysvenlo.ais.businesslogic.api;

import io.github.fontysvenlo.ais.datarecords.DiscountData;

/**
 * Interface for validating input data in the business logic layer. Provides
 * methods to check if data fields meet the required format and constraints.
 */
public interface ValidatorInterface {

    /**
     * Validates the given name.
     *
     * @param name the name to validate
     *
     * A valid name must: - not be null - have a length of at least 2 - contain
     * only letters, spaces, and the following characters: ',. -
     *
     * @return true if the name is valid, false otherwise
     */
    public boolean isValidName(String name);

    /**
     * Validates the given email.
     *
     * @param email the email to validate
     *
     * A valid email must: - not be null - contain at least one character before
     * the @ - contain at least one character before and after the dot - not
     * contain any spaces
     *
     * @return true if the email is valid, false otherwise
     */
    public boolean isValidEmail(String email);

    /**
     * Validates the given phone number.
     *
     * @param phoneNumber the phone number to validate
     *
     * A valid phone number must: - not be null - start with an optional +
     * followed by 1 to 3 digits - contain an optional opening parenthesis -
     * contain 1 to 3 digits - contain an optional closing parenthesis - contain
     * 1 to 4 digits - contain an optional separator (., -, or space) - contain
     * 1 to 4 digits
     *
     * @return true if the phone number is valid, false otherwise
     */
    public boolean isValidPhoneNumber(String phoneNumber);

    /**
     * Validates if a password meets the required criteria: - minimum length of
     * 8 characters - contain at least one digit - contain at least one
     * uppercase letter - contain at least one lowercase letter - contain at
     * least one special character: @#$%^&amp;+=!
     *
     * @param password the password to validate
     * @return true if password is valid, false otherwise
     */
    public boolean isValidPassword(String password);

    /**
     * Validates the given type.
     *
     * @param type the type to validate
     *
     * A valid type must: - not be null - be either "SalesManager",
     * "SalesEmployee", or "AccountManager"
     *
     * @return true if the type is valid, false otherwise
     */
    public boolean isValidType(String type);

    /**
     * Validates the given discount.
     *
     * @param discount the discount to validate
     *
     * A valid discount must: be larger than 0, and less than or equal to 100. And the days must be larger than 0.
     *
     * @return true if the type is valid, false otherwise
     */
    public boolean isValidDiscount(DiscountData discount);
}
