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
