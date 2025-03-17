package io.github.fontysvenlo.ais.businesslogic;

import io.github.fontysvenlo.ais.businesslogic.api.ValidatorInterface;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class Validator implements ValidatorInterface {

    private static final Logger logger = Logger.getLogger(Validator.class.getName());

    private static final Pattern EMAIL_PATTERN
            = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private static final Pattern PASSWORD_PATTERN
            = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$");

    @Override
    public boolean isValidName(String name) {
        if (name == null || name.trim().length() < 2) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    @Override
    public boolean isValidPassword(String password) {
        // Log detailed information about password validation
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
}
