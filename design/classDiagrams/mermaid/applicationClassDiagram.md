```mermaid
---
title: Class Diagram For The Application
---
classDiagram

namespace assembler {
    
    class Assembler{
    }
    class PropertiesLoader{
    }
    
}

namespace businesslogic {
    
    class BusinessLogicFactory{
    }
    class BusinessLogicImpl{
    }
    class Customer{
    }
    class CustomerManagerImpl{
    }
    class Employee{
    }
    class EmployeeManagerImpl{
    }
    class Validator{
    }
    
}

namespace businesslogic-api {
    
    class BusinessLogic{
        <<interface>>
    }
    class CustomerManager{
        <<interface>>
    }
    class EmployeeManager{
        <<interface>>
    }
    class ValidatorInterface{
        <<interface>>
    }
    
}

namespace datarecords {
    
    class CustomerData{
    }
    class EmployeeData{
    }
}

namespace persistence {
    
    class CustomerRepositoryImpl{
    }
    class DBConfig{
    }
    class DBProvider{
    }
    class EmployeeRepositoryImpl{
    }
    class PersistenceFactory{
    }
    class PersistenceImpl{
    }
}

namespace persistence-api {
    class CustomerRepository{
        <<interface>>>
    }
    class EmployeeRepository{
        <<interface>>>
    }
    class Persistence{
        <<interface>>>
    }
}

namespace restapi {
    
    class APIServer{
    }
    class CustomerResource{
    }
    class EmployeeResource{
    }
    class ServerConfig{
    }
}

```