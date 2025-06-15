sequenceDiagram
  actor Client as Account Manager
  participant ER as EmployeeResource
  participant EM as EmployeeManagerImpl
  participant V as Validator
  participant REPO as EmployeeRepositoryImpl

  Client ->> ER: POST /employees (Employee JSON)
  ER ->> EM: add(employeeData)
  EM ->> V: isValidName(employeeData.Firstname)
  V -->> EM: true
  EM ->> V: isValidEmail(employeeData.Email)
  V -->> EM: true
  EM ->> EM: hashPassword(employeeData.Password)
  EM ->> REPO: add(employeeData)
  REPO -->> EM: savedEmployeeData
  EM -->> ER: savedEmployeeData
  ER -->> Client: 201 Created + savedEmployeeData
