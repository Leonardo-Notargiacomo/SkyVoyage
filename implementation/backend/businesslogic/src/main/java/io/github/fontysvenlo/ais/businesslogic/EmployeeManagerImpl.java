package io.github.fontysvenlo.ais.businesslogic;

import io.github.fontysvenlo.ais.businesslogic.api.EmployeeManager;
import io.github.fontysvenlo.ais.datarecords.EmployeeData;
import io.github.fontysvenlo.ais.persistence.api.EmployeeRepository;

import java.util.List;

public class EmployeeManagerImpl implements EmployeeManager {
    private final EmployeeRepository employeeRepository;

    public EmployeeManagerImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public EmployeeData add(EmployeeData employeeData) {
        Employee employee = new Employee(employeeData);
        return employeeRepository.add(employeeData);
    }

    @Override
    public List<EmployeeData> getAll() {
        return employeeRepository.getAll();
    }

    @Override
    public EmployeeData login(String username, String password) {

        EmployeeData employeeData = employeeRepository.getByUsername(username);
        if (employeeData == null) {
            return null;
        }

        if (employeeData.password().equals(password)) {
            return employeeData;
        }
        return null;
    }
}
