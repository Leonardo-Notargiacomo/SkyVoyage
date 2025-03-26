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

**Description**: The sales employee views the list of available flights after entering a search.

**Precondition**: - The sales employee is logged into the system.
                  - The sales employee has made a flight search with valid criteria.

**Scenario**:
1. The system processes the search request and retrieves available flights.
2. The system displays a list of flights that match the search criteria.
3. Each flight result includes details(airline, departure, arrival, duration, price...)
4. The sales employee can sort the flight results (e.g., by price, duration, airline).
5. The sales employee selects a flight to view more details.
6. The system displays the flight details page with additional information.

**Result**: The sales employee successfully views the available flights and can proceed with booking or other actions.

**Exception**:
- **2a.** If no flights match the search criteria, the system displays a message: "No flights found. Try modifying your search criteria."
- **5a.** If the system encounters an error retrieving flight data, it displays an error message: "An error occurred while retrieving flights. Please try again later."

**Extensions**:
- **6a** The sales employee can compare multiple flights before selecting one.
- **6b** The sales employee can save a flight for later or share details with a customer.

---

# Use Case: Search Flights 

**Actor**: Sales Employee

**Description**: A sales employee searches for available flights based on specified criteria.

**Precondition**:  The sales employee is logged into the system.

**Scenario**:
1. The sales employee selects the flight search section.
2. The system displays the flight search interface.
3. The sales employee enters search criteria (e.g., departure city, date, number of passengers...).
4. The sales employee submits the search request.
5. The system processes the request and retrieves available flights.
6. The system displays a list of available flights matching the search criteria.

**Result**: The sales employee successfully views a list of available flights.

**Exception**:
- **4a.** If no flights match the criteria, the system displays a message no results found and suggests alternative options.
- **5a.** If the system encounters an error retrieving flights, an error message is displayed.

**Extensions**:
- **5b** The sales employee can apply additional filters (e.g., price, airline, layovers).
1. The sales employee selects filter options.
2. The system updates the flight list based on the selected filters.

---
