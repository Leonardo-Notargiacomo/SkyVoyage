<script>
  import { goto } from "$app/navigation";
  import { bookingStore } from "$lib/stores/bookingStore";
  import { get } from "svelte/store";
  import { api } from "$lib/api.js"; // Import the API helper

  const booking = get(bookingStore);
  let flight = booking.flight;
  let passengers = booking.AdultPassengers + booking.infantsPassengers || 1;
  
  // Add validation state variables
  let isValidating = false;
  let validationError = "";
  let emailErrors = Array(passengers).fill("");
  let existingCustomers = Array(passengers).fill(null);

  // Redirect if no flight selected
  if (!flight) {
    goto("/SearchFlights");
  }

  let customers = Array.from({ length: passengers }, () => ({
    firstName: "",
    lastName: "",
    email: "",
    phone: "",
    street: "",
    houseNumber: "",
    city: "",
    country: "",
    isInfant: false,
  }));

  // Set infant status for the last passengers if there are any
  if (booking.infantsPassengers > 0) {
    for (let i = 0; i < booking.infantsPassengers; i++) {
      const infantIndex = passengers - i - 1;
      if (infantIndex >= 0) {
        customers[infantIndex].isInfant = true;
      }
    }
  }

  // Function to check if an email already exists in the database
  async function checkEmailExists(email, index) {
    if (!email || email.trim() === "") {
      emailErrors[index] = ""; 
      return false; // No need to check empty emails
    }
    
    try {
      // Call the API to check if the email exists - use fetchAPI instead of get
      const response = await api.fetchAPI(`bookings/check-customer?email=${encodeURIComponent(email.trim())}`);
      
      // If the email exists, store the customer data
      if (response && response.exists) {
        console.log("Found existing customer:", response.customer);
        
        const existingCustomer = response.customer;
        existingCustomers[index] = existingCustomer;
        
        // Check if all fields match exactly
        const currentCustomer = customers[index];
        const exactMatch = compareCustomerData(currentCustomer, existingCustomer);
        
        if (exactMatch) {
          // Use the existing customer data
          customers[index] = { 
            ...existingCustomer,
            isInfant: currentCustomer.isInfant // Preserve the infant status from our form
          };
          emailErrors[index] = ""; 
          return false; // No error, use existing data
        } else {
          // Show error that the email is already in use
          emailErrors[index] = `A customer with email "${email}" already exists with different details`;
          return true; // Return error status
        }
      } else {
        existingCustomers[index] = null;
        emailErrors[index] = "";
        return false; // No error
      }
    } catch (error) {
      console.error("Error checking email:", error);
      emailErrors[index] = "Error validating email, please try again";
      return true; // Return error status
    }
  }
  
  // Compare customer data to see if fields match
  function compareCustomerData(formCustomer, dbCustomer) {
    // Skip check if any is null
    if (!formCustomer || !dbCustomer) return false;
    
    // Compare essential fields (case-insensitive for strings)
    const normalizeString = (str) => str && typeof str === 'string' ? str.trim().toLowerCase() : "";
    
    const sameFirstName = normalizeString(formCustomer.firstName) === normalizeString(dbCustomer.firstName);
    const sameLastName = normalizeString(formCustomer.lastName) === normalizeString(dbCustomer.lastName);
    const samePhone = !formCustomer.phone || !dbCustomer.phone || 
                      normalizeString(formCustomer.phone) === normalizeString(dbCustomer.phone);
    
    // Compare address if provided in form
    let sameAddress = true;
    if (formCustomer.street && formCustomer.houseNumber) {
      sameAddress = (
        normalizeString(formCustomer.street) === normalizeString(dbCustomer.street) &&
        normalizeString(formCustomer.houseNumber) === normalizeString(dbCustomer.houseNumber) &&
        normalizeString(formCustomer.city) === normalizeString(dbCustomer.city) &&
        normalizeString(formCustomer.country) === normalizeString(dbCustomer.country)
      );
    }
    
    return sameFirstName && sameLastName && samePhone && sameAddress;
  }

  async function continueToSummary() {
    // Reset validation state
    isValidating = true;
    validationError = "";
    let hasErrors = false;
    
    try {
      // Validate all adult customers' emails (only adults need emails)
      for (let i = 0; i < booking.AdultPassengers; i++) {
        if (customers[i].email) {
          const emailError = await checkEmailExists(customers[i].email, i);
          if (emailError) {
            hasErrors = true;
          }
        }
      }
      
      if (hasErrors) {
        validationError = "Please fix the validation errors before continuing";
        isValidating = false;
        return;
      }
      
      // All validations passed, save to bookingStore and continue
      bookingStore.update((state) => ({
        ...state,
        customers: customers,
      }));
      
      // Save to sessionStorage too
      sessionStorage.setItem("customers", JSON.stringify(customers));
      
      // Navigate to overview page
      goto("/booking/overview");
    } catch (error) {
      console.error("Validation error:", error);
      validationError = "An error occurred during validation. Please try again.";
    } finally {
      isValidating = false;
    }
  }

  function formatDateTime(dateString) {
    return new Date(dateString).toLocaleString("en-GB", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  }

  function formatDuration(minutes) {
    if (minutes < 60) return `${minutes}min`;
    const h = Math.floor(minutes / 60);
    const m = minutes % 60;
    return `${h}h ${m > 0 ? `${m}min` : ""}`;
  }
</script>

<!-- Add breadcrumb navigation to match SearchFlights page -->
<nav class="flex my-4" aria-label="Breadcrumb">
  <ol class="inline-flex items-center space-x-2">
    <li class="inline-flex items-center">
      <a
        class="inline-flex items-center text-sm font-medium text-gray-600 hover:text-blue-500 transition-colors"
      >
      </a>
    </li>
    <li>
      <div class="flex items-center">
      </div>
    </li>
    <li>
      <div class="flex items-center">
      </div>
    </li>
  </ol>
</nav>

<div class="max-w-6xl mx-auto p-6">
  <!-- Flight Summary Card -->
  <div class="mb-6 bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
    <div class="bg-blue-50 p-4 border-b border-gray-200">
      <h2 class="text-lg font-medium text-blue-800">Flight Information</h2>
    </div>
    <div class="p-4">
      <!-- Flight summary details -->
      <div class="flex flex-wrap justify-between">
        <div class="mb-4 md:mb-0">
          <p class="text-sm text-gray-500">From</p>
          <p class="text-xl font-semibold">
            {booking.flight.departure.airport} ({booking.flight.departure.iata})
          </p>
          <p class="text-sm text-gray-500">
            {formatDateTime(booking.flight.departure.scheduled)}
          </p>
        </div>

        <div class="flex items-center px-4">
          <div class="text-center">
            <div class="text-xs text-gray-500">{formatDuration(booking.flight.duration)}</div>
            <div class="w-32 h-0.5 bg-gray-300 my-1 relative">
              <div class="absolute -left-1 -top-1.5 w-3 h-3 bg-blue-600 rounded-full"></div>
              <div class="absolute -right-1 -top-1.5 w-3 h-3 bg-green-600 rounded-full"></div>
            </div>
            <div class="text-xs text-blue-600">{booking.flight.airline}</div>
          </div>
        </div>

        <div class="text-right">
          <p class="text-sm text-gray-500">To</p>
          <p class="text-xl font-semibold">
            {booking.flight.arrival.airport} ({booking.flight.arrival.iata})
          </p>
          <p class="text-sm text-gray-500">
            {formatDateTime(booking.flight.arrival.scheduled)}
          </p>
        </div>
      </div>
    </div>
  </div>

  <!-- Passenger Form -->
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden mb-6">
    <div class="bg-blue-50 p-4 border-b border-gray-200">
      <h1 class="text-lg font-medium text-blue-800">Enter Passenger Details</h1>
    </div>

    <div class="p-4">
      <!-- Display validation errors if any -->
      {#if validationError}
        <div class="mb-4 p-3 bg-red-100 text-red-800 border border-red-200 rounded-md">
          <p class="flex items-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
            {validationError}
          </p>
        </div>
      {/if}

      <form on:submit|preventDefault={continueToSummary} class="space-y-6">
        {#each customers as customer, index}
          <fieldset class="border p-4 rounded-md border-gray-300 bg-white shadow-sm">
            <legend class="px-2 text-lg font-medium text-blue-600">
              Passenger {index + 1}
              {#if index === 0}
                <span class="text-sm bg-blue-100 text-blue-800 px-2 py-0.5 rounded-full">Lead Passenger</span>
              {/if}
              {#if customer.isInfant}
                <span class="text-sm bg-yellow-100 text-yellow-800 px-2 py-0.5 rounded-full">Infant</span>
              {/if}
            </legend>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label
                  for="firstName-{index}"
                  class="block text-sm font-medium text-gray-700 mb-1"
                >
                  First Name
                </label>
                <input
                  id="firstName-{index}"
                  type="text"
                  bind:value={customer.firstName}
                  required
                  class="w-full border p-2 rounded-md border-gray-300 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="John"
                />
              </div>

              <div>
                <label
                  for="lastName-{index}"
                  class="block text-sm font-medium text-gray-700 mb-1"
                >
                  Last Name
                </label>
                <input
                  id="lastName-{index}"
                  type="text"
                  bind:value={customer.lastName}
                  required
                  class="w-full border p-2 rounded-md border-gray-300 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Doe"
                />
              </div>

              {#if !customer.isInfant}
                <div>
                  <label
                    for="email-{index}"
                    class="block text-sm font-medium text-gray-700 mb-1"
                  >
                    Email
                  </label>
                  <input
                    id="email-{index}"
                    type="email"
                    bind:value={customer.email}
                    required
                    on:blur={() => checkEmailExists(customer.email, index)}
                    class="w-full border p-2 rounded-md border-gray-300 focus:ring-blue-500 focus:border-blue-500 {emailErrors[index] ? 'border-red-500' : ''}"
                    placeholder="john@example.com"
                  />
                  {#if emailErrors[index]}
                    <p class="mt-1 text-sm text-red-600">{emailErrors[index]}</p>
                  {/if}
                </div>

                <div>
                  <label
                    for="phone-{index}"
                    class="block text-sm font-medium text-gray-700 mb-1"
                  >
                    Phone
                  </label>
                  <input
                    id="phone-{index}"
                    type="tel"
                    bind:value={customer.phone}
                    class="w-full border p-2 rounded-md border-gray-300 focus:ring-blue-500 focus:border-blue-500"
                    placeholder="+31 123 456 789"
                  />
                </div>
              {/if}

              <div>
                <label
                  for="street-{index}"
                  class="block text-sm font-medium text-gray-700 mb-1"
                >
                  Street
                </label>
                <input
                  id="street-{index}"
                  type="text"
                  bind:value={customer.street}
                  class="w-full border p-2 rounded-md border-gray-300 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Main Street"
                />
              </div>

              <div>
                <label
                  for="houseNumber-{index}"
                  class="block text-sm font-medium text-gray-700 mb-1"
                >
                  House Number
                </label>
                <input
                  id="houseNumber-{index}"
                  type="text"
                  bind:value={customer.houseNumber}
                  class="w-full border p-2 rounded-md border-gray-300 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="25A"
                />
              </div>

              <div>
                <label
                  for="city-{index}"
                  class="block text-sm font-medium text-gray-700 mb-1"
                >
                  City
                </label>
                <input
                  id="city-{index}"
                  type="text"
                  bind:value={customer.city}
                  class="w-full border p-2 rounded-md border-gray-300 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Erzurum"
                />
              </div>

              <div>
                <label
                  for="country-{index}"
                  class="block text-sm font-medium text-gray-700 mb-1"
                >
                  Country
                </label>
                <input
                  id="country-{index}"
                  type="text"
                  bind:value={customer.country}
                  class="w-full border p-2 rounded-md border-gray-300 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Turkiye"
                />
              </div>
            </div>

            {#if existingCustomers[index]}
              <div class="mt-3 p-2 bg-yellow-50 border border-yellow-300 rounded-md">
                <p class="text-sm text-yellow-700">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 inline mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  This customer is already in our system.
                </p>
              </div>
            {/if}
          </fieldset>
        {/each}

        <div class="flex items-center justify-between pt-4">
          <a
            href="/SearchFlights"
            class="inline-flex items-center text-gray-600 hover:text-gray-800 font-medium"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-5 w-5 mr-2"
              viewBox="0 0 20 20"
              fill="currentColor"
            >
              <path
                fill-rule="evenodd"
                d="M9.707 16.707a1 1 0 01-1.414 0l-6-6a1 1 0 010-1.414l6-6a1 1 0 011.414 1.414L5.414 9H17a1 1 0 110-2H5.414l4.293 4.293a1 1 0 010 1.414z"
                clip-rule="evenodd"
              />
            </svg>
            Back to Search
          </a>
          <button
            type="submit"
            disabled={isValidating || emailErrors.some(error => error !== "")}
            class="bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-5 rounded-lg shadow-sm transition-all duration-200 flex items-center disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {#if isValidating}
              <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              Validating...
            {:else}
              Continue to Booking Summary
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="h-5 w-5 ml-1"
                viewBox="0 0 20 20"
                fill="currentColor"
              >
                <path
                  fill-rule="evenodd"
                  d="M10.293 3.293a1 1 0 011.414 0l6 6a1 1 0 010 1.414l-6 6a1 1 0 01-1.414-1.414L14.586 11H3a1 1 0 110-2h11.586l-4.293-4.293a1 1 0 010-1.414z"
                  clip-rule="evenodd"
                />
              </svg>
            {/if}
          </button>
        </div>
      </form>
    </div>
  </div>
</div>
