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

---

# Test Case: Create Booking

**Name**: testCreateBookingValid

**Precondition**: Sales Employee is logged in and on the booking overview page with a selected flight and entered customer data.

**Scenario**:

1. Sales Employee selects a flight.
2. Sales Employee fills in all required customer details.
3. Sales Employee clicks on “Confirm Booking.”
4. System checks that all required fields are completed.
5. System saves the booking data.
6. System redirects to the homepage.

**Result**:

- Booking is stored in sessionStorage under “confirmedBooking”.
- System displays a success message: “Booking confirmed!”.

---

**Name**: testCreateBookingWithDiscountReason

**Precondition**: Sales Employee is on the booking overview page with valid flight and customer data.

**Scenario**:

1. Sales Employee enters a discount between 0–100%.
2. Sales Employee provides a reason in the “Reason for Discount” field.
3. Sales Employee clicks on “Apply Discount.”
4. System checks the discount range and updates the store.
5. System updates the total price based on the discount.

**Result**:

- Discount is applied correctly.
- Reason is saved in the store.
- Total price is recalculated and updated.
- If discount is 69%, Dank Mode is activated with visual effects.

---

**Name**: testCreateBookingReservation

**Precondition**: Sales Employee is on the booking overview page with all fields completed.

**Scenario**:

1. Sales Employee clicks on “Reserve (Pay Later).”
2. System verifies that all necessary data is entered.
3. System saves the booking as a reservation.
4. System redirects to the homepage.

**Result**:

- Booking is stored in sessionStorage under “reservedBooking”.
- System displays a message: “Booking reserved for later payment!”.

---

**Name**: testCreateBookingMissingCustomerData

**Precondition**: Sales Employee selects a flight but does not enter complete customer data.

**Scenario**:

1. Sales Employee clicks on “Confirm Booking.”
2. System checks for missing required fields.
3. System prevents submission and shows an error or blocks the button.

**Result**:

- Booking is not stored.
- System displays an error or prevents the action until all fields are completed.

---

**Name**: testCreateBookingInvalidDiscount

**Precondition**: Sales Employee is on the booking overview page.

**Scenario**:

1. Sales Employee enters a discount below 0 or above 100.
2. Sales Employee clicks on “Apply Discount.”
3. System checks the discount range.

**Result**:

- Discount is not applied.
- System may show a warning or ignore invalid input.

---

**Name**: testCreateBookingCancel

**Precondition**: Sales Employee is in the middle of a booking process.

**Scenario**:

1. Sales Employee clicks on “Cancel Booking.”
2. System resets the booking store.
3. System redirects to the homepage.

**Result**:

- Booking is removed.
- System displays message: “Booking cancelled.”
- User is redirected to the homepage.

---

# Test Case: Manage Discounts

**Name**: testConfigureEarlyBirdDiscount

**Precondition**: DiscountManager is initialized with a repository.

**Scenario**:
1. Sales manager creates an Early Bird Discount with the following parameters:
   - ID: 1
   - Name: "Early Bird Discount"
   - Amount: 10.0%
   - Type: "early_bird"
   - Days: 30
2. Sales manager submits the discount configuration.
3. System stores the discount in the repository.
4. Sales manager retrieves the discount by its ID.

**Result**:
- The system successfully stores the early bird discount.
- The retrieved discount matches the submitted configuration with:
  - Name: "Early Bird Discount"
  - Amount: 10.0%
  - Type: "early_bird"
  - Days: 30
- System returns a 201 (Created) status code.

---

**Name**: testConfigureLastMinuteDiscount

**Precondition**: DiscountManager is initialized with a repository.

**Scenario**:
1. Sales manager creates a Last-Minute Discount with the following parameters:
   - ID: 2
   - Name: "Last-Minute Discount"
   - Amount: 20.0%
   - Type: "last_minute"
   - Days: 3
2. Sales manager submits the discount configuration.
3. System stores the discount in the repository.
4. Sales manager retrieves the discount by its ID.

**Result**:
- The system successfully stores the last-minute discount.
- The retrieved discount matches the submitted configuration with:
  - Name: "Last-Minute Discount"
  - Amount: 20.0%
  - Type: "last_minute"
  - Days: 3
- System returns a 201 (Created) status code.

---

**Name**: testValidateDiscountWithInvalidPercentage

**Precondition**: DiscountManager is initialized.

**Scenario**:
1. Sales manager attempts to validate two invalid discounts:
   - A discount with negative percentage (-10.0%)
   - A discount with percentage over 100% (110.0%)
2. System performs validation on both discounts.

**Result**:
- System correctly identifies both discounts as invalid.
- Validation returns false for both discounts.
- System returns a 400 (Bad Request) status code.

---

**Name**: testValidateDiscountWithValidInput

**Precondition**: DiscountManager is initialized.

**Scenario**:
1. Sales manager attempts to validate a discount with valid parameters:
   - Amount: 50.0%
   - Days: 10
2. System performs validation on the discount.

**Result**:
- System identifies the discount as valid.
- Validation returns true.
- System returns a 200 (OK) status code.

---

**Name**: testValidateDiscountWithInvalidDays

**Precondition**: DiscountManager is initialized.

**Scenario**:
1. Sales manager attempts to validate two invalid discounts:
   - A discount with zero days
   - A discount with negative days (-5)
2. System performs validation on both discounts.

**Result**:
- System correctly identifies both discounts as invalid.
- Validation returns false for both discounts.
- System returns a 400 (Bad Request) status code.

---

**Name**: testAddDiscountThrowsExceptionForInvalidInput

**Precondition**: DiscountManager is initialized.

**Scenario**:
1. Sales manager attempts to add a discount with invalid parameters:
   - Amount: 110.0% (exceeds maximum allowed)
2. Sales manager submits the discount.

**Result**:
- System rejects the discount and throws an IllegalArgumentException.
- Error message indicates invalid discount percentage.
- No discount is added to the repository.
- System returns a 400 (Bad Request) status code.

---

**Name**: testGetAllDiscounts

**Precondition**: DiscountManager is initialized and repository contains multiple discounts.

**Scenario**:
1. Sales manager requests to view all available discounts.
2. System queries the repository for all discounts.

**Result**:
- System returns a list of all discounts.
- List contains the correct number of discounts (2).
- Discounts have the correct names: "Discount 1" and "Discount 2".
- System returns a 200 (OK) status code.

---

**Name**: testGetDiscountsByType

**Precondition**: DiscountManager is initialized and repository contains discounts of different types.

**Scenario**:
1. Sales manager requests to view all discounts of type "early_bird".
2. System queries the repository for discounts of the specified type.

**Result**:
- System returns only discounts of type "early_bird".
- List contains the correct number of discounts (2).
- Discounts have the correct names: "Early Bird 1" and "Early Bird 2".
- System returns a 200 (OK) status code.

---

**Name**: testUpdateDiscount

**Precondition**: DiscountManager is initialized and repository contains an existing discount.

**Scenario**:
1. Sales manager retrieves an existing discount with ID 1.
2. Sales manager updates the discount with new parameters:
   - Name: "Updated Discount" (changed from "Original Discount")
   - Amount: 15.0% (changed from 10.0%)
   - Days: 7 (changed from 5)
3. Sales manager submits the updated discount.
4. System updates the discount in the repository.
5. Sales manager retrieves the discount by its ID.

**Result**:
- System successfully updates the discount.
- Retrieved discount matches the updated parameters:
  - Name: "Updated Discount"
  - Amount: 15.0%
  - Days: 7
- System returns a 200 (OK) status code.

---

**Name**: testDeleteDiscount

**Precondition**: DiscountManager is initialized and repository contains an existing discount with ID 1.

**Scenario**:
1. Sales manager requests to delete the discount with ID 1.
2. System deletes the discount from the repository.
3. Sales manager attempts to retrieve the deleted discount by its ID.

**Result**:
- System successfully deletes the discount.
- When attempting to retrieve the deleted discount, system returns an empty result.
- System returns a 204 (No Content) status code.

---

# Test Case: View KPI Dashboard

**Name**: testViewKPIDashboardWithData

**Precondition**: Actor is logged into the system.
                  Actor has the role "Account Manager" or "Sales Manager"
                  Systsem has KPI data available

**Scenario**:

1. Actor navigates to the KPI dashboard section.
2. System displays the KPI dashboard interface.
3. System successfully retrieves business metrics.
4. System displays: Revenue, Most Booked Destinations, Total Kilometers Traveled
5. Actor sees and reviews the data.


**Result**:

- KPI dashboard loads successfully.
- All metrics are visible and accurate.
- Actor gains insights into business performance. 


---


**Name**: testViewKPIDashboardNoData

**Precondition**: Actor is logged into the system. 
                  Actor has the role "Account Manager" or "Sales Manager".
                  No booking data is available.

**Scenario**:

1. Actor navigates to the KPI dashboard section.
2. System attempts to retrieve KPI data.
3. No KPI data is found.
4. Systems displays message: „No KPI data available“


**Result**:

- Dashboard loads with no business metrics.
- Actor is clearly informed that no data is available.


---


**Name**: testViewKPIDashboardDataRetrievalError

**Precondition**: Actor is logged into the system. 
                  Actor has the role "Account Manager" or "Sales Manager".
                  System encounters a data retrieval error.

**Scenario**:

1. Actor navigates to the KPI dashboard section.
2. System attempts to retrieve KPI data.
3. System encounters a data retrieval error.
4. System displays error message: „Error retrieving KPI data. Please try again later“.


**Result**:

- Dashboard fails to display KPI data.
- Actor is informed of the issue and prompted to try again later.


  ---

# Test Case: View Tickets

**Name**: testGetTicketData

**Precondition**: Actor is logged into the system and is viewing a booking 

**Scenario**:

1. Actor selects the view ticket option
2. System shows ticket data

**Result**:

- Actor sees ticket data
- Ticket data shown is of requested booking

**Extension**: N/A

---

**Name**: testGetTicketDataWithNoTickets

**Precondition**: Actor is logged into the system and is viewing a booking without a booking ID
**Scenario**:

1. Actor selects the view ticket option
2. System shows no ticket data

**Result**:

- Actor sees message stating that no tickets were found
- No ticket data shown

**Extension**: N/A

---

# Test Case: View Bookings

**Name**: testDisplayBookingListSuccessfully

**Precondition**: The system contains active bookings stored in the database, and the Sales Employee is logged in.

**Scenario**:

1. Sales Employee navigates to the "Bookings" page.
2. The system fetches the list of active bookings from the backend.
3. Each booking contains flight information and at least one customer.
4. The system transforms and displays each booking in a card format.
5. Sales Employee clicks on a booking to open a detailed modal view.

**Result**:

- All bookings are fetched and displayed correctly.
- Each card shows the booking ID, flight info, passenger count, and total price.
- Clicking a card opens a modal with flight timeline and customer details.

---

**Name**: testSearchBookingById

**Precondition**: Bookings with valid IDs exist in the system.

**Scenario**:

1. Sales Employee types a valid booking ID into the search input.
2. The system filters the list in real-time based on the input.

**Result**:

- The correct booking appears in the filtered results.
- If no booking matches, the system shows “No bookings found.”

---

**Name**: testOpenBookingModal

**Precondition**: At least one booking is visible in the list.

**Scenario**:

1. Sales Employee clicks on a booking entry.
2. The system opens a modal displaying detailed booking data, including flight timeline, passengers, and pricing.

**Result**:

- The modal opens and displays accurate booking data.
- The UI remains responsive, and no crashes occur.

---

**Name**: testSoftDeleteBooking

**Precondition**: A booking is available and visible in the list.

**Scenario**:

1. Sales Employee opens the modal of a booking.
2. Sales Employee clicks the “Delete Booking” button.
3. The system sends a soft delete request to the backend.
4. The booking is removed from the active view.

**Result**:

- The booking disappears from the list of active bookings.
- A snackbar appears with the message: “Booking deleted.”

---

**Name**: testUndoBookingDelete

**Precondition**: A booking has just been soft deleted.

**Scenario**:

1. Within 7 seconds of deletion, Sales Employee clicks the “Undo” button on the snackbar.
2. The system restores the booking to the active list.

**Result**:

- The booking reappears in the active list.
- No booking data is lost or corrupted.
