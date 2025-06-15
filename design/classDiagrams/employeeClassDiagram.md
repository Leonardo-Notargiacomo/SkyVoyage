classDiagram
    %% Data Records
    class EmployeeData {
        <<record>>
        +Integer id
        +String Firstname
        +String Lastname
        +String Email
        +String Password
        +String Type
    }

    %% Interfaces
    class EmployeeManager {
        <<interface>>
        +EmployeeData add(EmployeeData)
        +List~EmployeeData~ list()
        +EmployeeData getOne(String)
        +EmployeeData update(EmployeeData)
        +EmployeeData delete(String)
        +EmployeeData getByEmail(String)
    }

    class ValidatorInterface {
        <<interface>>
        +boolean isValidName(String)
        +boolean isValidEmail(String)
        +boolean isValidPhoneNumber(String)
        +boolean isValidPassword(String)
        +boolean isValidType(String)
    }

    class EmployeeRepository {
        <<interface>>
        +EmployeeData add(EmployeeData)
        +EmployeeData update(EmployeeData)
        +EmployeeData delete(String)
        +EmployeeData getOne(String)
        +List~EmployeeData~ getAll()
        +EmployeeData getByEmail(String)
    }

    %% Implementation Classes
    class EmployeeManagerImpl {
        -EmployeeRepository employeeRepository
        -ValidatorInterface validator
        +EmployeeData add(EmployeeData)
        +List~EmployeeData~ list()
        +EmployeeData getOne(String)
        +EmployeeData update(EmployeeData)
        +EmployeeData delete(String)
        +EmployeeData getByEmail(String)
        -String hashPassword(String)
        +boolean verifyPassword(String, String)
    }

    class Validator {
        -static Pattern EMAIL_PATTERN
        -static Pattern PASSWORD_PATTERN
        +boolean isValidName(String)
        +boolean isValidEmail(String)
        +boolean isValidPhoneNumber(String)
        +boolean isValidPassword(String)
        +boolean isValidType(String)
    }

    class EmployeeRepositoryImpl {
        -DataSource db
        +EmployeeData add(EmployeeData)
        +EmployeeData update(EmployeeData)
        +EmployeeData delete(String)
        +EmployeeData getOne(String)
        +List~EmployeeData~ getAll()
        +EmployeeData getByEmail(String)
        -int getRoleIdByTypeName(String)
        -String getTypeNameByRoleId(int)
    }

    class Employee {
        -EmployeeData data
        +Employee(EmployeeData)
        +String getFullName()
        +EmployeeData getData()
    }

    class EmployeeResource {
        -EmployeeManager employeeManager
        +void create(Context)
        +void getAll(Context)
        +void delete(Context, String)
        +void getOne(Context, String)
        +void update(Context, String)
        -Map createValidationErrorMap(String)
    }

    %% Relationships
    EmployeeManagerImpl ..|> EmployeeManager
    Validator ..|> ValidatorInterface
    EmployeeRepositoryImpl ..|> EmployeeRepository

    EmployeeManagerImpl o-- EmployeeRepository
    EmployeeManagerImpl o-- ValidatorInterface
    EmployeeManagerImpl ..> Employee

    Employee o-- EmployeeData

    EmployeeResource o-- EmployeeManager
    EmployeeManagerImpl ..> EmployeeData
    EmployeeRepositoryImpl ..> EmployeeData
    EmployeeResource ..> EmployeeData
