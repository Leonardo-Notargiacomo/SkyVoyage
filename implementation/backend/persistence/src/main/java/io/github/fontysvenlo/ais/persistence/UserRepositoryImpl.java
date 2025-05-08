package io.github.fontysvenlo.ais.persistence;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import javax.sql.DataSource;

import io.github.fontysvenlo.ais.persistence.api.UserRepository;

public class UserRepositoryImpl implements UserRepository {

    private final DataSource db;
    public UserRepositoryImpl(DBConfig config) {
        this.db = DBProvider.getDataSource(config);
    }
    
    /**
     * Retrieves the password for a given email from the database.
     *
     * @param email the email address of the user
     * @return an Optional containing the password if found, or an empty Optional if not found
     */
    @Override
    public Optional<String> getPasswordByEmail(String email) {
        String query = "SELECT password FROM public.employee where email = ?;";
        try (PreparedStatement statement = db.getConnection().prepareStatement(query)) {
            statement.setString(1, email);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) return Optional.of(rs.getString("password"));

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exception (e.g., log it)
        }
        return Optional.empty();
    }
}
