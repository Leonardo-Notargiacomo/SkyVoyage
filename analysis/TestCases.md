# Test Cases

This document outlines the test cases for the project. Each test case describes a specific scenario to be tested, including the steps to reproduce, expected results, and actual results.

---

# Test Case: Create Account

**Name**: testCreateAccount

**Precondition**: Actor is logged into the system and is on the employee management interface.

**Scenario**:
1. Actor selects the option to create a new account.
2. System prompts the Actor to enter the employee's details (name, email, role).
3. Actor enters the required information. 
4. System validates the information. 
5. Actor submits the form. 
6. System creates the new account for the employee. 
7. Test checks if the account was created successfully. 
8. Actor receives a confirmation message.

**Result**:
- Actor successfully creates a new account for the employee.

---

**Extension**: N/A

**Name**: testCreateAccountInvalidEmail

**Precondition**: Actor is logged into the system and is on the employee management interface.

**Scenario**:
1. Actor selects the option to create a new account.
2. System prompts the Actor to enter the employee's details (name, email, role).
3. Actor enters an email that is already in use.
4. Test checks if the system displays an error message and prompts the Actor to enter a different email.

**Result**:
- System displays an error message and prompts the Actor to enter a different email.

**Extension**: N/A

---