package io.github.fontysvenlo.ais.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.sql.DataSource;

import io.github.fontysvenlo.ais.datarecords.EmployeeData;
import io.github.fontysvenlo.ais.persistence.api.EmployeeRepository;

/**
 * This class knows everything about storing and retrieving employees from the
 * database. Uses JDBC to connect to a PostgreSQL database.
 */
class EmployeeRepositoryImpl implements EmployeeRepository {

    private final DataSource db;

    public EmployeeRepositoryImpl(DBConfig config) {
        this.db = DBProvider.getDataSource(config);
    }

    /**
     * Maps employee type name to role ID in the database.
     *
     * @param typeName the employee type name
     * @return the corresponding role ID
     */
    private int getRoleIdByTypeName(String typeName) {
        try (Connection connection = db.getConnection(); PreparedStatement stmt = connection.prepareStatement(
                "SELECT id FROM public.role WHERE name = ?")) {
            stmt.setString(1, typeName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
                // Default to first role if not found
                return 1;
            }
        } catch (SQLException e) {
            return 1; // Default to first role
        }
    }

    /**
     * Maps role ID to employee type name.
     *
     * @param roleId the role ID
     * @return the corresponding employee type name
     */
    private String getTypeNameByRoleId(int roleId) {
        try (Connection connection = db.getConnection(); PreparedStatement stmt = connection.prepareStatement(
                "SELECT name FROM public.role WHERE id = ?")) {
            stmt.setInt(1, roleId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
                return "SalesEmployee"; // Default
            }
        } catch (SQLException e) {
            return "SalesEmployee"; // Default
        }
    }

    @Override
    public EmployeeData add(EmployeeData employeeData) {
        // Try-with-resources to ensure connection and statement are closed automatically
        try (Connection connection = db.getConnection(); PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO public.employee (firstname, lastname, email, password, roleid) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {

            // Get the role ID from the employee type name
            int roleId = getRoleIdByTypeName(employeeData.Type());
            // Convert email to lowercase for consistency
            String lowercaseEmail = employeeData.Email().toLowerCase();

            // Set parameters for the insert statement
            stmt.setString(1, employeeData.Firstname());
            stmt.setString(2, employeeData.Lastname());
            stmt.setString(3, lowercaseEmail);
            stmt.setString(4, employeeData.Password());
            stmt.setInt(5, roleId);

            // Execute the insert and check if a row was affected
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating employee failed, no rows affected.");
            }

            // Retrieve the generated ID for the new employee
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    // Return a new EmployeeData object with the generated ID
                    return new EmployeeData(id, employeeData.Firstname(), employeeData.Lastname(),
                            lowercaseEmail, employeeData.Password(), employeeData.Type());
                } else {
                    throw new SQLException("Creating employee failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            // Return null if any SQL exception occurs
            return null;
        }
    }

    @Override
    public EmployeeData update(EmployeeData employeeData) {
        try (Connection connection = db.getConnection(); PreparedStatement stmt = connection.prepareStatement(
                "UPDATE public.employee SET firstname = ?, lastname = ?, email = ?, password = ?, roleid = ? WHERE id = ?")) {

            int roleId = getRoleIdByTypeName(employeeData.Type());
            String lowercaseEmail = employeeData.Email().toLowerCase();

            stmt.setString(1, employeeData.Firstname());
            stmt.setString(2, employeeData.Lastname());
            stmt.setString(3, lowercaseEmail);
            stmt.setString(4, employeeData.Password());
            stmt.setInt(5, roleId);
            stmt.setInt(6, employeeData.id());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                return new EmployeeData(
                        employeeData.id(),
                        employeeData.Firstname(),
                        employeeData.Lastname(),
                        lowercaseEmail,
                        employeeData.Password(),
                        employeeData.Type()
                );
            }
            return null;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public EmployeeData delete(String id) {
        try {
            int employeeId = Integer.parseInt(id);
            EmployeeData employee = getOne(id);

            if (employee != null) {
                try (Connection connection = db.getConnection(); PreparedStatement stmt = connection.prepareStatement(
                        "DELETE FROM public.employee WHERE id = ?")) {
                    stmt.setInt(1, employeeId);
                    int affectedRows = stmt.executeUpdate();

                    if (affectedRows > 0) {
                        return employee;
                    }
                }
            }
            return null;
        } catch (NumberFormatException e) {
            return null;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public EmployeeData getOne(String id) {
        try {
            int employeeId = Integer.parseInt(id);

            try (Connection connection = db.getConnection(); PreparedStatement stmt = connection.prepareStatement(
                    "SELECT e.id, e.firstname, e.lastname, e.email, e.password, r.name as type "
                    + "FROM public.employee e JOIN public.role r ON e.roleid = r.id WHERE e.id = ?")) {
                stmt.setInt(1, employeeId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new EmployeeData(
                                rs.getInt("id"),
                                rs.getString("firstname"),
                                rs.getString("lastname"),
                                rs.getString("email"),
                                rs.getString("password"),
                                rs.getString("type")
                        );
                    }
                }
            }
            return null;
        } catch (NumberFormatException e) {
            return null;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public List<EmployeeData> getAll() {
        List<EmployeeData> result = new ArrayList<>();

        try (Connection connection = db.getConnection(); PreparedStatement stmt = connection.prepareStatement(
                "SELECT e.id, e.firstname, e.lastname, e.email, e.password, r.name as type "
                + "FROM public.employee e JOIN public.role r ON e.roleid = r.id")) {

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new EmployeeData(
                            rs.getInt("id"),
                            rs.getString("firstname"),
                            rs.getString("lastname"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("type")
                    ));
                }
            }
        } catch (SQLException e) {
        }

        return result;
    }
    @Override
    public EmployeeData getByEmail(String email) {
        String statement = "SELECT * FROM public.employee WHERE email = ?";
        try {
            PreparedStatement preparedStatement = db.getConnection().prepareStatement(statement);
            preparedStatement.setString(1, email);
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new EmployeeData(
                        resultSet.getInt("id"),
                        resultSet.getString("firstname"),
                        resultSet.getString("lastname"),
                        resultSet.getString("email"),
                        resultSet.getString("password"),
                        resultSet.getString("roleid")
                );
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
