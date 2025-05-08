<script>
  import { onMount } from "svelte";
  import { slide } from "svelte/transition";
  import { api } from "$lib/api";
  import { goto } from "$app/navigation";
  import { bookingStore } from "$lib/stores/bookingStore";
  import { get } from "svelte/store";

  let departure = "";
  let arrival = "";
  let departureDate = "";
  let returnDate = "";
  let stops = "Any";
  let passengers = 1;
  let travelClass = "ECONOMY";

  let adults = 1;
  let infants = 0;
  let currencyCode = "EUR";
  let maxResults = 5;
  let includedAirlineCodes = "";
  let excludedAirlineCodes = "";
  let maxPrice = "";

  let flights = [];
  let showAdvancedOptions = false;
  let loading = false;
  let error = null;

  let selectedFlight = null;

  onMount(async () => {
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

    const params = new URLSearchParams();

    if (departure) params.append("originLocationCode", departure);
    if (arrival) params.append("destinationLocationCode", arrival);
    if (departureDate) params.append("departureDate", departureDate);
    if (returnDate) params.append("returnDate", returnDate);

    // Passenger counts - only adults and infants
    params.append("adults", adults.toString());
    if (infants > 0) params.append("infants", infants.toString());

    params.append("travelClass", travelClass);

    // Convert stops selection to nonStop parameter
    if (stops === "Direct") {
      params.append("nonStop", "true");
    } else {
      params.append("nonStop", "false");
    }

    // Additional filters
    params.append("max", maxResults.toString());
    if (currencyCode) params.append("currencyCode", currencyCode);
    if (maxPrice) params.append("maxPrice", maxPrice);
    if (includedAirlineCodes)
      params.append("includedAirlineCodes", includedAirlineCodes);
    if (excludedAirlineCodes)
      params.append("excludedAirlineCodes", excludedAirlineCodes);

    const queryString = params.toString();
    goto(`/SearchFlights?${queryString}`);
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
    <h2 class="text-xl font-semibold mb-4 flex items-center">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3.055 11H5a2 2 0 012 2v1a2 2 0 002 2 2 2 0 012 2v2.945M8 3.935V5.5A2.5 2.5 0 0010.5 8h.5a2 2 0 012 2 2 2 0 104 0 2 2 0 012-2h1.064M15 20.488V18a2 2 0 012-2h3.064M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
      Search for Flights
    </h2>
    <form on:submit={searchFlights} class="space-y-4">
      <div class="flex flex-col md:flex-row gap-4">
        <div class="flex-1 relative">
          <label
            for="departure-airport"
            class="block text-sm font-medium text-gray-700 mb-1 flex items-center"
          >
            <div class="bg-blue-50 p-1 rounded-full mr-2">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z" />
              </svg>
            </div>
            Departure Airport
          </label>
          <div class="relative">
            <input
              id="departure-airport"
              type="text"
              bind:value={departure}
              placeholder="e.g. FRA"
              class="w-full p-2 pl-10 border border-gray-300 rounded-md"
              required
            />
            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 3l4 4L19 7" transform="rotate(-45 12 12)" />
              </svg>
            </div>
          </div>
        </div>
        
        <div class="flex-1 relative">
          <label
            for="arrival-airport"
            class="block text-sm font-medium text-gray-700 mb-1 flex items-center"
          >
            <div class="bg-green-50 p-1 rounded-full mr-2">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            </div>
            Arrival Airport
          </label>
          <div class="relative">
            <input
              id="arrival-airport"
              type="text"
              bind:value={arrival}
              placeholder="e.g. JFK"
              class="w-full p-2 pl-10 border border-gray-300 rounded-md"
              required
            />
            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21l-4-4L5 17" transform="rotate(-45 12 12)" />
              </svg>
            </div>
          </div>
        </div>
      </div>

      <div class="flex flex-col md:flex-row gap-4 items-start">
        <div class="flex-1">
          <label
            for="departure-date"
            class="block text-sm font-medium text-gray-700 mb-1 flex items-center"
          >
            <div class="bg-blue-50 p-1 rounded-full mr-2">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </div>
            Departure Date
          </label>
          <div class="relative">
            <input
              id="departure-date"
              type="date"
              bind:value={departureDate}
              class="w-full p-2 pl-10 border border-gray-300 rounded-md"
              required
            />
            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </div>
          </div>
        </div>
        
        <div class="flex-1">
          <label
            for="return-date"
            class="block text-sm font-medium text-gray-700 mb-1 flex items-center"
          >
            <div class="bg-red-50 p-1 rounded-full mr-2">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </div>
            Return Date (optional)
          </label>
          <div class="relative">
            <input
              id="return-date"
              type="date"
              bind:value={returnDate}
              class="w-full p-2 pl-10 border border-gray-300 rounded-md"
            />
            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </div>
          </div>
        </div>
      </div>

      <div>
        <button
          type="button"
          class="flex items-center text-blue-600 hover:text-blue-800 text-sm"
          on:click={() => (showAdvancedOptions = !showAdvancedOptions)}
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4" />
          </svg>
          <span>{showAdvancedOptions ? "▲ Hide" : "▼ Show"} advanced options</span>
        </button>
      </div>

      {#if showAdvancedOptions}
        <div transition:slide={{ duration: 300 }}>
          <div
            class="grid grid-cols-1 md:grid-cols-3 gap-4 items-start pt-2 border-t border-gray-200"
          >
            <div>
              <label
                for="flight-type"
                class="block text-sm font-medium text-gray-700 mb-1 flex items-center"
              >
                <div class="bg-purple-50 p-1 rounded-full mr-2">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-purple-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
                  </svg>
                </div>
                Flight Type
              </label>
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
              </div>
            </div>

            <div>
              <label for="passengers" class="block text-sm font-medium text-gray-700 mb-1 flex items-center">
                <div class="bg-yellow-50 p-1 rounded-full mr-2">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-yellow-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                  </svg>
                </div>
                Passengers
              </label>

              <div class="flex flex-col gap-2">
                <div class="flex items-center justify-between">
                  <span class="text-sm">Adults (12+ years)</span>
                  <div class="flex items-center">
                    <button
                      type="button"
                      class="w-8 h-8 rounded-full border border-gray-300 flex items-center justify-center"
                      on:click={() => (adults = Math.max(1, adults - 1))}
                      >-</button
                    >
                    <span class="mx-2 w-6 text-center">{adults}</span>
                    <button
                      type="button"
                      class="w-8 h-8 rounded-full border border-gray-300 flex items-center justify-center"
                      on:click={() => (adults = Math.min(9, adults + 1))}
                      >+</button
                    >
                  </div>
                </div>

                <div class="flex items-center justify-between">
                  <span class="text-sm">Infants (0-2 years)</span>
                  <div class="flex items-center">
                    <button
                      type="button"
                      class="w-8 h-8 rounded-full border border-gray-300 flex items-center justify-center"
                      on:click={() => (infants = Math.max(0, infants - 1))}
                      >-</button
                    >
                    <span class="mx-2 w-6 text-center">{infants}</span>
                    <button
                      type="button"
                      class="w-8 h-8 rounded-full border border-gray-300 flex items-center justify-center"
                      on:click={() => (infants = Math.min(adults, infants + 1))}
                      >+</button
                    >
                  </div>
                </div>
              </div>
            </div>

            <div>
              <label
                for="travelClass"
                class="block text-sm font-medium text-gray-700 mb-1 flex items-center"
              >
                <div class="bg-indigo-50 p-1 rounded-full mr-2">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-indigo-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                  </svg>
                </div>
                Travel Class
              </label>
              <select
                id="travelClass"
                bind:value={travelClass}
                class="w-full p-2 border border-gray-300 rounded-md"
              >
                <option value="ECONOMY">Economy</option>
                <option value="BUSINESS">Business</option>
                <option value="FIRST">First Class</option>
              </select>
            </div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
            <div>
              <label
                for="maxPrice"
                class="block text-sm font-medium text-gray-700 mb-1 flex items-center"
              >
                <div class="bg-emerald-50 p-1 rounded-full mr-2">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-emerald-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                Maximum Price in €
              </label>
              <input
                id="maxPrice"
                type="number"
                bind:value={maxPrice}
                placeholder="e.g. 1000"
                class="w-full p-2 border border-gray-300 rounded-md"
              />
            </div>

            <div>
              <label
                for="maxResults"
                class="block text-sm font-medium text-gray-700 mb-1 flex items-center"
              >
                <div class="bg-gray-100 p-1 rounded-full mr-2">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-gray-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 10h16M4 14h16M4 18h16" />
                  </svg>
                </div>
                Maximum Results
              </label>
              <input
                id="maxResults"
                type="number"
                min="1"
                max="250"
                bind:value={maxResults}
                class="w-full p-2 border border-gray-300 rounded-md"
              />
            </div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
            <div>
              <label
                for="includedAirlineCodes"
                class="block text-sm font-medium text-gray-700 mb-1"
                >Preferred Airlines (comma-separated codes)</label
              >
              <input
                id="includedAirlineCodes"
                type="text"
                bind:value={includedAirlineCodes}
                placeholder="e.g. LH,AF,BA"
                class="w-full p-2 border border-gray-300 rounded-md"
              />
            </div>

            <div>
              <label
                for="excludedAirlineCodes"
                class="block text-sm font-medium text-gray-700 mb-1"
                >Excluded Airlines (comma-separated codes)</label
              >
              <input
                id="excludedAirlineCodes"
                type="text"
                bind:value={excludedAirlineCodes}
                placeholder="e.g. FR,W6,U2"
                class="w-full p-2 border border-gray-300 rounded-md"
              />
            </div>
          </div>
        </div>
      {/if}

      <div class="pt-2">
        <button
          type="submit"
          class="w-full bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-5 rounded-lg shadow-sm transition-all duration-200 transform hover:-translate-y-0.5 flex items-center justify-center"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
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
      class="mt-6 p-4 bg-red-50 border border-red-200 text-red-700 rounded-md flex items-center"
    >
      <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
      </svg>
      {error}
    </div>
  {:else if flights.length === 0}
    <div
      class="mt-6 p-4 bg-blue-50 border border-blue-200 text-blue-700 rounded-md flex items-center"
    >
      <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
      No flights available. Please try a different search.
    </div>
  {/if}

  <!-- Flight Cards -->
  <div class="mt-6 grid grid-cols-1 md:grid-cols-2 gap-4">
    {#each flights as flight}
      <button
        class="bg-white border border-gray-200 shadow-sm rounded-lg p-4 cursor-pointer w-full text-left transition-all duration-200 transform hover:border-blue-500 hover:-translate-y-0.5 hover:shadow-lg"
        on:click={() => (selectedFlight = flight)}
        on:keydown={(e) => e.key === "Enter" && (selectedFlight = flight)}
        aria-label={`Select flight from ${flight.departure.iata} to ${flight.arrival.iata}`}
      >
        <div class="flex justify-between items-center mb-3">
          <div class="flex items-center">
            <div class="bg-blue-100 p-1.5 rounded-full mr-2 flex-shrink-0">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-blue-700" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z" />
              </svg>
            </div>
            <h3 class="text-blue-700 font-semibold">
              {flight.departure.airport} → {flight.arrival.airport}
            </h3>
          </div>
          {#if flight.airline}
            <div class="bg-gray-100 px-3 py-1.5 rounded-md">
              <p class="font-semibold text-gray-700">{flight.airline.name}</p>
            </div>
          {/if}
        </div>
        
        <div class="grid grid-cols-1 sm:grid-cols-3 gap-3 mb-1 border-t border-gray-100 pt-2">
          <div class="flex items-start">
            <div class="bg-blue-50 p-1.5 rounded-full mr-2 mt-0.5">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
              </svg>
            </div>
            <div>
              <p class="text-xs text-gray-500">Departure</p>
              <p class="text-sm font-medium">{formatDateTime(flight.departure.scheduled)}</p>
              <p class="text-xs text-gray-500">{flight.departure.iata}</p>
            </div>
          </div>
          
          <div class="flex items-start">
            <div class="bg-green-50 p-1.5 rounded-full mr-2 mt-0.5">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" transform="rotate(180 12 12)" />
              </svg>
            </div>
            <div>
              <p class="text-xs text-gray-500">Arrival</p>
              <p class="text-sm font-medium">{formatDateTime(flight.arrival.scheduled)}</p>
              <p class="text-xs text-gray-500">{flight.arrival.iata}</p>
            </div>
          </div>
          
          <div class="flex items-start">
            <div class="bg-purple-50 p-1.5 rounded-full mr-2 mt-0.5">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-purple-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <div>
              <p class="text-xs text-gray-500">Duration</p>
              <p class="text-sm font-medium">{formatDuration(parseInt(flight.duration))}</p>
            </div>
          </div>
        </div>

        <div class="flex justify-between items-center mt-3 pt-2 border-t border-gray-100">
          <div class="flex items-center">
            <div class="bg-yellow-50 p-1.5 rounded-full mr-2">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-yellow-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
              </svg>
            </div>
            <p class="text-sm text-gray-600">{flight.status || "Scheduled"}</p>
          </div>
          <div class="bg-emerald-50 px-3 py-1.5 rounded-md">
            <p class="font-semibold text-emerald-700">€{flight.price}</p>
          </div>
        </div>
      </button>
    {/each}
  </div>

  <!-- Modal -->
  {#if selectedFlight}
    <div class="fixed inset-0 flex items-center justify-center z-50">
      <button 
        type="button" 
        class="fixed inset-0 bg-black/50 backdrop-blur-sm" 
        on:click={() => (selectedFlight = null)} 
        on:keydown={(e) => e.key === "Escape" && (selectedFlight = null)} 
        aria-label="Close flight details modal">
      </button>

      <div class="bg-white rounded-lg shadow-xl w-full max-w-2xl z-10">
        <div class="bg-blue-50 p-4 rounded-t-lg border-b border-blue-100 flex justify-between items-center">
          <h3 class="text-xl font-bold text-blue-800">
            Flight Details
          </h3>
          <button 
            class="text-gray-400 hover:text-gray-600 transition-colors"
            on:click={() => (selectedFlight = null)}
            aria-label="Close modal"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
        
        <div class="p-4 border-b border-gray-100 flex justify-between items-center">
          <div class="flex items-center">
            <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center mr-4">
              <span class="font-semibold text-blue-700">{selectedFlight.airline.substring(0, 2)}</span>
            </div>
            <div>
              <h4 class="font-medium text-lg">
                {selectedFlight.departure.airport} → {selectedFlight.arrival.airport}
              </h4>
              <p class="text-sm text-gray-500">
                Flight {selectedFlight.id} • {selectedFlight.airline}
              </p>
            </div>
          </div>
          <div class="text-right">
            <p class="text-sm text-gray-500">Price</p>
            <p class="text-xl font-semibold text-blue-700">€{selectedFlight.price}</p>
          </div>
        </div>

        <div class="p-6">
          <div class="flex mb-6">
            <div class="flex flex-col items-center mr-4">
              <div class="w-3 h-3 bg-blue-600 rounded-full"></div>
              <div class="w-0.5 bg-gray-300 flex-grow my-2"></div>
              <div class="w-3 h-3 bg-green-600 rounded-full"></div>
            </div>
            
            <div class="flex-grow">
              <div class="mb-8">
                <div class="flex items-start">
                  <div class="bg-blue-50 p-2 rounded-full mr-3">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                    </svg>
                  </div>
                  <div>
                    <div class="flex justify-between">
                      <p class="text-lg font-semibold">{formatDateTime(selectedFlight.departure.scheduled)}</p>
                      <p class={`text-sm ml-4 px-2 py-0.5 rounded-full ${selectedFlight.departure.delay > 0 ? 'bg-orange-50 text-orange-700' : 'bg-green-50 text-green-700'}`}>
                        {selectedFlight.departure.delay > 0 ? `Delayed by ${selectedFlight.departure.delay} min` : "On time"}
                      </p>
                    </div>
                    <p class="font-medium">{selectedFlight.departure.airport} ({selectedFlight.departure.iata})</p>
                    <p class="text-sm text-gray-600">
                      Terminal: {selectedFlight.departure.terminal !== "null" ? selectedFlight.departure.terminal : "TBA"}, 
                      Gate: {selectedFlight.departure.gate !== "null" ? selectedFlight.departure.gate : "TBA"}
                    </p>
                  </div>
                </div>
              </div>
              
              <div>
                <div class="flex items-start">
                  <div class="bg-green-50 p-2 rounded-full mr-3">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                    </svg>
                  </div>
                  <div>
                    <div class="flex justify-between">
                      <p class="text-lg font-semibold">{formatDateTime(selectedFlight.arrival.scheduled)}</p>
                      <p class={`text-sm ml-4 px-2 py-0.5 rounded-full ${selectedFlight.arrival.delay > 0 ? 'bg-orange-50 text-orange-700' : 'bg-green-50 text-green-700'}`}>
                        {selectedFlight.arrival.delay > 0 ? `Delayed by ${selectedFlight.arrival.delay} min` : "On time"}
                      </p>
                    </div>
                    <p class="font-medium">{selectedFlight.arrival.airport} ({selectedFlight.arrival.iata})</p>
                    <p class="text-sm text-gray-600">
                      Terminal: {selectedFlight.arrival.terminal !== "null" ? selectedFlight.arrival.terminal : "TBA"}, 
                      Gate: {selectedFlight.arrival.gate !== "null" ? selectedFlight.arrival.gate : "TBA"}
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="grid grid-cols-3 gap-4 border-t pt-4">
            <div class="flex items-start">
              <div class="bg-purple-50 p-2 rounded-full mr-3">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-purple-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <div>
                <p class="text-sm text-gray-500">Duration</p>
                <p class="font-medium">
                  {formatDuration(parseInt(selectedFlight.duration))}
                </p>
              </div>
            </div>
            
            <div class="flex items-start">
              <div class="bg-yellow-50 p-2 rounded-full mr-3">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-yellow-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <div>
                <p class="text-sm text-gray-500">Status</p>
                <p class="font-medium capitalize">{selectedFlight.status || "Scheduled"}</p>
              </div>
            </div>

            <div class="flex items-start">
              <div class="bg-indigo-50 p-2 rounded-full mr-3">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-indigo-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                </svg>
              </div>
              <div>
                <p class="text-sm text-gray-500">Class</p>
                <p class="font-medium">{travelClass === "ECONOMY" ? "Economy" : travelClass === "BUSINESS" ? "Business" : "First Class"}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  {/if}
</div>
