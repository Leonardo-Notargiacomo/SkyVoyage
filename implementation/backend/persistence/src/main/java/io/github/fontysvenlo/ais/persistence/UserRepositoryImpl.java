package io.github.fontysvenlo.ais.persistence;
import io.github.fontysvenlo.ais.persistence.api.UserRepository;

import java.sql.*;
import java.util.Optional;

public class UserRepositoryImpl implements UserRepository {
    private final String url = "localhost";
    private final String user = "PRJ2-User-09";
    private final String password = "PRJ2-Password-09";

    @Override
    public Optional<String> getPasswordByEmail(String email) {
        String query = "SELECT password FROM employeeqq WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return Optional.of(rs.getString("password"));
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exception (e.g., log it)
        }
        return Optional.empty();
    }
}
