package io.github.fontysvenlo.ais.persistence.api;
import java.util.Optional;

public interface UserRepository {
    Optional<String> getPasswordByEmail(String email);
}
