package io.github.fontysvenlo.ais.businesslogic;

import java.util.logging.Logger;
import java.util.regex.Pattern;

import io.github.fontysvenlo.ais.businesslogic.api.ValidatorInterface;
import io.github.fontysvenlo.ais.datarecords.DiscountData;

public class Validator implements ValidatorInterface {

    private static final Logger logger = Logger.getLogger(Validator.class.getName());

    private static final Pattern EMAIL_PATTERN
            = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+\\..+)$");

    private static final Pattern PASSWORD_PATTERN
            = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$");

    @Override
    public boolean isValidName(String name) {
        if (name == null || name.trim().length() < 2) {
            return false;
        }
        return name.matches("^[a-zA-Z]+(([',. -][a-zA-Z ])?[a-zA-Z]*)*$");
    }

    @Override
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    @Override
    public boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return false;
        }
        return phoneNumber.matches("^\\+?[0-9]{1,3}[-.\\s]?\\(?[0-9]{1,3}\\)?[-.\\s]?[0-9]{1,4}[-.\\s]?[0-9]{1,4}$");
    }

    @Override
    public boolean isValidPassword(String password) {
        logger.info("Validating password: " + (password != null ? "Length: " + password.length() : "null"));

        if (password == null || password.length() < 8) {
            logger.warning("Password failed: too short or null");
            return false;
        }

        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasSpecial = password.matches(".*[@#$%^&+=!].*");

        logger.info("Password validation - hasDigit: " + hasDigit
                + ", hasLower: " + hasLower
                + ", hasUpper: " + hasUpper
                + ", hasSpecial: " + hasSpecial);

        return hasDigit && hasLower && hasUpper && hasSpecial;
    }

    public boolean isValidType(String type) {
        return type != null && !type.trim().isEmpty()
                && (type.equals("SalesManager")
                || type.equals("SalesEmployee")
                || type.equals("AccountManager"));
    }

    public boolean isDiscountExisting(DiscountData discount) {
        return discount != null;
    }

    public boolean isDiscountMoreThanZero(double amount) {
        return !(amount <= 0);
    }

    public boolean isDiscountLessThanHundred(double amount) {
        return !(amount > 100);
    }

    public boolean areDiscountDaysValid(int days) {
        return days > 0;
    }

}
