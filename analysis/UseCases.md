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
