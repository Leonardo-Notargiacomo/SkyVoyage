package io.github.fontysvenlo.ais.businesslogic;

import io.github.fontysvenlo.ais.businesslogic.api.ValidatorInterface;

public class Validator implements ValidatorInterface {

    //Checks if the provided email is not `null` and matches a common pattern like `username@domain.com`.
    public boolean isValidEmail(String email) {
        return email != null && email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
    }

    //Checks if the provided phone number is not `null` and matches a common pattern like `+31 6 12345678`.
    public boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^\\+?[0-9]{1,3}[-.\\s]?\\(?[0-9]{1,3}\\)?[-.\\s]?[0-9]{1,4}[-.\\s]?[0-9]{1,4}$");
    }

    //Ensures the name is not `null` and contains only letters, allowing some punctuation and spacing.
    public boolean isValidName(String name) {
        return name != null && name.matches("^[a-zA-Z]+(([',. -][a-zA-Z ])?[a-zA-Z]*)*$");
    }

    //Requires a `non-null` password with uppercase, lowercase, digits, special characters, and at least eight total characters.
    public boolean isValidPassword(String password) {
        return password != null && password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");
    }
}
