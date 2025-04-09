package io.github.fontysvenlo.ais.persistence;
import io.github.fontysvenlo.ais.persistence.api.UserRepository;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Optional;

public class UserRepositoryImpl implements UserRepository {

    private final DataSource db;
    public UserRepositoryImpl(DBConfig config) {
        this.db = DBProvider.getDataSource(config);
    }



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
