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
