package io.github.fontysvenlo.ais.businesslogic.api;

import io.github.fontysvenlo.ais.datarecords.EmployeeData;

import java.util.List;

public interface EmployeeManager {


    public EmployeeData add(EmployeeData employeeData);

    public List<EmployeeData> list();

    public EmployeeData getOne(String id);

    public EmployeeData update(EmployeeData employeeData);

    public EmployeeData delete(String id);

}
