package io.github.fontysvenlo.ais.persistence;

import io.github.fontysvenlo.ais.datarecords.EmployeeData;
import io.github.fontysvenlo.ais.persistence.api.EmployeeRepository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EmployeeRepositoryImpl implements EmployeeRepository {

    private final DataSource db;
    private final List<EmployeeData> employees = new ArrayList<>();

    public EmployeeRepositoryImpl(DataSource db) {
        this.db = db;
    }
    @Override
    public EmployeeData add(EmployeeData employeeData) {
        int maxId = employees.stream().mapToInt(EmployeeData::id).max().orElse(0);
        EmployeeData employeeDataWithId = new EmployeeData(maxId++, employeeData.firstName(), employeeData.lastName(), employeeData.username(), employeeData.password());
        employees.add(employeeDataWithId);
        return employeeDataWithId;
    }
    @Override
    public EmployeeData getByUsername(String username) {
        return employees.stream().filter(e -> e.username().equals(username)).findFirst().orElse(null);
    }

    @Override
    public List<EmployeeData> getAll() {
        return Collections.unmodifiableList(employees);
    }

    @Override
    public EmployeeData login(String username, String password) {
        return null;
    }
}
