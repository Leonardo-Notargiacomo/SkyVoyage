<script>
  import { goto } from "$app/navigation";
  import { bookingStore } from "$lib/stores/bookingStore";
  import { get } from "svelte/store";

  const booking = get(bookingStore);
  let flight = booking.flight;
  let passengers = booking.AdultPassengers + booking.infantsPassengers || 1;

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

  function continueToSummary() {
    // Save customers to sessionStorage
    sessionStorage.setItem("customers", JSON.stringify(customers));

    bookingStore.update((state) => ({
      ...state,
      customers: customers,
    }));
    goto("/booking/overview");
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
        href="/home"
        class="inline-flex items-center text-sm font-medium text-gray-600 hover:text-blue-500 transition-colors"
      >
        <svg
          class="w-4 h-4 me-2"
          aria-hidden="true"
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 20 20"
          fill="currentColor"
        >
          <path
            d="m19.707 9.293-2-2-7-7a1 1 0 0 0-1.414 0l-7 7-2 2a1 1 0 0 0 1.414 1.414L2 10.414V18a2 2 0 0 0 2 2h3a1 1 0 0 0 1-1v-4a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1v4a1 1 0 0 0 1 1h3a2 2 0 0 0 2-2v-7.586l.293.293a1 1 0 0 0 1.414-1.414Z"
          />
        </svg>
        Dashboard
      </a>
    </li>
    <li>
      <div class="flex items-center">
        <svg
          class="w-4 h-4 text-gray-400 mx-1"
          aria-hidden="true"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 6 10"
        >
          <path
            stroke="currentColor"
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="m1 9 4-4-4-4"
          />
        </svg>
        <a href="/SearchFlights" class="ms-1 text-sm font-medium text-gray-600 hover:text-blue-500">Search Flights</a>
      </div>
    </li>
    <li>
      <div class="flex items-center">
        <svg
          class="w-4 h-4 text-gray-400 mx-1"
          aria-hidden="true"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 6 10"
        >
          <path
            stroke="currentColor"
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="m1 9 4-4-4-4"
          />
        </svg>
        <span class="ms-1 text-sm font-medium text-gray-400">Passenger Details</span>
      </div>
    </li>
  </ol>
</nav>

<div class="max-w-6xl mx-auto p-6">
  <!-- Flight Summary Card -->
  <div class="mb-6 bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
    <div class="bg-blue-50 p-4 border-b border-gray-200">
      <h2 class="text-lg font-medium text-blue-800">Flight Summary</h2>
    </div>
    <div class="p-4">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div>
          <span class="text-sm text-gray-500">Flight</span>
          <p class="font-medium">{flight.airline}</p>
          <p class="text-sm text-gray-600">
            {flight.departure.airport} ({flight.departure.iata}) → {flight.arrival.airport} ({flight.arrival.iata})
          </p>
        </div>
        <div>
          <span class="text-sm text-gray-500">Departure</span>
          <p class="font-medium">{formatDateTime(flight.departure.scheduled)}</p>
          {#if flight.departure.terminal}
            <p class="text-sm text-gray-600">Terminal {flight.departure.terminal}</p>
          {/if}
        </div>
        <div>
          <span class="text-sm text-gray-500">Duration</span>
          <p class="font-medium">{formatDuration(flight.duration)}</p>
          <p class="text-sm text-gray-600">
            {booking.AdultPassengers} Adult{booking.AdultPassengers !== 1 ? 's' : ''}
            {#if booking.infantsPassengers > 0}
              , {booking.infantsPassengers} Infant{booking.infantsPassengers !== 1 ? 's' : ''}
            {/if}
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
      <form on:submit|preventDefault={continueToSummary} class="space-y-6">
        {#each customers as customer, index}
          <fieldset class="border p-4 rounded-md border-gray-300 bg-white shadow-sm">
            <legend class="px-2 text-lg font-medium text-blue-600">
              Passenger {index + 1}
              {#if index === 0}
                <span class="text-sm bg-blue-100 text-blue-800 px-2 py-0.5 rounded-full">Lead Passenger</span>
              {/if}
              {#if index >= booking.AdultPassengers}
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

              {#if index < booking.AdultPassengers}
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
                    required={index === 0}
                    class="w-full border p-2 rounded-md border-gray-300 focus:ring-blue-500 focus:border-blue-500"
                    placeholder="john@example.com"
                  />
                </div>
              {/if}

              {#if index < booking.AdultPassengers}
                <div>
                  <label
                    for="phone-{index}"
                    class="block text-sm font-medium text-gray-700 mb-1"
                  >
                    Phone Number
                  </label>
                  <input
                    id="phone-{index}"
                    type="tel"
                    bind:value={customer.phone}
                    required={index === 0}
                    class="w-full border p-2 rounded-md border-gray-300 focus:ring-blue-500 focus:border-blue-500"
                    placeholder="+31 123 4567890"
                  />
                </div>
              {/if}

              <div>
                <label
                  for="street-{index}"
                  class="block text-sm font-medium text-gray-700 mb-1"
                >
                  Street Name
                </label>
                <input
                  id="street-{index}"
                  type="text"
                  bind:value={customer.street}
                  required={index === 0}
                  class="w-full border p-2 rounded-md border-gray-300 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Borelstraat"
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
                  required={index === 0}
                  class="w-full border p-2 rounded-md border-gray-300 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="41"
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
                  required={index === 0}
                  class="w-full border p-2 rounded-md border-gray-300 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Heerlen"
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
                  required={index === 0}
                  class="w-full border p-2 rounded-md border-gray-300 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Netherlands"
                />
              </div>

              {#if index >= booking.AdultPassengers}
                {customer.isInfant = true}
              {/if}
            </div>
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
            class="bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-5 rounded-lg shadow-sm transition-all duration-200 flex items-center"
          >
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
          </button>
        </div>
      </form>
    </div>
  </div>
</div>
