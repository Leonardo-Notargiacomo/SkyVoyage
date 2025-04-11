# Test Cases

This document outlines the test cases for the project. Each test case describes a specific scenario to be tested, including the steps to reproduce, expected results, and actual results.

---

# Test Case: Create Employee

**Name**: testCreateEmployee

**Precondition**: Actor is logged into the system and is on the employee management interface.

**Scenario**:

1. Actor selects the option to create a new account.
2. System prompts the Actor to enter the employee's details (name, email, role).
3. Actor enters the required information:
   - Name: "John Smith"
   - Email: "john.smith@company.com"
   - Role: "Sales employee"
4. System validates the information:
   - Checks if email format is valid
   - Verifies email is not already in use
   - Confirms all required fields are filled
5. Actor submits the form.
6. System creates the new account for the employee with ID "EMP2025001".
7. Test checks if the account was created successfully by querying the database for the new employee record.
8. Actor receives a confirmation message: "Employee account for John Smith has been created successfully."

**Result**:

- Actor successfully creates a new account for the employee.
- Employee ID "EMP2025001" is generated and stored in the system.

---

**Extension**: N/A

**Name**: testCreateEmployeeInvalidEmail

**Precondition**: Actor is logged into the system and is on the employee management interface.

**Scenario**:

1. Actor selects the option to create a new account.
2. System prompts the Actor to enter the employee's details (name, email, role).
3. Actor enters the following information:
   - Name: "Jane Doe"
   - Email: "john.smith@company.com" (already in use)
   - Role: "Sales employee"
4. Test checks if the system displays the error message: "Email address is already associated with an existing account. Please use a different email address."

**Result**:

- System displays the error message: "Email address is already associated with an existing account. Please use a different email address."
- Form is not submitted and account is not created.
- System highlights the email field in red to indicate the error.

**Extension**: 4a. If the entered email is already in use, the system displays an error message and prompts the actor to enter a different email.

---

**Name**: testCreateEmployeeInvalidInformation

**Precondition**: Actor is logged into the system and is on the employee management interface.

**Scenario**:

1. Actor selects the option to create a new account.
2. System prompts the Actor to enter the employee's details (name, email, role).
3. Actor enters the following information:
   - Name: "" (empty)
   - Email: "invalid-email"
   - Role: "Sales manager"
4. Actor submits the form.
5. Test checks if the system displays appropriate error messages for invalid fields.

**Result**:

- System displays the error message: "Please correct the following errors:"
- System shows "Name field cannot be empty" next to the name field.
- System shows "Invalid email format" next to the email field.
- Form is not submitted and account is not created.
- System highlights the invalid fields in red.

**Extension**: 6a. If the information is invalid, the system displays an error message and prompts the actor to correct the information.

---

**Name**: testCreateEmployeeSystemError

**Precondition**: Actor is logged into the system and is on the employee management interface. The database connection is configured to fail during account creation.

**Scenario**:

1. Actor selects the option to create a new account.
2. System prompts the Actor to enter the employee's details (name, email, role).
3. Actor enters valid information:
   - Name: "Alex Johnson"
   - Email: "alex.johnson@company.com"
   - Role: "Account manager"
4. Actor submits the form.
5. System attempts to create the account but encounters a database connection error.
6. Test checks if the system displays an appropriate error message.

**Result**:

- System displays the error message: "An unexpected error occurred while creating the account. Please try again later or contact system administrator."
- Account is not created in the system.

**Extension**: 6b. If the system encounters an unexpected error, the system displays an error message.

---

# Test Case: Search Flights

**Name**: testSearchFlights

**Precondition**: The sales employee is logged into the system and on the flight search interface.

**Scenario**:

1. Sales employee selects the flight search option.
2. The system displays the flight search interface.
3. Sales employee enters flight search criteria:
   - Deaparture: "Amsterdam"
   - Arrival: "Sint Maarten"
   - Travel date: "24.03.2025 - "07.04.2025
   - Number passengers: "2"
4. Sales employee clicks the "Search" button.
   - Checks if Deaparture & Arrival exists.
   - Checks if Travel date is not in the past.
   - Checks if Number passengers is not an negative number.
5. The system retrieves available flights based on the provided criteria.
6. The system displays a list of available flights, including details such as:
   - Airline name
   - Departure time
   - Arrival time
   - Duration
   - Price
8. Sales employee can sort or filter the results (e.g., by price, airline, duration).

**Result**:

- Flights matching the search criteria are displayed.
- Sales employee can view flight details and proceed with the next steps (e.g., booking).

---

**Name**: testSearchFlightsInvalidInformation

**Precondition**: Sales Employee is logged into the system and is on the search flights interface.

**Scenario**:

1. Sales Employee selects the flight search option.
2. System displays the flight search interface.
3. Sales Employee enters the following invalid search criteria:
   - Departure: " " (empty)
   - Arrival: " " (empty)
   - Travel date: "21.03.2024" (date in the past)
   - Number passengers: "-1" (negativ number)
4. Sales Employee submits the search request.
5. Test checks if the system displays appropriate error messages for invalid fields.

**Result**:

- System shows "Departure cannot be empty" next to the departure field.
- System shows "Arrival cannot be empty" next to the arrival field.
- System shows "Invalid date format" next to the date field.
- System shows "Number of passengers must be at least 1" next to the passenger field
- Form is not submitted, and no search results are displayed.
- System highlights the invalid fields in red.

**Extension**: 6a. If the information is invalid, the system displays an error message and prompts the Sales Employee to correct the information.

---

**Name**: testSearchFlightsNoResult

**Precondition**: Sales Employee is logged into the system and is on the flight search interface.

**Scenario**:

1. Sales Employee selects the flight search option.
2. System displays the flight search interface.
3. Sales Employee enters the following search criteria:
   - Departure: "Amsterdam"
   - Arrival: "Sint Maarten" 
   - Travel date: "26.03.2025" - "06.04.2025" 
   - Number passengers: "1"
4. Sales Employee submits the search request.
5. System processes the request and checks for available flights.
6. No flights are found for the given criteria.

**Result**:

- System displays the message: "No flights found for the selected criteria. Please try different dates or departure."
- System suggests nearby airports or alternative travel dates (if available).
- Sales Employee can modify the search criteria and try again.


**Extension**: 6a. If no flights are found, the system provides alternative suggestions or prompts the user to refine their search.

---

**Name**: testSearchFlightsSystemError

**Precondition**: Sales Employee is logged into the system and is on the flight search interface.

**Scenario**:

1. Sales Employee selects the flight search option.
2. System displays the flight search interface.
3. Sales Employee enters the following search criteria:
   - Departure: "Amsterdam"
   - Arrival: "Sint Maarten" 
   - Travel date: "26.03.2025" - "06.04.2025" 
   - Number passengers: "1"
4. Sales Employee submits the search request.
5. System encounters an unexpected error (e.g., database connection issue or API failure).

**Result**:

- System displays an error message: "An error occurred while retrieving flights. Please try again later."
- System logs the error for debugging.
- Sales Employee cannot view flight results until the issue is resolved.


**Extension**: 5a. If the system encounters an unexpected error, the system displays an error message and logs the issue for further investigation.

---

# Test Case: Display Flights

**Name**: testDisplayFlightsSuccessful

**Precondition**: The sales employee is logged into the system and made a flight search.

**Scenario**:

1. Sales Employee submits a flight search request
2. The system retrieves flight data from the external API.
3. The system displays a list of available flights, including details such as:
   - Deaparture: "Schiphol (AMS)"
   - Arrival: "Norwich International Airport (NWI)"
   - Departure Date: "31/03/2025, 11:15"
   - Arrival Date: "31/03/2025, 12:15"
   - Duration: "1 hour"
   - Price: "16€"
4. Sales Employee selects a flight to view more details.

**Result**:

- System successfully displays a list of flights matching the search criteria.
- Sales Employee can proceed to the next step (e.g., booking).
  
---

**Name**: testDisplayFlightsSystemError

**Precondition**:  The sales employee is logged into the system and made a flight search.

1. Sales Employee submits a flight search request.
2. The system attempts to retrieve flight data but encounters an unexpected error (e.g., API failure, database connection issue).
3. The system displays an error message: "An error occurred while retrieving flights. Please try again later."
4. The system logs the error for debugging.

**Result**:

- Sales Employee is informed of the issue and cannot view flight results until it is resolved.
- The system logs the error for further investigation.


**Extension**: N/A

---

# Test Case: Set Price Per Kilometre

**Name**: testSetPrice

**Precondition**: Actor is logged into the system and is on the sales manager interface.

**Scenario**:

1. Actor navigates to the sales manager section.
2. System displays the current price per kilometre.
3. System prompts the Actor to enter the new price per kilometre.
4. Actor enters the new price (e.g., "0.15").
5. System validates the entered price:
   - Checks if the price is a valid number
   - Confirms the price is within the acceptable range (e.g., larger than €0.01)
6. Actor submits the price.
7. System updates the price per kilometre in the system.
8. Test checks if the price was updated successfully by querying the system for the new price.
9. Actor receives a confirmation message: "Price per kilometre has been updated successfully."

**Result**:

- Actor successfully sets a new price per kilometre.
- The new price is saved and used for future calculations.
  
---

**Name**: testSetPriceInvalid

**Precondition**: Actor is logged into the system and is on the sales manager interface.

**Scenario**:

1. Actor navigates to the sales manager section.
2. System displays the current price per kilometre.
3. System prompts the Actor to enter the new price per kilometre.
4. Actor enters an invalid price (e.g., "-0.15" or "abc").
5. System validates the entered price:
   - Checks if the price is a valid number
   - Confirms the price is larger than €0
6. System displays an error message: "Invalid price. Please enter a valid number larger than €0"
7. Actor corrects the price and submits the price again.

**Result**:

- Actor successfully sets a new price per kilometre.
- The new price is saved and used for future calculations.

---

# Test Case: Log into system

**Name**: testLoginSuccessful

**Precondition**: Actor has a valid account with an assigned role & is on the login page.

**Scenario**:

1. Actor enters a valid email and correct password.
2. Actor submits the login request.
3. System verifies the credentials.
4. System checks the user’s role and grants access:
    - Sales Employee → Flight search and booking.
    - Account Manager → User creation, flight search.
    - Sales Manager → Flight search, booking management, and viewing bookings.
5. System redirects the Actor to the appropriate dashboard.

**Result**:

- Actor is successfully logged in.
- Actor is redirected to the dashboard.
  
---

**Name**: testLoginInvalidCredentials

**Precondition**: Actor is on the login

**Scenario**:

1. Actor enters an incorrect email or wrong password
2. Actor submits the login request.
3. System verifies credentials and determines they are invalid.
4. System displays an error message:
   -  Invalid username or password. Please try again."
5. Actor remains on the login page.

**Result**:

- Actor is not logged in.
- System displays an error message.
