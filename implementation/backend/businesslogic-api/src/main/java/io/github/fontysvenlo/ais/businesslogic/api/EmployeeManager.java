package io.github.fontysvenlo.ais.businesslogic.api;

import io.github.fontysvenlo.ais.datarecords.EmployeeData;

import java.util.List;

public interface EmployeeManager {

    public EmployeeData add(EmployeeData employeeData);

    public List<EmployeeData> list();

    public EmployeeData get(String id);

    public EmployeeData update(EmployeeData employeeData);

    public void delete(String id);

}
