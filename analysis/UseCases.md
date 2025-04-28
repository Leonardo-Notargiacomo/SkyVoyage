# Use Cases

This document outlines the use cases for the project. Each use case describes a specific interaction between a user and the system to achieve a goal.

---

# Use Case: Create Account

**Actor**: Account Manager

**Description**: Account manager creates a new account for an employee.

**Precondition**: Account manager is logged into the system.

**Scenario**:
1. Actor selects the employee management section.
2. System displays the employee management interface.
3. Actor selects the option to create a new account.
4. System prompts the actor to enter the employee's details (name, email, role).
5. Actor enters the required information.
6. System validates the information.

**Result**: Account manager successfully creates a new account for the employee.

**Exception**:
- **4a.** If the entered email is already in use, the system displays an error message and prompts the actor to enter a different email.
- **6a.** If the information is invalid, the system displays an error message and prompts the actor to correct the information.
- **6b.** If the system encounters an unexpected error, the system displays an error message.

**Extensions**:
- **5a** Account manager can assign an additional role to the employee. 
1. Actor selects the role section.
2. System displays the available roles.
3. Actor selects the desired role.
4. Return to step 5.

---

# Use Case: Display Flights 

**Actor**: Sales Employee

**Description**: Actor views the list of available flights.

**Precondition**: Actor is logged into the system.
            

**Scenario**:
1. Actor goes to flight page.
2. System shows flight page with all availabe flights.

**Result**: Actor successfully views the available flights and can proceed with booking or other actions.

**Exception**:
- **2a.** If no flights are found, the system displays a message: "No flights found."

**Extensions**:
- n/a

---

# Use Case: Search Flight 

**Actor**: Sales Employee

**Description**: Actor searches for available flights based on specified criteria.

**Precondition**: Actor is logged into the system. 
                  Actor is on the Home page

**Scenatio:**
1. Actor fills in the Search Flight form.
2. System searches available flights accortding to the users flight criteria.
3. System redirects user to a different page with the result flights.

**Result**: Actor successfully views a list of available flights.

**Exception**:
- **2a.** If no flights match the criteria, the system displays a message no results found.
- **3a.** If the system encounters an error retrieving flights, an error message is displayed.

**Extensions**:
- **2b** Actor can apply additional filters (e.g., price, airline..).
1. Actor selects filter options.
2. System updates the flight list based on the selected filters.

---

# Use Case: Set Price Per Kilometre

**Actor**: Sales Manager

**Description**: Sales Manager sets the price per kilometre of a flight

**Precondition**: Sales Manager is logged into the system.

**Scenario**:
1. Actor navigates to the sales manager section.
2. System displays the sales manager interface.
3. System prompts the actor to enter the new price per kilometre.
4. Actor enters the desired price.
5. System validates the entered price.
6. System updates the price per kilometre in the system.

**Result**: Sales Manager successfully sets the price Per Kilometre.

**Exception**:
- **4a.** If the entered price is invalid (e.g., negative or non-numeric), the system displays an error message and prompts the actor to enter a valid price.
- **6a.** If the system encounters an unexpected error, it displays an error message.

**Extensions**:
- N/A

---

# Use Case: Log into system

**Actor**: Sales Employee, Sales Manager, Account Manager

**Description**: The system authenticates actor with different roles and grants them access to the related application functions.

**Precondition**: The Actor has a valid account with an assigned role (e.g., Sales Employee, Account Manager, Sales Manager).

**Scenario**:
1. Actor is on the login page.
2. System displays the login interface.
3. Actor enters their credentials (email & password).
4. Actor submits the login request.
5. The system verifies the credentials:
   * If credentials are valid, proceed to step 6.
   * If credentials are invalid, display an error message.
6. System checks the actor's role and grants access based on role permissions.
7. The system redirects the Actor to the appropriate dashboard.

**Result**: The Actor successfully logs in and access functions according to their assigned role.

**Exception**:
- **5a.** Invalid Credentials: If the email or password is incorrect, the system displays an error message: "Invalid email or password. Please try again."


**Extensions**:
- N/A

---

# Use Case: Create Booking

**Actor**: Sales Employee

**Description**:  
The system allows the Sales Employee to create a booking by entering customer details, reviewing the selected flight, optionally applying a discount with a reason, and confirming, reserving, or canceling the booking.

**Precondition**:  
The Sales Employee is logged in and has selected a flight from the flight search results.

**Scenario**:

1. Sales Employee is on the customer details page.
2. System displays the selected flight overview and a form to enter passenger details.
3. Sales Employee enters customer information (first name, last name, email, and optionally phone).
4. Sales Employee proceeds to the booking summary page.
5. System displays booking summary including flight details, passenger details, and price calculation.
6. Sales Employee optionally enters a discount percentage and a reason for the discount.
7. Sales Employee chooses one of the following actions:
    - Confirm Booking → Saves the booking as confirmed.
    - Reserve (Pay Later) → Saves the booking as reserved (pending payment).
    - Cancel Booking → Cancels the booking process and returns to the home screen.

**Result**:  
The booking is created with accurate customer details and stored as confirmed or reserved, or the process is canceled.

**Exception**:

- 6a. Invalid Discount: If the discount entered is not a number between 0 and 100, the system ignores it.
- 7a. Missing Details: If customer data is incomplete, the system prevents booking confirmation.

**Extensions**:

- If the discount is 69%, the system enters "Dank Mode" with animations and gifs for entertainment.

---

# Use Case: Fill in customer information

**Actor**: Sales Employee

**Description**:
The actor fills in the passenger information for each passenger for the booking. 

**Precondition**:
The sales employee has selected a flight and is now on the page for filling out the customer information. 

**Scenario**:
1. Actor fills in all of the information fields of the first passenger.
2. If there are multiple passengers, actor fills in first name and last name of these passengers, and possibly one or more of the other fields.
3. Actor presses 'Continue to booking summary' button.
4. System shows the booking overview page.

**Result**:
The details of all passengers are visible in the overview, ready to be accepted. Actor can still see them, and continue with the making of the booking. 

**Exception**:
- 3a. Missing details: If not all fields of the first passenger are filled out, the system gives a message of a field missing data.
- 3b. Missing details: If the first name and/or the last name of any passenger that is not the first one is/are not filled out, the system gives a message of a field missing data.

---

# Use Case: Manage Discounts

**Actor**: Sales Manager

**Description**:
The actor manages early bird and last-minute discount offers.

**Precondition**:
Sales Manager is logged into the system.

**Scenario**:
1. Actor navigates to the sales manager section.
2. System displays the discount management interface showing current discount settings.
3. Actor selects the type of discount to configure (early bird or last-minute).
4. System displays the configuration form for the selected discount type:
5. Actor enters the discount parameters.
6. System validates the entered information.
7. Actor submits the new discounts.
8. System updates the discount settings and applies them to future bookings.

**Result**:
Sales Manager successfully changes discount offers that will be automatically applied to eligible bookings.

**Exception**:
- 5a. If the entered discount percentage is invalid (e.g., negative or over 100%), the system displays an error message and prompts the actor to enter a valid percentage.
- 5b. If the entered time parameters are invalid (e.g., negative days or exceeding limits), the system displays an error message.
- 8a. If the system encounters an unexpected error, it displays an error message.

**Extensions**:

- N/A


---

# Use Case: View KPI Dashboard

**Actor**: Sales Manager, Account Manager

**Description**:
The Actor accesses the KPI dashboard to view key business performance indicators, including revenue, most booked destinations, and total kilometers traveled.

**Precondition**:
Actor is logged into the system.
Actor must have the role "Account Manager" or "Sales Manager".
System has booking data available

**Scenario**:
1. Actor navigates to the KPI dashboard section.
2. System displays the KPI dashboard interface.
3. System retrieves and displays the following business metrics: Revenue, Most Booked Destinations, Total Kilometers Traveled.
4. Actor can see all business metrics.

**Result**:
Actor successfully views business performance indicators and gains insight into booking trends and revenue.

**Exception**:
- 3a. No Data Available: If there are no available bookings system displays a message: „No KPI data available“.
- 4a. Data Retrieval Error: If there is an issue retrieving KPI data, system display: „Error retrieving KPI data. Please try again later“.

**Extensions**:

- N/A

---
