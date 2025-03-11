package io.github.fontysvenlo.ais.businesslogic.api;

import io.github.fontysvenlo.ais.datarecords.EmployeeData;

import java.util.List;

public interface EmployeeManager {
    EmployeeData add(EmployeeData employeeData);
    List<EmployeeData> getAll();
    EmployeeData login(String username, String password);
}
