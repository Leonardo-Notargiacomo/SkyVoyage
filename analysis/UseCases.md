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
2. System shows flight page & a couple availabe flights.

**Result**: Actor successfully views the available flights and can proceed with booking or other actions.

**Exception**:
- **2a.** If no flights are found, the system displays a message: "No flights found."

**Extensions**:
- n/a

---

# Use Case: Search Flights 

**Actor**: Sales Employee

**Description**: Actor searches for available flights based on specified criteria.

**Precondition**: Actor is logged into the system. 
                  Actor is on the search flight page

**Scenario**:
1. Actor enters the flight search section.
2. System displays the flight search interface.
3. Actor enters search criteria (e.g., departure city, date, number of passengers...).
4. Actor submits the search request.
5. System processes the request and retrieves available flights.
6. System displays a list of available flights matching the search criteria.

**Result**: Actor successfully views a list of available flights.

**Exception**:
- **4a.** If no flights match the criteria, the system displays a message no results found.
- **5a.** If the system encounters an error retrieving flights, an error message is displayed.

**Extensions**:
- **5b** Actor can apply additional filters (e.g., price, airline..).
1. Actor selects filter options.
2. System updates the flight list based on the selected filters.

---
