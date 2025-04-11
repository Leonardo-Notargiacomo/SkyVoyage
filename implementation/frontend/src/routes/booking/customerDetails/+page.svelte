<script>
  import { goto } from "$app/navigation";
  import { bookingStore } from "$lib/stores/bookingStore";
  import { get } from "svelte/store";

  const booking = get(bookingStore);
  let flight = booking.flight;
  let passengers = booking.passengers || 1;

  // Redirect if no flight selected
  if (!flight) {
    goto("/SearchFlights");
  }

  let customers = Array.from({ length: passengers }, () => ({
    firstName: "",
    lastName: "",
    email: "",
    phone: "",
  }));

  function continueToSummary() {
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
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" />
        </svg>
        {showDebugPanel ? 'Hide' : 'Show'} Debug Data
      </button>
      
      {#if showDebugPanel}
        <div class="mt-4 p-4 bg-gray-900 text-green-400 rounded-md overflow-auto font-mono text-xs" style="max-height: 500px;">
          <div class="mb-4 pb-2 border-b border-gray-700">
            <h3 class="text-white font-bold">🛠️ DEBUG MODE - BOOKING STORE DATA</h3>
            <p class="text-gray-400 text-xs mt-1">Available variables for development</p>
          </div>
          
          <div class="mb-4">
            <h4 class="text-purple-400 font-bold">Flight Object Structure:</h4>
            <ul class="ml-4 text-gray-300">
              <li>flight.id: <span class="text-yellow-300">{flight?.id || 'undefined'}</span></li>
              <li>flight.airline: <span class="text-yellow-300">{flight?.airline || 'undefined'}</span></li>
              <li>flight.price: <span class="text-yellow-300">{flight?.price || 'undefined'}</span></li>
              <li>flight.currency: <span class="text-yellow-300">{flight?.currency || 'undefined'}</span></li>
              <li>flight.duration: <span class="text-yellow-300">{flight?.duration || 'undefined'}</span></li>
              <li>flight.status: <span class="text-yellow-300">{flight?.status || 'undefined'}</span></li>
              <li>flight.departure: Object (iata, airport, terminal, gate, scheduled)</li>
              <li>flight.arrival: Object (iata, airport, terminal, gate, scheduled)</li>
              <li>flight.fullOffer: Full flight offer data from Amadeus</li>
            </ul>
          </div>
          
          <div class="mb-4">
            <h4 class="text-purple-400 font-bold">Booking Store Variables:</h4>
            <ul class="ml-4 text-gray-300">
              <li>AdultPassengers: <span class="text-yellow-300">{fullBookingData?.AdultPassengers || 'undefined'}</span></li>
              <li>infantsPassengers: <span class="text-yellow-300">{fullBookingData?.infantsPassengers || 'undefined'}</span></li>
              <li>travelClass: <span class="text-yellow-300">{fullBookingData?.travelClass || 'undefined'}</span></li>
              <li>customers: Array of customer objects (will be populated after form submission)</li>
            </ul>
          </div>
          
          <div>
            <h4 class="text-purple-400 font-bold">Raw Booking Store Data:</h4>
            <pre class="text-green-300 mt-2 p-2 bg-gray-800 rounded overflow-auto" style="max-height: 300px;">{JSON.stringify(fullBookingData, null, 2)}</pre>
          </div>
          
          {#if flight?.fullOffer}
            <div class="mt-4">
              <h4 class="text-purple-400 font-bold">Full Amadeus Flight Offer:</h4>
              <pre class="text-green-300 mt-2 p-2 bg-gray-800 rounded overflow-auto" style="max-height: 300px;">{JSON.stringify(flight.fullOffer, null, 2)}</pre>
            </div>
          {/if}
        </div>
      {/if}
    </div>
    <!-- End Debug panel -->

  <!-- Flight Summary Card -->
  {#if flight}
    <div class="bg-white border border-gray-200 rounded-xl shadow-sm mb-8 p-6">
      <h2 class="text-xl font-semibold text-blue-700 mb-4">Flight Overview</h2>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6 text-sm">
        <div>
          <p class="text-gray-500">Airline</p>
          <p class="font-medium">{flight.airline}</p>
          <p class="text-gray-500 mt-2">Flight Number</p>
          <p class="font-medium">{flight.id}</p>
        </div>
        <div>
          <p class="text-gray-500">Price</p>
          <p class="font-medium text-lg text-blue-800">€{flight.price}</p>
          <p class="text-gray-500 mt-2">Status</p>
          <p class="font-medium capitalize">{flight.status}</p>
        </div>
        <div>
          <p class="text-gray-500">From</p>
          <p class="font-medium">
            {flight.departure.airport} ({flight.departure.iata})
          </p>
          <p class="text-sm">
            Terminal: {flight.departure.terminal !== "null"
              ? flight.departure.terminal
              : "TBA"}, Gate: {flight.departure.gate !== "null"
              ? flight.departure.gate
              : "TBA"}
          </p>
          <p class="text-sm">
            Departure: {formatDateTime(flight.departure.scheduled)}
          </p>
        </div>
        <div>
          <p class="text-gray-500">To</p>
          <p class="font-medium">
            {flight.arrival.airport} ({flight.arrival.iata})
          </p>
          <p class="text-sm">
            Terminal: {flight.arrival.terminal !== "null"
              ? flight.arrival.terminal
              : "TBA"}, Gate: {flight.arrival.gate !== "null"
              ? flight.arrival.gate
              : "TBA"}
          </p>
          <p class="text-sm">
            Arrival: {formatDateTime(flight.arrival.scheduled)}
          </p>
        </div>
      </div>
      <div class="mt-4 text-sm">
        <p class="text-gray-500">Duration</p>
        <p class="font-medium">{formatDuration(parseInt(flight.duration))}</p>
      </div>
    </div>
  {/if}

  <!-- Passenger Form -->
  <h1 class="text-2xl font-semibold mb-6 text-gray-800">
    Enter Passenger Details
  </h1>

  <form on:submit|preventDefault={continueToSummary} class="space-y-8">
    {#each customers as customer, index}
      <fieldset class="border p-4 rounded-md border-gray-300">
        <legend class="text-lg font-medium text-blue-600 mb-2">
          Passenger {index + 1}
        </legend>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">
              First Name
            </label>
            <input
              type="text"
              bind:value={customer.firstName}
              required
              class="w-full border p-2 rounded-md border-gray-300"
              placeholder="John"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">
              Last Name
            </label>
            <input
              type="text"
              bind:value={customer.lastName}
              required
              class="w-full border p-2 rounded-md border-gray-300"
              placeholder="Doe"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">
              Email
            </label>
            <input
              type="email"
              bind:value={customer.email}
              required
              class="w-full border p-2 rounded-md border-gray-300"
              placeholder="john@example.com"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">
              Phone (optional)
            </label>
            <input
              type="tel"
              bind:value={customer.phone}
              class="w-full border p-2 rounded-md border-gray-300"
              placeholder="+49 123 4567890"
            />
          </div>
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
