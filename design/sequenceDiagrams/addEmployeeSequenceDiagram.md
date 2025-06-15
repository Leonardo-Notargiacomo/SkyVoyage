sequenceDiagram
  actor Client as Account Manager
  participant ER as EmployeeResource
  participant EM as EmployeeManagerImpl
  participant V as Validator
  participant REPO as EmployeeRepositoryImpl

  Note over Client, REPO: Happy Path - Successful Employee Creation
  Client ->>+ ER: POST /employees (Employee JSON)
  ER ->>+ EM: add(employeeData)
  EM ->>+ V: isValidName(employeeData.Firstname)
  V -->>- EM: true
  EM ->>+ V: isValidName(employeeData.Lastname)
  V -->>- EM: true
  EM ->>+ V: isValidEmail(employeeData.Email)
  V -->>- EM: true
  EM ->>+ V: isValidPassword(employeeData.Password)
  V -->>- EM: true
  EM ->>+ V: isValidType(employeeData.Type)
  V -->>- EM: true
  EM ->> EM: hashPassword(employeeData.Password)
  EM ->>+ REPO: add(employeeData)
  REPO -->>- EM: savedEmployeeData
  EM -->>- ER: savedEmployeeData
  ER -->>- Client: 201 Created + savedEmployeeData

  Note over Client, REPO: Exception Path 1 - Invalid Name Validation
  Client ->>+ ER: POST /employees (Employee JSON)
  ER ->>+ EM: add(employeeData)
  EM ->>+ V: isValidName(employeeData.Firstname)
  V -->>- EM: false
  EM -->>- ER: ValidationException("Invalid first name")
  ER ->> ER: createValidationErrorMap("Invalid first name")
  ER -->>- Client: 400 Bad Request + validation errors

  Note over Client, REPO: Exception Path 2 - Invalid Email Validation
  Client ->>+ ER: POST /employees (Employee JSON)
  ER ->>+ EM: add(employeeData)
  EM ->>+ V: isValidName(employeeData.Firstname)
  V -->>- EM: true
  EM ->>+ V: isValidName(employeeData.Lastname)
  V -->>- EM: true
  EM ->>+ V: isValidEmail(employeeData.Email)
  V -->>- EM: false
  EM -->>- ER: ValidationException("Invalid email format")
  ER ->> ER: createValidationErrorMap("Invalid email format")
  ER -->>- Client: 400 Bad Request + validation errors

  Note over Client, REPO: Exception Path 3 - Duplicate Email
  Client ->>+ ER: POST /employees (Employee JSON)
  ER ->>+ EM: add(employeeData)
  EM ->>+ V: isValidName(employeeData.Firstname)
  V -->>- EM: true
  EM ->>+ V: isValidName(employeeData.Lastname)
  V -->>- EM: true
  EM ->>+ V: isValidEmail(employeeData.Email)
  V -->>- EM: true
  EM ->>+ V: isValidPassword(employeeData.Password)
  V -->>- EM: true
  EM ->>+ V: isValidType(employeeData.Type)
  V -->>- EM: true
  EM ->> EM: hashPassword(employeeData.Password)
  EM ->>+ REPO: add(employeeData)
  REPO -->>- EM: DuplicateEmailException("Email already exists")
  EM -->>- ER: DuplicateEmailException("Email already exists")
  ER -->>- Client: 409 Conflict + error message

  Note over Client, REPO: Exception Path 4 - Database Connection Error
  Client ->>+ ER: POST /employees (Employee JSON)
  ER ->>+ EM: add(employeeData)
  EM ->>+ V: isValidName(employeeData.Firstname)
  V -->>- EM: true
  EM ->>+ V: isValidName(employeeData.Lastname)
  V -->>- EM: true
  EM ->>+ V: isValidEmail(employeeData.Email)
  V -->>- EM: true
  EM ->>+ V: isValidPassword(employeeData.Password)
  V -->>- EM: true
  EM ->>+ V: isValidType(employeeData.Type)
  V -->>- EM: true
  EM ->> EM: hashPassword(employeeData.Password)
  EM ->>+ REPO: add(employeeData)
  REPO -->>- EM: DatabaseException("Database connection failed")
  EM -->>- ER: DatabaseException("Database connection failed")
  ER -->>- Client: 500 Internal Server Error + generic error message

  Note over Client, REPO: Exception Path 5 - Invalid Employee Type
  Client ->>+ ER: POST /employees (Employee JSON)
  ER ->>+ EM: add(employeeData)
  EM ->>+ V: isValidName(employeeData.Firstname)
  V -->>- EM: true
  EM ->>+ V: isValidName(employeeData.Lastname)
  V -->>- EM: true
  EM ->>+ V: isValidEmail(employeeData.Email)
  V -->>- EM: true
  EM ->>+ V: isValidPassword(employeeData.Password)
  V -->>- EM: true
  EM ->>+ V: isValidType(employeeData.Type)
  V -->>- EM: false
  EM -->>- ER: ValidationException("Invalid employee type")
  ER ->> ER: createValidationErrorMap("Invalid employee type")
  ER -->>- Client: 400 Bad Request + validation errors