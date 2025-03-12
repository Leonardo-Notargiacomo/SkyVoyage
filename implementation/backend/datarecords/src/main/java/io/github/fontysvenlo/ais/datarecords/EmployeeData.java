package io.github.fontysvenlo.ais.datarecords;

/**
 * Data record for an employee.
 * A record is not mutable. Getter methods (e.g. firstName(), not getFirstName()),
 * hashCode(), equals() and toString() available for free.
 * @param id the unique identifier of the employee
 * @param Firstname the first name of the employee
 * @param Lastname the last name of the employee
 * @param email the email of the employee
 * @param Password the password of the employee
 * @param type the type of the employee
 */

public record EmployeeData (Integer id, String Firstname, String Lastname, String email, String Password, String type) { }
