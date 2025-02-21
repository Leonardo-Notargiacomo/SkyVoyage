```mermaid
---
title: Domain Model For A Travel Agency
---
classDiagram

    Discount "*" -- "*" Ticket
    Discount--Sales Manager
    SalesEmployee--Discount

    Ticket--Customer
    Ticket--Flight
    Customer--Booking

    Flight--Trip
    Flight--Airport
    Flight--Plane
    KPI--Flight

    Employee<|--SalesEmployee
    Employee<|--SalesManager
    Employee<|--AccountManager

    Booking--SalesEmployee
    Booking--Trip

    Airport--Plane
    
    AccountManager--KPI

    class Discount{
    }
    class Ticket{
    }
    class Booking{
    }
    class Trip{
    }
    class Employee{
    }
    class Flight{
    }
    class Customer{
    }
    class Airport{
    }
    class Plane{
    }
    class SalesEmployee{
    }
    class SalesManager{
    }
    class AccountManager{
    }
    class KPI {
    }
```