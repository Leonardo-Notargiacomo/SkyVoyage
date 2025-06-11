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

1. Sales Employee submits a flight search request with valid criteria.
2. The system retrieves flight data from the external API.
3. The system displays a list of available flights, including details such as:
   - Deaparture: "Amsterdam"
   - Arrival: "Sint Maarten"
   - Duration: "15 hours"
   - Airline: "Lufthansa"
   - Price: "1,500€"
4. Sales Employee can sort the flights (e.g., by price, duration, airline).
5. Sales Employee selects a flight to view more details.
6. System navigates to the flight details page.

**Result**:

- System successfully displays a list of flights matching the search criteria.
- Sales Employee can proceed to the next step (e.g., booking).
  
---

**Name**: testDisplayFlightsNoResult

**Precondition**: Sales Employee is logged into the system and made a flight search with criteria that may not return results.

**Scenario**:

1. Sales Employee submits a flight search request with criteria that have no available flights.
2. The system processes the request.
3. No flights match the criteria.
4. The system displays a message: "No flights found. Try modifying your search criteria."
5. The system suggests alternative search options (e.g., nearby airports, different dates).

**Result**:

- Sales Employee is informed that no flights are available.
- Sales Employee can modify the search criteria and try again.


**Extension**: N/A

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

# Test Case: Discount Management

**Name**: testAddDiscountThrowsExceptionForInvalidInput

**Precondition**: The system has a discount management module with validation rules in place.

**Scenario**:

1. A discount object is created with invalid data:
   - ID: 6
   - Name: "Invalid"
   - Amount: 110.0 (exceeds 100%)
   - Type: "regular"
   - EmployeeID: 6
   - Days: 10
2. The system attempts to add this discount through the discount manager.
3. The system validates the discount data before adding it to the repository.

**Result**:

- System throws an IllegalArgumentException due to the invalid discount amount.
- The discount is not added to the repository.
- The repository's add method is never called.

---

**Name**: testCalculateDiscountedPriceWithNoApplicableDiscount

**Precondition**: The system has existing discount rules in the repository.

**Scenario**:

1. A base price of 100.0 is provided.
2. A departure date 50 days in the future is specified.
3. The system has two discounts in the repository:
   - "Early Bird" discount of 15% applicable for bookings 30 days before departure
   - "Super Early" discount of 25% applicable for bookings 20 days before departure
4. The system calculates the discounted price based on these parameters.

**Result**:

- No discount is applied since the booking is 50 days before departure, which doesn't match any discount criteria.
- The original base price (100.0) is returned without any discount.

---

**Name**: testCalculateDiscountedPriceWithEmptyDiscountList

**Precondition**: The discount repository is empty.

**Scenario**:

1. A base price of 100.0 is provided.
2. A departure date 15 days in the future is specified.
3. The discount repository contains no discount rules.
4. The system calculates the discounted price.

**Result**:

- No discount is applied since there are no discount rules in the repository.
- The original base price (100.0) is returned without any discount.

---

**Name**: testCalculateDiscountedPriceWithZeroDiscount

**Precondition**: The system has a discount rule with 0% discount in the repository.

**Scenario**:

1. A base price of 100.0 is provided.
2. A departure date 15 days in the future is specified.
3. The system has one discount in the repository:
   - "No Discount" with 0% applicable for bookings 30 days before departure
4. The system calculates the discounted price.

**Result**:

- Even though the discount rule applies, the discount percentage is 0%.
- The original base price (100.0) is returned without any change.

---

**Name**: testCalculateDiscountedPriceWithSingleApplicableDiscount

**Precondition**: The system has multiple discount rules in the repository.

**Scenario**:

1. A base price of 100.0 is provided.
2. A departure date 25 days in the future is specified.
3. The system has two discounts in the repository:
   - "Early Bird" discount of 15% applicable for bookings 30 days before departure
   - "Super Early" discount of 25% applicable for bookings 20 days before departure
4. The system calculates the discounted price.

**Result**:

- Only the "Early Bird" discount is applicable (25 days ≤ 30 days).
- The "Super Early" discount does not apply (25 days > 20 days).
- The discounted price is calculated as 100.0 - (100.0 * 0.15) = 85.0.

---

**Name**: testCalculateDiscountedPriceWithMultipleApplicableDiscounts

**Precondition**: The system has multiple discount rules in the repository.

**Scenario**:

1. A base price of 100.0 is provided.
2. A departure date 15 days in the future is specified.
3. The system has three discounts in the repository:
   - "Early Bird" discount of 15% applicable for bookings 30 days before departure
   - "Super Early" discount of 25% applicable for bookings 20 days before departure
   - "Last Minute" discount of 10% applicable for bookings 10 days before departure
4. The system calculates the discounted price.

**Result**:

- Both "Early Bird" (15%) and "Super Early" (25%) discounts are applicable.
- The "Last Minute" discount does not apply (15 days > 10 days).
- The system selects the highest applicable discount (25%).
- The discounted price is calculated as 100.0 - (100.0 * 0.25) = 75.0.

---

**Name**: testCalculateDiscountedPriceWithLastMinuteDiscount

**Precondition**: The system has multiple discount rules including last-minute discounts.

**Scenario**:

1. A base price of 100.0 is provided.
2. A departure date 5 days in the future is specified.
3. The system has three discounts in the repository:
   - "Early Bird" discount of 15% applicable for bookings 30 days before departure
   - "Super Early" discount of 25% applicable for bookings 20 days before departure
   - "Last Minute" discount of 35% applicable for bookings 7 days before departure
4. The system calculates the discounted price.

**Result**:

- All three discounts are applicable, including the "Last Minute" discount.
- The system selects the highest applicable discount (35%).
- The discounted price is calculated as 100.0 - (100.0 * 0.35) = 65.0.

---

**Name**: testGetAllDiscounts

**Precondition**: The discount repository contains discount records.

**Scenario**:

1. The system contains two discount records in the repository:
   - Discount 1: 10% discount, type "type1", 5 days before departure
   - Discount 2: 20% discount, type "type2", 10 days before departure
2. The system requests all discounts from the discount manager.

**Result**:

- The system successfully retrieves all discount records from the repository.
- The returned list contains two discount objects with the correct names "Discount 1" and "Discount 2".

---

