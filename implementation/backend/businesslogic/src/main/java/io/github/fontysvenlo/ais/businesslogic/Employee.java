package io.github.fontysvenlo.ais.businesslogic;

import io.github.fontysvenlo.ais.businesslogic.api.ValidatorInterface;
import io.github.fontysvenlo.ais.datarecords.EmployeeData;

public class Employee {
    private EmployeeData employeeData;

    /**
     * Constructor
     * @param employeeData the customer data
     */
    public Employee(EmployeeData employeeData) {
        this.employeeData = employeeData;
    }
}
