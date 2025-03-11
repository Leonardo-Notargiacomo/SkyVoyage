package io.github.fontysvenlo.ais.persistence.api;

import io.github.fontysvenlo.ais.datarecords.EmployeeData;

import java.util.List;

public interface EmployeeRepository {
    EmployeeData add(EmployeeData employeeData);
    List<EmployeeData> getAll();
    EmployeeData login(String username, String password);

    EmployeeData getByUsername(String username);
}
