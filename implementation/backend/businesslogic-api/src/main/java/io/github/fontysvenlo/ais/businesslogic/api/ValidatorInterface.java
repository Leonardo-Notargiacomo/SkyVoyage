package io.github.fontysvenlo.ais.businesslogic.api;

public interface ValidatorInterface {
    boolean isValidEmail(String email);
    boolean isValidPhoneNumber(String phoneNumber);
    boolean isValidName(String name);
    boolean isValidPassword(String password);
}
