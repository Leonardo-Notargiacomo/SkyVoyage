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

  // Debug variables
  let showDebugPanel = false;
  let fullBookingData = get(bookingStore);

  function toggleDebugPanel() {
    showDebugPanel = !showDebugPanel;
  }
</script>

<div class="max-w-4xl mx-auto p-6">
  <!-- Debug Panel -->
  <div class="mt-10 border-b-2 mb-4 border-gray-200 pb-4">
    <button
      on:click={toggleDebugPanel}
      class="flex items-center text-sm text-gray-500 hover:text-gray-700 font-mono"
    >
      <svg
        xmlns="http://www.w3.org/2000/svg"
        class="h-5 w-5 mr-1"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4"
        />
      </svg>
      {showDebugPanel ? "Hide" : "Show"} Debug Data
    </button>

    {#if showDebugPanel}
      <div
        class="mt-4 p-4 bg-gray-900 text-green-400 rounded-md overflow-auto font-mono text-xs"
        style="max-height: 500px;"
      >
        <div class="mb-4 pb-2 border-b border-gray-700">
          <h3 class="text-white font-bold">
            🛠️ DEBUG MODE - BOOKING STORE DATA
          </h3>
          <p class="text-gray-400 text-xs mt-1">
            Available variables for development
          </p>
        </div>

        <div class="mb-4">
          <h4 class="text-purple-400 font-bold">Flight Object Structure:</h4>
          <ul class="ml-4 text-gray-300">
            <li>
              flight.id: <span class="text-yellow-300"
                >{flight?.id || "undefined"}</span
              >
            </li>
            <li>
              flight.airline: <span class="text-yellow-300"
                >{flight?.airline || "undefined"}</span
              >
            </li>
            <li>
              flight.price: <span class="text-yellow-300"
                >{flight?.price || "undefined"}</span
              >
            </li>
            <li>
              flight.currency: <span class="text-yellow-300"
                >{flight?.currency || "undefined"}</span
              >
            </li>
            <li>
              flight.duration: <span class="text-yellow-300"
                >{flight?.duration || "undefined"}</span
              >
            </li>
            <li>
              flight.status: <span class="text-yellow-300"
                >{flight?.status || "undefined"}</span
              >
            </li>
            <li>
              flight.departure: Object (iata, airport, terminal, gate,
              scheduled)
            </li>
            <li>
              flight.arrival: Object (iata, airport, terminal, gate, scheduled)
            </li>
            <li>flight.fullOffer: Full flight offer data from Amadeus</li>
          </ul>
        </div>

        <div class="mb-4">
          <h4 class="text-purple-400 font-bold">Booking Store Variables:</h4>
          <ul class="ml-4 text-gray-300">
            <li>
              AdultPassengers: <span class="text-yellow-300"
                >{fullBookingData?.AdultPassengers || "undefined"}</span
              >
            </li>
            <li>
              infantsPassengers: <span class="text-yellow-300"
                >{fullBookingData?.infantsPassengers || "undefined"}</span
              >
            </li>
            <li>
              travelClass: <span class="text-yellow-300"
                >{fullBookingData?.travelClass || "undefined"}</span
              >
            </li>
            <li>
              customers: Array of customer objects (will be populated after form
              submission)
            </li>
          </ul>
        </div>

        <div>
          <h4 class="text-purple-400 font-bold">Raw Booking Store Data:</h4>
          <pre
            class="text-green-300 mt-2 p-2 bg-gray-800 rounded overflow-auto"
            style="max-height: 300px;">{JSON.stringify(
              fullBookingData,
              null,
              2,
            )}</pre>
        </div>

        {#if flight?.fullOffer}
          <div class="mt-4">
            <h4 class="text-purple-400 font-bold">
              Full Amadeus Flight Offer:
            </h4>
            <pre
              class="text-green-300 mt-2 p-2 bg-gray-800 rounded overflow-auto"
              style="max-height: 300px;">{JSON.stringify(
                flight.fullOffer,
                null,
                2,
              )}</pre>
          </div>
        {/if}
      </div>
    {/if}
  </div>
  <!-- End Debug panel -->

  <!-- Passenger Form -->
  <h1 class="text-2xl font-semibold mb-6 text-gray-800">
    Enter Passenger Details
  </h1>

  <form on:submit|preventDefault={continueToSummary} class="space-y-8">
    {#each customers as customer, index}
      <fieldset class="border p-4 rounded-md border-gray-300">
        <legend class="text-lg font-medium text-blue-600 mb-2">
          Passenger {index + 1}
          {#if index === 0}
            (Lead Passenger)
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
              class="w-full border p-2 rounded-md border-gray-300"
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
              class="w-full border p-2 rounded-md border-gray-300"
              placeholder="Doe"
            />
          </div>

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
              class="w-full border p-2 rounded-md border-gray-300"
              placeholder="john@example.com"
            />
          </div>

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
              class="w-full border p-2 rounded-md border-gray-300"
              placeholder="+31 123 4567890"
            />
          </div>

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
              class="w-full border p-2 rounded-md border-gray-300"
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
              class="w-full border p-2 rounded-md border-gray-300"
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
              class="w-full border p-2 rounded-md border-gray-300"
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
              class="w-full border p-2 rounded-md border-gray-300"
              placeholder="Netherlands"
            />
          </div>

          {#if index !== 0}
            <div class="col-span-2">
              <label
                for="isInfant-{index}"
                class="inline-flex items-center text-sm font-medium text-gray-700 mb-1"
              >
                <input
                  id="isInfant-{index}"
                  type="checkbox"
                  bind:checked={customer.isInfant}
                  class="h-4 w-4 text-blue-600 border-gray-300 rounded"
                />
                Does not need a seat (Infant)
              </label>
            </div>
          {/if}
        </div>
      </fieldset>
    {/each}

    <div class="pt-4">
      <button
        type="submit"
        class="w-full bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-5 rounded-lg shadow-sm transition-all duration-200 transform hover:-translate-y-0.5"
      >
        Continue to Booking Summary
      </button>
    </div>
  </form>
</div>
