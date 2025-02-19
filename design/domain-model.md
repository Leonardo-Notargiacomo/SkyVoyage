```mermaid
---
title: Domain Model For A Travel Agency
---
classDiagram

    Discount-->Ticket
    Discount-->Sales Manager

    Ticket-->Customer
    Ticket-->Flight

    Flight-->Trip
    Flight-->Airport
    Flight-->Plane
    Flight-->KPI

    Employee<|--SalesEmployee
    Employee<|--SalesManager
    Employee<|--AccountManager

    Booking-->SalesEmployee

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