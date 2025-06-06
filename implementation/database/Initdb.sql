-- Create Role table
CREATE TABLE Role (
    ID SERIAL PRIMARY KEY,
    Name VARCHAR(256),
    IsAdmin BOOLEAN
);

-- Create Address table
CREATE TABLE Address (
    ID SERIAL PRIMARY KEY,
    Street VARCHAR(256),
    HouseNumber VARCHAR(256),
    City VARCHAR(256),
    Country VARCHAR(256)
);

-- Create Customer table
CREATE TABLE Customer (
    ID SERIAL PRIMARY KEY,
    Firstname VARCHAR(256),
    Lastname VARCHAR(256),
    Email VARCHAR(256),
    PhoneNumber VARCHAR(256),
    AddressID INTEGER,
    IsInfant BOOLEAN,
    FOREIGN KEY (AddressID) REFERENCES Address(ID)
);

-- Create Employee table
CREATE TABLE Employee (
    ID SERIAL PRIMARY KEY,
    Firstname VARCHAR(256),
    Lastname VARCHAR(256),
    Email VARCHAR(256),
    Password VARCHAR(256),
    RoleID INTEGER,
    FOREIGN KEY (RoleID) REFERENCES Role(ID)
);

-- Create Flight table
CREATE TABLE Flight (
    ID VARCHAR(56) PRIMARY KEY,
    Departure_Airport VARCHAR(256),
    Departure_Airport_Short VARCHAR(56),
    Departure_Terminal VARCHAR(256),
    Departure_Gate VARCHAR(56),
    Departure_Scheduled_Time TIMESTAMP,
    Departure_Delay INTEGER,
    Arrival_Airport VARCHAR(256),
    Arrival_Airport_Short VARCHAR(56),
    Arrival_Terminal VARCHAR(256),
    Arrival_Gate VARCHAR(56),
    Arrival_Scheduled_Time TIMESTAMP,
    Arrival_Delay INTEGER,
    Duration INTEGER
);

-- Create Booking table
CREATE TABLE Booking (
    ID SERIAL PRIMARY KEY,
    EmployeeID INTEGER,
    IsActive BOOLEAN,
    FOREIGN KEY (EmployeeID) REFERENCES Employee(ID)
);

-- Create Ticket table
CREATE TABLE Ticket (
    ID SERIAL PRIMARY KEY,
    FlightID VARCHAR(56),
    CustomerID INTEGER,
    Tariff INTEGER,
    FOREIGN KEY (FlightID) REFERENCES Flight(ID),
    FOREIGN KEY (CustomerID) REFERENCES Customer(ID)
);

-- Create Discount table
CREATE TABLE Discount (
    ID SERIAL PRIMARY KEY,
    Name VARCHAR(256),
    Amount DOUBLE PRECISION,
    Type VARCHAR(256),
    EmployeeID INTEGER,
    Days INTEGER,
    FOREIGN KEY (EmployeeID) REFERENCES Employee(ID)
);

-- Create junction tables for many-to-many relationships

-- Create Booking_Flight junction table
CREATE TABLE Booking_Flight (
    BookingID INTEGER,
    FlightID VARCHAR(56),
    PRIMARY KEY (BookingID, FlightID),
    FOREIGN KEY (BookingID) REFERENCES Booking(ID),
    FOREIGN KEY (FlightID) REFERENCES Flight(ID)
);

-- Create Discount_Ticket junction table
CREATE TABLE Discount_Ticket (
    DiscountID INTEGER,
    TicketID INTEGER,
    PRIMARY KEY (DiscountID, TicketID),
    FOREIGN KEY (DiscountID) REFERENCES Discount(ID),
    FOREIGN KEY (TicketID) REFERENCES Ticket(ID)
);