package io.github.fontysvenlo.ais.persistence.api;

import io.github.fontysvenlo.ais.datarecords.EmployeeData;

import java.util.List;

public interface EmployeeRepository {

    EmployeeData add(EmployeeData employeeData);

    EmployeeData update(EmployeeData employeeData);

    EmployeeData delete(String id);

    EmployeeData getOne(String id);

    List<EmployeeData> getAll();
}
