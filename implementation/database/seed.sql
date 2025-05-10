-- Insert roles
INSERT INTO Role (Name, IsAdmin) VALUES
( 'SalesManager', False),
( 'SalesEmployee', FALSE),
( 'AccountManager', True);

-- Insert employees
INSERT INTO Employee (Firstname, Lastname, Email, Password, RoleID) VALUES
('Furkan', 'Öztürk', 'furkan@fontys.nl', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 3), -- password: 'password'
('Thomas', 'Lindelauf', 'Thomas@fontys.nl', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 3),
('Leonardo', 'Notargiacomo', 'Leonardo@fontys.nl', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 3),
('Til', 'Mozes', 'Til@fontys.nl', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 1),
('Soheil', 'Abdali', 'Soheil@fontys.nl', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 2);

-- Insert Addresses
INSERT INTO Address (Street, HouseNumber, City, Country) VALUES
('Main Street', '123', 'New York', 'USA'),
('Second Street', '456A', 'London', 'UK'),
('Third Avenue', '789', 'Berlin', 'Germany');

-- Insert Customers
INSERT INTO Customer (Firstname, Lastname, Email, PhoneNumber, AddressID, IsInfant) VALUES
('John', 'Doe', 'john.doe@example.com', '+123456789', 1, FALSE),
('Jane', 'Smith', 'jane.smith@example.com', '+987654321', 2, FALSE),
('Alice', 'Johnson', 'alise.johnson@example.com', '+555666777', 3, FALSE),
('Baby', 'Doe', 'baby.doe@example.com', '+111222333', 1, TRUE);

-- Insert Flights
INSERT INTO Flight (
    ID, Departure_Airport, Departure_Airport_Short, Departure_Terminal, Departure_Gate, Departure_Scheduled_Time, Departure_Delay,
    Arrival_Airport, Arrival_Airport_Short, Arrival_Terminal, Arrival_Gate, Arrival_Scheduled_Time, Arrival_Delay, Duration
) VALUES
('FL001', 'JFK International', 'JFK', 'T1', 'G5', '2025-05-11 08:00:00', 0, 'Heathrow', 'LHR', 'T3', 'B2', '2025-05-11 20:00:00', 15, 480),
('FL002', 'Berlin Tegel', 'TXL', 'A', '3', '2025-05-12 14:00:00', 10, 'JFK International', 'JFK', 'T4', 'D1', '2025-05-12 18:30:00', 0, 510);

-- Insert Bookings
INSERT INTO Booking (EmployeeID, IsActive) VALUES
(1, TRUE),
(2, TRUE);

-- Insert Booking_Flight entries
INSERT INTO Booking_Flight (BookingID, FlightID) VALUES
(1, 'FL001'),
(2, 'FL002');

-- Insert Tickets
INSERT INTO Ticket (FlightID, CustomerID, Tariff) VALUES
('FL001', 1, 350),
('FL001', 4, 100),
('FL002', 2, 400);
('FL002', 3, 250);