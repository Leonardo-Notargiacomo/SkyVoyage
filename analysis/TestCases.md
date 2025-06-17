# Test Cases

This document outlines the test cases for the project. Each test case describes a specific scenario from a user's perspective while matching the underlying test implementation.

---

# Test Case: Employee Account Security

**Name**: testPasswordIsHashed

**Precondition**: A Account Manager is logged into the employee management system.

**Scenario**:

1. Account Manager navigates to the "Add New Employee" screen.
2. Account Manager fills in the employee form with:
   - First Name: "John"
   - Last Name: "Doe"
   - Email: "john.doe@example.com"
   - Password: "SecurePassword123!"
   - Role: "Sales Manager"
3. Account Manager clicks the "Create Employee" button.
4. System processes the request and creates the employee account.

**Result**:

- Account Manager sees a success message: "Employee John Doe created successfully."
- The password is securely stored in the database.
- If the Account Manager were to view the database directly, they would see the password is stored as a BCrypt hash (starting with "$2a$", "$2b$", or "$2y$").
- The employee can log in with their original password despite it being stored in a hashed format.

---

# Test Case: Employee Management

**Name**: testGetAllEmployees

**Precondition**: A Account Manager is logged into the employee management system.

**Scenario**:

1. Account Manager navigates to the "Employees" page.
2. System retrieves the employee list from the database.
3. System displays the list of employees on the interface.

**Result**:

- Account Manager sees a complete list of employees in the system.
- The HTTP status code in the browser's network tab shows 200 (OK).

---

**Name**: testCreateEmployee

**Precondition**: A Account Manager is logged into the employee management system.

**Scenario**:

1. Account Manager navigates to the "Add New Employee" screen.
2. Account Manager fills in all required fields:
   - First Name: "John"
   - Last Name: "Smith"
   - Email: "john.smith@company.com"
   - Password: "Johndoe1230@"
   - Role: "Sales Manager"
3. Account Manager clicks the "Create Employee" button.

**Result**:

- Account Manager sees a success message: "Employee created successfully."
- The new employee appears in the employee list.
- The HTTP status code in the browser's network tab shows 201 (Created).

---

**Name**: testCreateEmployeeNull

**Precondition**: A Account Manager is logged into the employee management system.

**Scenario**:

1. Account Manager navigates to the "Add New Employee" screen.
2. Account Manager clicks the "Create Employee" button without entering any information.

**Result**:

- Account Manager sees an error message: "Employee data cannot be empty."
- No employee is created in the system.
- The form remains on the screen for the Account Manager to complete properly.
- The HTTP status code in the browser's network tab shows 400 (Bad Request).

---

**Name**: testCreateEmployeeInvalidInformation

**Precondition**: A Account Manager is logged into the employee management system.

**Scenario**:

1. Account Manager navigates to the "Add New Employee" screen.
2. Account Manager fills in the employee form with:
   - First Name: "John"
   - Last Name: "Smith"
   - Email: "john.smith" (missing domain)
   - Password: "john12345"
   - Role: "Sales Manager"
3. Account Manager clicks the "Create Employee" button.

**Result**:

- Account Manager sees an error message: "Invalid email: john.smith"
- The email field is highlighted in red.
- No employee is created in the system.
- The HTTP status code in the browser's network tab shows 400 (Bad Request).

---

**Name**: testCreateEmployeeMultipleValidationErrors

**Precondition**: A Account Manager is logged into the employee management system.

**Scenario**:

1. Account Manager navigates to the "Add New Employee" screen.
2. Account Manager fills in the employee form with multiple invalid entries:
   - First Name: "J" (too short)
   - Last Name: "" (empty)
   - Email: "invalid-email" (invalid format)
   - Password: "weak" (too weak)
   - Role: "" (empty)
3. Account Manager clicks the "Create Employee" button.

**Result**:

- Account Manager sees multiple validation errors:
  - "First name must be at least 2 characters"
  - "Last name is required"
  - "Invalid email format"
  - "Password must meet complexity requirements"
  - "Role is required"
- All invalid fields are highlighted in red.
- No employee is created in the system.
- The HTTP status code in the browser's network tab shows 400 (Bad Request).

---

**Name**: testGetEmployee

**Precondition**: A Account Manager is logged into the employee management system with existing employees.

**Scenario**:

1. Account Manager navigates to the "Employees" page.
2. Account Manager clicks on the "Manage" button next to employee "John Smith" (ID: 1).
3. System retrieves the employee's information from the database.

**Result**:

- Account Manager sees a detailed profile page for John Smith.
- The page displays all employee information including name, email, and role.
- The HTTP status code in the browser's network tab shows 200 (OK).

---

**Name**: testDeleteEmployee

**Precondition**: A Account Manager is logged into the employee management system with existing employees.

**Scenario**:

1. Account Manager navigates to the "Employees" page.
2. Account Manager finds employee "John Smith" (ID: 1) in the list.
3. Account Manager clicks the "Delete" button next to the employee's name.
4. System displays a confirmation dialog: "Are you sure you want to delete this employee account?"
5. Account Manager clicks "Confirm".

**Result**:

- Account Manager sees a success message: "Employee account deleted successfully."
- John Smith no longer appears in the employee list.
- The HTTP status code in the browser's network tab shows 204 (No Content).

---

**Name**: testGetAll

**Precondition**: A Account Manager is logged into the employee management system with multiple employees.

**Scenario**:

1. Account Manager navigates to the "Employees" page.
2. System retrieves all employee records from the database.
3. System displays the list of employees including:
   - John Smith (Sales Manager)
   - Jane Doe (HR Manager)

**Result**:

- Account Manager sees both employees listed in the employee page.
- The page shows each employee's name, email, and role.
- The HTTP status code in the browser's network tab shows 200 (OK).

---

# Test Case: Discount Management 

**Name**: Add Invalid Discount

**Precondition**: A sales manager is logged into the system and navigates to the discount management screen.

**Scenario**:

1. Sales manager clicks on "Create New Discount".
2. Sales manager fills in the discount form with:
   - Name: "Invalid Discount"
   - Amount: 110% (invalid percentage)
   - Type: "Regular"
   - Days before departure: 10
3. Sales manager clicks the "Create Discount" button.

**Result**:

- System displays an error message: "Invalid discount amount. Percentage must be between 0 and 100."
- The discount is not created in the system.
- The amount field is highlighted in red.
- The form remains on the screen for correction.

---

**Name**: Calculate Price with No Applicable Discount

**Precondition**: A sales employee is creating a booking that is 50 days before departure.

**Scenario**:

1. Sales employee searches for and selects a flight.
2. System displays the flight details with a base price of €100.
3. System checks for applicable discounts:
   - "Early Bird" discount of 15% (applies 30 days before departure)
   - "Super Early" discount of 25% (applies 20 days before departure)
4. Since the booking is 50 days before departure, no discount is applicable.

**Result**:

- The booking summary shows the original price: €100.
- No discount is applied to the booking.
- The sales employee proceeds with the booking at the full price.

---

**Name**: Calculate Price with No Discounts in System

**Precondition**: A sales employee is creating a booking, but no discount rules exist in the system.

**Scenario**:

1. Sales employee searches for and selects a flight.
2. System displays the flight details with a base price of €100.
3. System checks for applicable discounts but finds none in the database.

**Result**:

- The booking summary shows the original price: €100.
- No discount section is displayed in the booking summary.
- The sales employee proceeds with the booking at the full price.

---

**Name**: Calculate Price with Zero Percent Discount

**Precondition**: A sales employee is creating a booking with a 0% discount rule in place.

**Scenario**:

1. Sales employee searches for and selects a flight for a date 15 days in the future.
2. System displays the flight details with a base price of €100.
3. System checks for applicable discounts and finds:
   - "No Discount" promotion with 0% applicable for bookings 30 days before departure.

**Result**:

- The booking summary shows the original price: €100.
- Even though the discount rule applies, no actual discount is calculated (0%).
- The sales employee proceeds with the booking at the full price.

---

**Name**: Calculate Price with Single Applicable Discount

**Precondition**: A sales employee is creating a booking 25 days before departure.

**Scenario**:

1. Sales employee searches for and selects a flight.
2. System displays the flight details with a base price of €100.
3. System checks for applicable discounts:
   - "Early Bird" discount of 15% (applies 30 days before departure)
   - "Super Early" discount of 25% (applies 20 days before departure)
4. Only the "Early Bird" discount is applicable since 25 days is within the 30-day window.

**Result**:

- The booking summary shows:
  - Original price: €100
  - "Early Bird" discount: -€15
  - Final price: €85
- The sales employee sees the discount applied and proceeds with the booking.

---

**Name**: Calculate Price with Multiple Applicable Discounts

**Precondition**: A sales employee is creating a booking 15 days before departure.

**Scenario**:

1. Sales employee searches for and selects a flight.
2. System displays the flight details with a base price of €100.
3. System checks for applicable discounts:
   - "Early Bird" discount of 15% (applies 30 days before departure)
   - "Super Early" discount of 25% (applies 20 days before departure)
   - "Last Minute" discount of 10% (applies 10 days before departure)
4. Both "Early Bird" and "Super Early" discounts are applicable.

**Result**:

- The booking summary shows:
  - Original price: €100
  - "Super Early" discount: -€25 (highest applicable discount)
  - Final price: €75
- The system applies only the highest discount (25%) rather than combining them.
- The sales employee sees the best discount applied and proceeds with the booking.

---

**Name**: Calculate Price with Last-Minute Discount

**Precondition**: A sales employee is creating a booking just 5 days before departure.

**Scenario**:

1. Sales employee searches for and selects a flight.
2. System displays the flight details with a base price of €100.
3. System checks for applicable discounts:
   - "Early Bird" discount of 15% (applies 30 days before departure)
   - "Super Early" discount of 25% (applies 20 days before departure)
   - "Last Minute" discount of 35% (applies 7 days before departure)
4. All three discounts are applicable since 5 days is within all time windows.

**Result**:

- The booking summary shows:
  - Original price: €100
  - "Last Minute" discount: -€35 (highest applicable discount)
  - Final price: €65
- The system applies the highest discount (35%).
- The sales employee sees the best discount applied and proceeds with the booking.

---

**Name**: View All Available Discounts

**Precondition**: A sales manager is logged into the system.

**Scenario**:

1. Sales manager navigates to the "Discount Management" screen.
2. System retrieves all discount rules from the database:
   - Discount 1: 10% discount, type "type1", 5 days before departure
   - Discount 2: 20% discount, type "type2", 10 days before departure
3. System displays the list of discount rules.

**Result**:

- Sales manager sees a complete list of all discount rules in the system.
- Each discount shows its name, percentage, type, and applicable time window.
- The sales manager can edit or delete existing discounts from this screen.

---
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

**Name**: testAddDiscountThrowsNullPointerExceptionForNullDiscount

**Precondition**: The system has a discount management module with validation rules in place.

**Scenario**:

1. A null discount object is passed to the discount manager.
2. The system attempts to add this discount through the discount manager.
3. The system validates the discount data before adding it to the repository.

**Result**:

- System throws a NullPointerException due to the null discount object.
- The discount is not added to the repository.
- The repository's add method is never called.

---

**Name**: testAddDiscountThrowsIllegalArgumentExceptionForZeroAmount

**Precondition**: The system has a discount management module with validation rules in place.

**Scenario**:

1. A discount object is created with zero amount:
   - ID: 7
   - Name: "Zero Amount"
   - Amount: 0.0 (zero percent)
   - Type: "regular"
   - EmployeeID: 6
   - Days: 10
2. The system attempts to add this discount through the discount manager.
3. The system validates the discount data before adding it to the repository.

**Result**:

- System throws an IllegalArgumentException with message "Discount amount must be greater than 0%".
- The discount is not added to the repository.
- The repository's add method is never called.

---

**Name**: testAddDiscountThrowsIllegalArgumentExceptionForNegativeAmount

**Precondition**: The system has a discount management module with validation rules in place.

**Scenario**:

1. A discount object is created with negative amount:
   - ID: 8
   - Name: "Negative Amount"
   - Amount: -10.0 (negative percent)
   - Type: "regular"
   - EmployeeID: 6
   - Days: 10
2. The system attempts to add this discount through the discount manager.
3. The system validates the discount data before adding it to the repository.

**Result**:

- System throws an IllegalArgumentException with message "Discount amount must be greater than 0%".
- The discount is not added to the repository.
- The repository's add method is never called.

---

**Name**: testAddDiscountThrowsIllegalArgumentExceptionForZeroDays

**Precondition**: The system has a discount management module with validation rules in place.

**Scenario**:

1. A discount object is created with zero days:
   - ID: 9
   - Name: "Zero Days"
   - Amount: 15.0
   - Type: "regular"
   - EmployeeID: 6
   - Days: 0 (zero days)
2. The system attempts to add this discount through the discount manager.
3. The system validates the discount data before adding it to the repository.

**Result**:

- System throws an IllegalArgumentException with message "Days until departure must be greater than 0".
- The discount is not added to the repository.
- The repository's add method is never called.

---

**Name**: testAddDiscountThrowsIllegalArgumentExceptionForNegativeDays

**Precondition**: The system has a discount management module with validation rules in place.

**Scenario**:

1. A discount object is created with negative days:
   - ID: 10
   - Name: "Negative Days"
   - Amount: 15.0
   - Type: "regular"
   - EmployeeID: 6
   - Days: -5 (negative days)
2. The system attempts to add this discount through the discount manager.
3. The system validates the discount data before adding it to the repository.

**Result**:

- System throws an IllegalArgumentException with message "Days until departure must be greater than 0".
- The discount is not added to the repository.
- The repository's add method is never called.

---

**Name**: testDeleteDiscount

**Precondition**: The system has a discount management module with a delete functionality.

**Scenario**:

1. A discount ID (1) is provided to the discount manager for deletion.
2. The system attempts to delete the discount through the discount manager.

**Result**:

- The discount manager calls the repository's delete method with the provided discount ID.
- The discount is successfully deleted from the repository.

---

**Name**: testCalculateDiscountedPriceThrowsNullPointerExceptionForNullDepartureDate

**Precondition**: The system has a discount management module with validation rules in place.

**Scenario**:

1. A base price of 100.0 is provided.
2. A null departure date is specified.
3. The system attempts to calculate the discounted price.

**Result**:

- System throws a NullPointerException with message "Departure date cannot be null".
- The discount calculation is not performed.

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
