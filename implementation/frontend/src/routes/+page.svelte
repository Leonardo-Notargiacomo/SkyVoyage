<script>
  import { onMount } from "svelte";
  import { slide } from "svelte/transition";
  import { api } from "$lib/api";
  import { goto } from "$app/navigation";

  let departure = "";
  let arrival = "";
  let departureDate = "";
  let returnDate = "";
  let stops = "Any";
  let passengers = 1;
  let flights = [];
  let showAdvancedOptions = false;
  let loading = false;
  let error = null;

  let selectedFlight = null;

  onMount(async () => {
    // Load flights from API
    loading = true;
    try {
      flights = await api.all("flights");
      error = null;
    } catch (err) {
      console.error("Failed to fetch flights:", err);
      error = "Failed to load flights. Please try again later.";
      flights = [];
    } finally {
      loading = false;
    }
  });

  const searchFlights = async (e) => {
     e.preventDefault();
     loading = true;
     error = null;

    goto(`/SearchFlights`);
    return;

    //  // Build query parameters based on form inputs
    //  const params = new URLSearchParams();

    //  if (departure) params.append("departure", departure);
    //  if (arrival) params.append("arrival", arrival);
    //  if (departureDate) params.append("departure_date", departureDate);
    //  if (returnDate) params.append("return_date", returnDate);
    //  if (stops !== "Any") params.append("stops", stops);
    //  if (passengers) params.append("passengers", passengers);

    //  const queryString = params.toString();
    //  const endpoint = queryString ? `flights?${queryString}` : "flights";

    //  try {
    //    flights = await api.all(endpoint);
    //    if (flights.length === 0) {
    //      error =
    //        "No flights found matching your search criteria. Please try different parameters.";
    //    } else {
    //      // Redirect to search results page with the query parameters
    //      goto(`/SearchFlights?${queryString}`);
    //      return; // Stop execution after redirection
    //    }
    //  } catch (err) {
    //    console.error("Failed to search flights:", err);
    //    error =
    //      "An error occurred while searching for flights. Please try again later.";
    //    flights = [];
    //  } finally {
    //    loading = false;
    //  }
  };

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

<!-- Search Form -->
<div class="max-w-6xl mx-auto p-6">
  <div class="bg-white p-6 shadow-md rounded-lg w-full">
    <h2 class="text-xl font-semibold mb-4">Search for Flights</h2>
    <form on:submit={searchFlights} class="space-y-4">
      <!-- Departure/Arrival airports unchanged -->
      <div class="flex flex-col md:flex-row gap-4">
        <!-- Departure Airport input -->
        <div class="flex-1">
          <label
            for="departure-airport"
            class="block text-sm font-medium text-gray-700 mb-1"
            >Departure Airport</label
          >
          <input
            id="departure-airport"
            type="text"
            bind:value={departure}
            placeholder="e.g. FRA"
            class="w-full p-2 border border-gray-300 rounded-md"
          />
        </div>
        <!-- Arrival Airport input -->
        <div class="flex-1">
          <label
            for="arrival-airport"
            class="block text-sm font-medium text-gray-700 mb-1"
            >Arrival Airport</label
          >
          <input
            id="arrival-airport"
            type="text"
            bind:value={arrival}
            placeholder="e.g. JFK"
            class="w-full p-2 border border-gray-300 rounded-md"
          />
        </div>
      </div>

      <!-- Departure/Return dates unchanged -->
      <div class="flex flex-col md:flex-row gap-4 items-start">
        <div class="flex-1">
          <label
            for="departure-date"
            class="block text-sm font-medium text-gray-700 mb-1"
            >Departure Date</label
          >
          <input
            id="departure-date"
            type="date"
            bind:value={departureDate}
            class="w-full p-2 border border-gray-300 rounded-md"
          />
          <label class="inline-flex items-center mt-2 text-sm text-gray-600">
            <input type="checkbox" class="mr-2" /> Flexible (+/- 2 days)
          </label>
        </div>
        <div class="flex-1">
          <label
            for="return-date"
            class="block text-sm font-medium text-gray-700 mb-1"
            >Return Date (optional)</label
          >
          <input
            id="return-date"
            type="date"
            bind:value={returnDate}
            class="w-full p-2 border border-gray-300 rounded-md"
          />
          <label class="inline-flex items-center mt-2 text-sm text-gray-600">
            <input type="checkbox" class="mr-2" /> Flexible (+/- 2 days)
          </label>
        </div>
      </div>

      <!-- Advanced Options toggle -->
      <div>
        <button
          type="button"
          class="flex items-center text-blue-600 hover:text-blue-800 text-sm"
          on:click={() => (showAdvancedOptions = !showAdvancedOptions)}
        >
          <span
            >{showAdvancedOptions ? "▲ Hide" : "▼ Show"} advanced options</span
          >
        </button>
      </div>

      <!-- Advanced options section -->
      {#if showAdvancedOptions}
        <div transition:slide={{ duration: 300 }}>
          <div
            class="flex flex-col md:flex-row gap-4 items-start pt-2 border-t border-gray-200"
          >
            <div class="flex-1">
              <label
                for="flight-type"
                class="block text-sm font-medium text-gray-700 mb-1"
                >Flight Type</label
              >
              <div id="flight-type" class="flex flex-col gap-1 text-sm">
                <label class="flex items-center gap-2">
                  <input
                    type="radio"
                    name="stops"
                    value="Any"
                    bind:group={stops}
                    checked
                  />
                  Any number of stops
                </label>
                <label class="flex items-center gap-2">
                  <input
                    type="radio"
                    name="stops"
                    value="Direct"
                    bind:group={stops}
                  />
                  Only direct flights
                </label>
                <label class="flex items-center gap-2">
                  <input
                    type="radio"
                    name="stops"
                    value="MaxOneStop"
                    bind:group={stops}
                  />
                  Max. one stop
                </label>
              </div>
            </div>

            <div class="flex-1">
              <label
                for="passengers"
                class="block text-sm font-medium text-gray-700 mb-1"
                >Passengers</label
              >
              <select
                id="passengers"
                bind:value={passengers}
                class="w-full p-2 border border-gray-300 rounded-md"
              >
                {#each Array(9) as _, i}
                  <option value={i + 1}
                    >{i + 1} Passenger{i > 0 ? "s" : ""}</option
                  >
                {/each}
              </select>
            </div>
          </div>
        </div>
      {/if}

      <!-- Search button -->
      <div class="pt-2">
        <button
          type="submit"
          class="w-full bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-5 rounded-lg shadow-sm transition-all duration-200 transform hover:-translate-y-0.5"
        >
          Search Flights
        </button>
      </div>
    </form>
  </div>

  <!-- Loading and Error States -->
  {#if loading}
    <div class="mt-6 flex justify-center">
      <div
        class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"
      ></div>
    </div>
  {:else if error}
    <div
      class="mt-6 p-4 bg-red-50 border border-red-200 text-red-700 rounded-md"
    >
      {error}
    </div>
  {:else if flights.length === 0}
    <div
      class="mt-6 p-4 bg-blue-50 border border-blue-200 text-blue-700 rounded-md"
    >
      No flights available. Please try a different search.
    </div>
  {/if}

  <!-- Flight Cards -->
  <div class="mt-6 grid grid-cols-1 md:grid-cols-2 gap-4">
    {#each flights as flight}
      <button
        class="bg-white border border-blue-100 shadow-sm rounded-lg p-4 cursor-pointer w-full text-left transition-all duration-200 transform hover:-translate-y-0.5 hover:shadow-lg"
        on:click={() => (selectedFlight = flight)}
        on:keydown={(e) => e.key === "Enter" && (selectedFlight = flight)}
        aria-label={`Select flight from ${flight.departure.iata} to ${flight.arrival.iata}`}
      >
        <h3 class="text-blue-600 text-sm font-semibold">
          {flight.departure.airport} → {flight.arrival.airport}
        </h3>
        <p class="text-gray-800 text-sm">
          {formatDateTime(flight.departure.scheduled)} – {formatDateTime(
            flight.arrival.scheduled
          )}
        </p>
        <p class="text-gray-600 text-sm">
          Duration: {formatDuration(parseInt(flight.duration))}
        </p>
        <p class="text-gray-600 text-sm font-semibold mt-1">
          Price: €{flight.price}
        </p>
      </button>
    {/each}
  </div>
</div>

<!-- Modal -->
{#if selectedFlight}
  <div class="fixed inset-0 flex items-center justify-center z-50">
    <button
      class="absolute inset-0 bg-black/40 backdrop-blur-sm"
      aria-label="Close modal"
      on:click={() => (selectedFlight = null)}
    ></button>
    <div class="bg-white p-6 rounded-lg z-50 shadow-xl max-w-md w-full">
      <div class="flex justify-between items-center mb-2">
        <h3 class="text-blue-600 font-bold text-lg">
          {selectedFlight.departure.airport} → {selectedFlight.arrival.airport}
        </h3>
        <button
          on:click={() => (selectedFlight = null)}
          class="text-gray-500 hover:text-black">&times;</button
        >
      </div>
      <div class="grid grid-cols-2 gap-3 mt-4">
        <div>
          <p class="text-sm text-gray-500">Airline</p>
          <p class="font-medium">{selectedFlight.airline}</p>
        </div>
        <div>
          <p class="text-sm text-gray-500">Flight</p>
          <p class="font-medium">{selectedFlight.id}</p>
        </div>
        <div>
          <p class="text-sm text-gray-500">From</p>
          <p class="font-medium">
            {selectedFlight.departure.airport} ({selectedFlight.departure.iata})
          </p>
          <p class="text-sm">
            Terminal: {selectedFlight.departure.terminal !== "null"
              ? selectedFlight.departure.terminal
              : "TBA"}, Gate: {selectedFlight.departure.gate !== "null"
              ? selectedFlight.departure.gate
              : "TBA"}
          </p>
          <p class="text-sm">
            Departure: {formatDateTime(selectedFlight.departure.scheduled)}
          </p>
          <p class="text-sm text-orange-600">
            {selectedFlight.departure.delay > 0
              ? `Delay: ${selectedFlight.departure.delay} min`
              : "On time"}
          </p>
        </div>
        <div>
          <p class="text-sm text-gray-500">To</p>
          <p class="font-medium">
            {selectedFlight.arrival.airport} ({selectedFlight.arrival.iata})
          </p>
          <p class="text-sm">
            Terminal: {selectedFlight.arrival.terminal !== "null"
              ? selectedFlight.arrival.terminal
              : "TBA"}, Gate: {selectedFlight.arrival.gate !== "null"
              ? selectedFlight.arrival.gate
              : "TBA"}
          </p>
          <p class="text-sm">
            Arrival: {formatDateTime(selectedFlight.arrival.scheduled)}
          </p>
          <p class="text-sm text-orange-600">
            {selectedFlight.arrival.delay > 0
              ? `Delay: ${selectedFlight.arrival.delay} min`
              : "On time"}
          </p>
        </div>
      </div>
      <div class="mt-4 border-t pt-4">
        <div class="flex justify-between items-center">
          <div>
            <p class="text-sm text-gray-500">Duration</p>
            <p class="font-medium">
              {formatDuration(parseInt(selectedFlight.duration))}
            </p>
          </div>
          <div>
            <p class="text-sm text-gray-500">Status</p>
            <p class="font-medium capitalize">{selectedFlight.status}</p>
          </div>
          <div>
            <p class="text-sm text-gray-500">Price</p>
            <p class="font-medium text-lg">€{selectedFlight.price}</p>
          </div>
        </div>
      </div>
      <div class="mt-4 pt-2">
        <button
          class="w-full bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded"
        >
          Select This Flight
        </button>
      </div>
    </div>
  </div>
{/if}
