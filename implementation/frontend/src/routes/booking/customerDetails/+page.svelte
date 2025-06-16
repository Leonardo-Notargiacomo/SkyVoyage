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

<style>
  .card-hover {
    transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  }
  
  .card-hover:hover {
    transform: translateY(-5px);
    box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
  }
  
  .header-gradient {
    background: linear-gradient(135deg, #1e40af 0%, #3b82f6 100%);
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
  }
  
  @keyframes fadeIn {
    from { opacity: 0; transform: translateY(10px); }
    to { opacity: 1; transform: translateY(0); }
  }
  
  .animate-fade-in {
    animation: fadeIn 0.3s ease-out forwards;
  }
  
  .passenger-card {
    position: relative;
    overflow: hidden;
  }
  
  .passenger-card:hover {
    border-color: #93c5fd;
  }
  

  
  .passenger-card:hover::before {
    transform: scaleX(1);
  }

  .form-input {
    transition: border-color 0.3s ease, box-shadow 0.3s ease;
  }
  
  .form-input:focus {
    border-color: #3b82f6;
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
  }
</style>

<!-- Add breadcrumb navigation -->
<nav class="flex my-4 px-4 md:px-6 max-w-7xl mx-auto" aria-label="Breadcrumb">
  <ol class="inline-flex items-center space-x-2">
    <li class="inline-flex items-center">
      <a href="/home" class="inline-flex items-center text-sm font-medium text-gray-600 hover:text-blue-500 transition-colors">
        <svg class="w-4 h-4 me-2" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="currentColor" viewBox="0 0 20 20">
          <path d="m19.707 9.293-2-2-7-7a1 1 0 0 0-1.414 0l-7 7-2 2a1 1 0 0 0 1.414 1.414L2 10.414V18a2 2 0 0 0 2 2h3a1 1 0 0 0 1-1v-4a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1v4a1 1 0 0 0 1 1h3a2 2 0 0 0 2-2v-7.586l.293.293a1 1 0 0 0 1.414-1.414Z" />
        </svg>
        Dashboard
      </a>
    </li>
    <li>
      <div class="flex items-center">
        <svg class="w-4 h-4 text-gray-400 mx-1" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 6 10">
          <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="m1 9 4-4-4-4" />
        </svg>
        <a href="/SearchFlights" class="ms-1 text-sm font-medium text-gray-600 hover:text-blue-500 transition-colors">Flight Search</a>
      </div>
    </li>
    <li>
      <div class="flex items-center">
        <svg class="w-4 h-4 text-gray-400 mx-1" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 6 10">
          <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="m1 9 4-4-4-4" />
        </svg>
        <span class="ms-1 text-sm font-medium text-gray-400">Passenger Details</span>
      </div>
    </li>
  </ol>
</nav>

<div class="max-w-7xl mx-auto px-4 py-4 md:px-6">
  <div class="mb-8 bg-gradient-to-r from-blue-50 to-indigo-100 rounded-lg shadow-sm border border-blue-200 overflow-hidden p-6">
    <h1 class="text-3xl font-bold header-gradient flex items-center">
      <div class="bg-blue-600 p-2 rounded-lg shadow-md mr-3">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
        </svg>
      </div>
      Passenger Information
    </h1>
    <p class="text-blue-700 ml-14">Enter details for all passengers traveling</p>
  </div>

  <!-- Flight Summary Card -->
  <div class="mb-8 bg-white rounded-xl shadow-md border border-gray-100 overflow-hidden card-hover">
    <div class="bg-blue-50 p-4 border-b border-blue-100">
      <h2 class="text-lg font-medium text-blue-800 flex items-center">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
        </svg>
        Flight Information
      </h2>
    </div>
    <div class="p-6">
      <!-- Flight summary details -->
      <div class="flex flex-wrap justify-between items-center">
        <div class="mb-4 md:mb-0">
          <p class="text-sm text-gray-500">From</p>
          <p class="text-xl font-semibold text-gray-800">
            {booking.flight.departure.airport} ({booking.flight.departure.iata})
          </p>
          <p class="text-sm text-gray-600">
            {formatDateTime(booking.flight.departure.scheduled)}
          </p>
        </div>

        <div class="flex-1 px-4 max-w-xs mx-auto">
          <div class="text-center">
            <div class="text-xs text-gray-500 mb-1">{formatDuration(booking.flight.duration)}</div>
            <div class="w-full h-0.5 bg-gray-300 relative">
              <div class="absolute -left-1 -top-1.5 w-3 h-3 bg-blue-600 rounded-full"></div>
              <div class="absolute -right-1 -top-1.5 w-3 h-3 bg-green-600 rounded-full"></div>
              <div class="absolute left-1/2 top-1/2 transform -translate-x-1/2 -translate-y-1/2">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-blue-600 -mt-2" viewBox="0 0 20 20" fill="currentColor">
                  <path d="M10.894 2.553a1 1 0 00-1.788 0l-7 14a1 1 0 001.169 1.409l5-1.429A1 1 0 009 15.571V11.43a1 1 0 00-1-1H4.429a1 1 0 00-.429.086l-.5.143a1 1 0 01-1.366-1.086l1.5-7A1 1 0 015 2h10a1 1 0 01.934.649l1.5 7a1 1 0 01-1.366 1.086l-.5-.143a1 1 0 00-.429-.086H11a1 1 0 00-1 1v4.143a1 1 0 00.725.962l5 1.428a1 1 0 001.17-1.408l-7-14z" />
                </svg>
              </div>
            </div>
            <div class="text-xs text-blue-600 mt-2 font-medium">{booking.flight.airline}</div>
          </div>
        </div>

        <div class="text-right">
          <p class="text-sm text-gray-500">To</p>
          <p class="text-xl font-semibold text-gray-800">
            {booking.flight.arrival.airport} ({booking.flight.arrival.iata})
          </p>
          <p class="text-sm text-gray-600">
            {formatDateTime(booking.flight.arrival.scheduled)}
          </p>
        </div>
      </div>

      <div class="mt-6 pt-4 border-t border-dashed border-gray-200 flex items-center justify-between">
        <div class="bg-blue-50 px-4 py-2 rounded-lg border border-blue-100 flex items-center">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-blue-500 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
          </svg>
          <span class="font-medium">
            {booking.AdultPassengers} Adult{booking.AdultPassengers > 1 ? 's' : ''}
            {booking.infantsPassengers > 0 ? `, ${booking.infantsPassengers} Infant${booking.infantsPassengers > 1 ? 's' : ''}` : ''}
          </span>
        </div>

        <div class="bg-green-50 px-4 py-2 rounded-lg border border-green-100">
          <p class="font-semibold text-green-700">€{booking.flight.price}</p>
        </div>
      </div>
    </div>
  </div>

  <!-- Passenger Form -->
  <div class="bg-white rounded-xl shadow-md border border-gray-100 overflow-hidden mb-8">
    <div class="bg-gradient-to-r from-blue-600 to-blue-800 p-4 text-white">
      <h2 class="text-lg font-medium flex items-center">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
        </svg>
        Enter Passenger Details
      </h2>
    </div>

    <div class="p-6">
      <!-- Display validation errors if any -->
      {#if validationError}
        <div class="mb-6 p-4 bg-red-50 border-l-4 border-red-500 text-red-700 rounded-lg flex items-center animate-fade-in shadow-md">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-3 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
          {validationError}
        </div>
      {/if}

      <form on:submit|preventDefault={continueToSummary} class="space-y-6">
        {#each customers as customer, index}
          <fieldset class="passenger-card border p-5 rounded-xl border-gray-200 bg-white shadow-sm hover:shadow-md transition-all duration-300">
            <legend class="px-2 font-semibold text-blue-700 bg-white rounded-md flex items-center space-x-2">
              <div class="bg-gradient-to-r from-blue-500 to-blue-700 text-white w-8 h-8 rounded-full flex items-center justify-center font-bold shadow-md">
                {index + 1}
              </div>
              <span class="ml-2">
                Passenger {index + 1}
              </span>
              
              {#if index === 0}
                <span class="ml-2 text-xs bg-blue-100 text-blue-800 px-2 py-1 rounded-full border border-blue-200">Lead Passenger</span>
              {/if}
              
              {#if customer.isInfant}
                <span class="ml-2 text-xs bg-yellow-100 text-yellow-800 px-2 py-1 rounded-full border border-yellow-200">Infant</span>
              {/if}
            </legend>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-5 mt-2">
              <div>
                <label
                  for="firstName-{index}"
                  class="block text-sm font-medium text-gray-700 mb-1.5 flex items-center"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-blue-500 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                  First Name
                </label>
                <input
                  id="firstName-{index}"
                  type="text"
                  bind:value={customer.firstName}
                  required
                  class="w-full border p-3 rounded-lg border-gray-300 focus:ring-blue-500 focus:border-blue-500 form-input"
                  placeholder="John"
                />
              </div>

              <div>
                <label
                  for="lastName-{index}"
                  class="block text-sm font-medium text-gray-700 mb-1.5 flex items-center"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-blue-500 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                  Last Name
                </label>
                <input
                  id="lastName-{index}"
                  type="text"
                  bind:value={customer.lastName}
                  required
                  class="w-full border p-3 rounded-lg border-gray-300 focus:ring-blue-500 focus:border-blue-500 form-input"
                  placeholder="Doe"
                />
              </div>

              {#if !customer.isInfant}
                <div>
                  <label
                    for="email-{index}"
                    class="block text-sm font-medium text-gray-700 mb-1.5 flex items-center"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-blue-500 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                    </svg>
                    Email
                  </label>
                  <input
                    id="email-{index}"
                    type="email"
                    bind:value={customer.email}
                    required
                    on:blur={() => checkEmailExists(customer.email, index)}
                    class="w-full border p-3 rounded-lg border-gray-300 focus:ring-blue-500 focus:border-blue-500 form-input {emailErrors[index] ? 'border-red-400 bg-red-50' : ''}"
                    placeholder="john@example.com"
                  />
                  {#if emailErrors[index]}
                    <p class="mt-1.5 text-sm text-red-600 flex items-center">
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                      </svg>
                      {emailErrors[index]}
                    </p>
                  {/if}
                </div>

                <div>
                  <label
                    for="phone-{index}"
                    class="block text-sm font-medium text-gray-700 mb-1.5 flex items-center"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-blue-500 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                    </svg>
                    Phone
                  </label>
                  <input
                    id="phone-{index}"
                    type="tel"
                    bind:value={customer.phone}
                    class="w-full border p-3 rounded-lg border-gray-300 focus:ring-blue-500 focus:border-blue-500 form-input"
                    placeholder="+31 123 456 789"
                  />
                </div>
              {/if}

              <div class="md:col-span-2">
                <h4 class="font-medium text-gray-700 mb-3 flex items-center">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-blue-500 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                  </svg>
                  Address Information
                </h4>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-5 p-4 bg-gray-50 rounded-lg">
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
                      class="w-full border p-3 rounded-lg border-gray-300 focus:ring-blue-500 focus:border-blue-500 form-input"
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
                      class="w-full border p-3 rounded-lg border-gray-300 focus:ring-blue-500 focus:border-blue-500 form-input"
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
                      class="w-full border p-3 rounded-lg border-gray-300 focus:ring-blue-500 focus:border-blue-500 form-input"
                      placeholder="Amsterdam"
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
                      class="w-full border p-3 rounded-lg border-gray-300 focus:ring-blue-500 focus:border-blue-500 form-input"
                      placeholder="Netherlands"
                    />
                  </div>
                </div>
              </div>
            </div>

            {#if existingCustomers[index]}
              <div class="mt-4 p-4 bg-yellow-50 border border-yellow-200 rounded-lg flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-yellow-600 mr-2 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <div>
                  <p class="text-sm text-yellow-700">
                    This customer is already in our system. We'll use their existing details for this booking.
                  </p>
                </div>
              </div>
            {/if}
          </fieldset>
        {/each}

        <div class="flex items-center justify-between pt-6 border-t border-gray-100">
          <a
            href="/SearchFlights"
            class="inline-flex items-center border border-gray-300 bg-white hover:bg-gray-50 text-gray-700 font-medium py-2.5 px-5 rounded-lg shadow-sm transition-all duration-300 transform hover:-translate-x-1"
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
            class="bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-700 hover:to-blue-800 text-white font-medium py-2.5 px-6 rounded-lg shadow-md transition-all duration-300 transform hover:-translate-y-1 hover:shadow-lg flex items-center disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:transform-none"
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
                class="h-5 w-5 ml-2"
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
