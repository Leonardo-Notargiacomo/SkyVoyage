package io.github.fontysvenlo.ais.businesslogic;

import io.github.fontysvenlo.ais.businesslogic.api.LoginService;
import io.github.fontysvenlo.ais.persistence.api.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Optional;

public class LoginServiceImpl implements LoginService {
    private final UserRepository userRepository;

    public LoginServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean login(String email, String password) {
        return userRepository.getPasswordByEmail(email)
                        .map(storedPassword -> BCrypt.checkpw(password, storedPassword))
                        .orElse(false);
    }
}
