<script>
  import { onMount } from "svelte";
  import { api } from "$lib/api";
  import { goto } from "$app/navigation";
  import { page } from "$app/stores";
  import { bookingStore } from "$lib/stores/bookingStore";

  let flightOffers = [];
  let loading = true;
  let error = null;
  let searchParams = {};
  let selectedOffer = null;
  let showDetailModal = false;
  let carriers = {};
  let locations = {};

  onMount(async () => {
    const urlParams = $page.url.searchParams;

    searchParams = {
      originLocationCode: urlParams.get("originLocationCode") || "",
      destinationLocationCode: urlParams.get("destinationLocationCode") || "",
      departureDate: urlParams.get("departureDate") || "",
      returnDate: urlParams.get("returnDate") || "",
      adults: urlParams.get("adults") || "1",
      infants: urlParams.get("infants") || "0",
      travelClass: urlParams.get("travelClass") || "ECONOMY",
      nonStop: urlParams.get("nonStop") === "true",
      max: urlParams.get("max") || "5",
      currencyCode: urlParams.get("currencyCode") || "EUR",
      maxPrice: urlParams.get("maxPrice") || ""
    };
    
    await searchFlights();
  });

  async function searchFlights() {
    loading = true;
    error = null;
    
    try {
      const queryParams = new URLSearchParams();
      
      for (const [key, value] of Object.entries(searchParams)) {
        if (value !== null && value !== "") {
          queryParams.append(key, value);
        }
      }
      
      const endpoint = `flights/search?${queryParams.toString()}`;
      const response = await api.all(endpoint);
      
      if (response && response.data) {
        flightOffers = response.data;
        
        if (response.dictionaries) {
          carriers = response.dictionaries.carriers || {};
          locations = response.dictionaries.locations || {};
        }
        
        if (flightOffers.length === 0) {
          error = "No flights found matching your criteria. Please try a different search.";
        }
      } else {
        error = "Invalid response format from server";
        flightOffers = [];
      }
    } catch (err) {
      console.error("Failed to fetch flights:", err);
      error = "Failed to load flights. Please try again later.";
      flightOffers = [];
    } finally {
      loading = false;
    }
  }

  function formatDateTime(dateString) {
    if (!dateString) return "N/A";
    return new Date(dateString).toLocaleString("en-GB", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  }

  function formatDate(dateString) {
    if (!dateString) return "N/A";
    return new Date(dateString).toLocaleString("en-GB", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    });
  }

  function formatTime(dateString) {
    if (!dateString) return "N/A";
    return new Date(dateString).toLocaleString("en-GB", {
      hour: "2-digit",
      minute: "2-digit",
    });
  }

  function formatDuration(isoDuration) {
    if (!isoDuration) return "N/A";
    const hoursMatch = isoDuration.match(/(\d+)H/);
    const minutesMatch = isoDuration.match(/(\d+)M/);
    const hours = hoursMatch ? parseInt(hoursMatch[1]) : 0;
    const minutes = minutesMatch ? parseInt(minutesMatch[1]) : 0;
    if (hours === 0) {
      return `${minutes}min`;
    } else if (minutes === 0) {
      return `${hours}h`;
    } else {
      return `${hours}h ${minutes}min`;
    }
  }

  function getAirlineName(carrierCode) {
    return carriers[carrierCode] || carrierCode;
  }

  function getLocationInfo(iataCode) {
    const location = locations[iataCode] || {};
    return {
      cityCode: location.cityCode || iataCode,
      countryCode: location.countryCode || "",
    };
  }

  function selectOffer(offer) {
    const outboundTrip = offer.trips.find((trip) => trip.type === "outbound");
    const returnTrip = offer.trips.find((trip) => trip.type === "return");
    const firstOutboundFlight = outboundTrip?.flights[0];
    const lastOutboundFlight = outboundTrip?.flights[outboundTrip.flights.length - 1];

    const flightData = {
      id: offer.id,
      airline: getAirlineName(firstOutboundFlight?.carrierCode),
      price: offer.price.total,
      currency: offer.price.currency,
      duration: outboundTrip?.duration
        ? parseDurationToMinutes(outboundTrip.duration)
        : 0,
      status: "Scheduled",
      departure: {
        iata: firstOutboundFlight?.departure.iata,
        airport: getLocationInfo(firstOutboundFlight?.departure.iata).cityCode,
        terminal: firstOutboundFlight?.departure.terminal || null,
        gate: null,
        delay: 0,
        scheduled: firstOutboundFlight?.departure.scheduled,
      },
      arrival: {
        iata: lastOutboundFlight?.arrival.iata,
        airport: getLocationInfo(lastOutboundFlight?.arrival.iata).cityCode,
        terminal: lastOutboundFlight?.arrival.terminal || null,
        gate: null,
        delay: 0,
        scheduled: lastOutboundFlight?.arrival.scheduled,
      },
      fullOffer: offer,
    };

    bookingStore.update((store) => ({
      ...store,
      flight: flightData,
      AdultPassengers: parseInt(searchParams.adults),
      infantsPassengers: parseInt(searchParams.infants) || 0,
      travelClass: searchParams.travelClass,
    }));
    goto("/booking/customerDetails");
  }

  function parseDurationToMinutes(isoDuration) {
    if (!isoDuration) return 0;
    const hoursMatch = isoDuration.match(/(\d+)H/);
    const minutesMatch = isoDuration.match(/(\d+)M/);
    const hours = hoursMatch ? parseInt(hoursMatch[1]) : 0;
    const minutes = minutesMatch ? parseInt(minutesMatch[1]) : 0;
    return hours * 60 + minutes;
  }

  // Add helper function for better date formatting
  function formatDateForDisplay(dateString) {
    if (!dateString) return "N/A";
    const date = new Date(dateString);
    const options = { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' };
    return date.toLocaleDateString('en-US', options);
  }

  // Get human readable name for travel class
  function getTravelClassName(classCode) {
    const classes = {
      "ECONOMY": "Economy",
      "BUSINESS": "Business",
      "FIRST": "First"
    };
    return classes[classCode] || classCode;
  }

  // Open modal with selected offer details
  function openDetailModal(offer) {
    selectedOffer = offer;
    showDetailModal = true;
  }

  // Close modal
  function closeModal() {
    showDetailModal = false;
  }

  // Get aircraft name from aircraft code
  function getAircraftName(aircraftCode) {
    if (!aircraftCode) return "Aircraft information not available";
    const aircraftDict = locations?.aircraft || {};
    return aircraftDict[aircraftCode] || aircraftCode;
  }

  // Get location name from IATA code
  function getLocationName(iataCode) {
    if (!iataCode) return "";
    const location = locations[iataCode] || {};
    return location.cityCode || iataCode;
  }

  // Get baggage allowance text
  function getBaggageAllowance(cabin = "ECONOMY") {
    const allowances = {
      "ECONOMY": "1 carry-on bag, 1 checked bag (23kg)",
      "BUSINESS": "2 carry-on bags, 2 checked bags (32kg each)",
      "FIRST": "2 carry-on bags, 3 checked bags (32kg each)"
    };
    
    return allowances[cabin] || allowances["ECONOMY"];
  }
</script>

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
        <span class="ms-1 text-sm font-medium text-gray-400"
          >Search Flights</span
        >
      </div>
    </li>
  </ol>
</nav>

<div class="max-w-6xl mx-auto p-6">
  <div class="mb-8 bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
    <div class="bg-blue-50 p-4 border-b border-gray-200">
      <h2 class="text-lg font-medium text-blue-800">Flight Search Details</h2>
    </div>
    <div class="p-4 grid grid-cols-1 md:grid-cols-2 gap-6">
      <div class="col-span-1 md:col-span-2 flex flex-col md:flex-row items-start md:items-center">
        <div class="flex items-center">
          <div class="bg-blue-100 p-2 rounded-full mr-3">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-blue-700" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
            </svg>
          </div>
          <div>
            <p class="text-sm text-gray-500">Trip Type</p>
            <p class="font-medium">{searchParams.returnDate ? "Round Trip" : "One Way"}</p>
          </div>
        </div>
        
        <div class="mt-4 md:mt-0 md:ml-6 md:pl-6 md:border-l border-gray-200">
          <div class="flex items-center">
            <div class="py-2 px-4 rounded-full bg-blue-600 text-white font-medium">
              {searchParams.originLocationCode}
            </div>
            <div class="mx-3 flex-1 h-0.5 bg-gray-300 relative">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-blue-600 absolute top-1/2 left-1/2 transform -translate-y-1/2 -translate-x-1/2" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M12.293 5.293a1 1 0 011.414 0l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414-1.414L14.586 11H3a1 1 0 110-2h11.586l-2.293-2.293a1 1 0 010-1.414z" clip-rule="evenodd" />
              </svg>
            </div>
            <div class="py-2 px-4 rounded-full bg-blue-600 text-white font-medium">
              {searchParams.destinationLocationCode}
            </div>
          </div>
        </div>
      </div>
      
      <!-- Departure date -->
      <div class="flex items-start">
        <div class="bg-green-100 p-2 rounded-full mr-3">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-green-700" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
          </svg>
        </div>
        <div>
          <p class="text-sm text-gray-500">Departure Date</p>
          <p class="font-medium">{formatDateForDisplay(searchParams.departureDate)}</p>
        </div>
      </div>

      <!-- Return date if exists -->
      {#if searchParams.returnDate}
        <div class="flex items-start">
          <div class="bg-red-100 p-2 rounded-full mr-3">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-red-700" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
          </div>
          <div>
            <p class="text-sm text-gray-500">Return Date</p>
            <p class="font-medium">{formatDateForDisplay(searchParams.returnDate)}</p>
          </div>
        </div>
      {/if}

      <!-- Passengers -->
      <div class="flex items-start">
        <div class="bg-yellow-100 p-2 rounded-full mr-3">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-yellow-700" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
          </svg>
        </div>
        <div>
          <p class="text-sm text-gray-500">Passengers</p>
          <p class="font-medium">
            {searchParams.adults} Adult{parseInt(searchParams.adults) !== 1 ? 's' : ''}
            {#if parseInt(searchParams.infants) > 0}
              , {searchParams.infants} Infant{parseInt(searchParams.infants) !== 1 ? 's' : ''}
            {/if}
          </p>
        </div>
      </div>
    </div>
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
  {:else if flightOffers.length === 0}
    <div
      class="mt-6 p-4 bg-blue-50 border border-blue-200 text-blue-700 rounded-md"
    >
      No flights available. Please try a different search.
    </div>
  {:else}
    <!-- Flight Offer Cards -->
    <div class="mt-6 space-y-6">
      {#each flightOffers as offer}
        {@const outboundTrip = offer.trips.find(
          (trip) => trip.type === "outbound"
        )}
        {@const returnTrip = offer.trips.find((trip) => trip.type === "return")}

        <div
          class="bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden hover:shadow-md transition-shadow"
        >
          <div
            class="p-4 border-b border-gray-100 flex justify-between items-center bg-gray-50"
          >
            <div>
              <span class="text-sm text-gray-500">Flight offer</span>
              <h3 class="font-medium">
                {searchParams.originLocationCode} → {searchParams.destinationLocationCode}
              </h3>
            </div>
            <div class="text-right">
              <span class="text-sm text-gray-500">Total Price</span>
              <p class="font-semibold text-xl text-blue-700">
                {offer.price.currency}
                {offer.price.total}
              </p>
            </div>
          </div>

          <!-- Outbound trip -->
          {#if outboundTrip}
            <div class="p-4 border-b border-gray-100">
              <div class="flex items-center gap-2 mb-3">
                <span
                  class="bg-blue-100 text-blue-800 text-xs font-medium px-2.5 py-0.5 rounded"
                  >Outbound</span
                >
                <span class="text-sm text-gray-500">
                  {outboundTrip.flights.length > 1
                    ? `${outboundTrip.flights.length - 1} connection${outboundTrip.flights.length > 2 ? "s" : ""}`
                    : "Direct flight"}
                </span>
                <span class="text-sm text-gray-500">·</span>
                <span class="text-sm text-gray-500"
                  >{formatDuration(outboundTrip.duration)}</span
                >
              </div>

              <div class="flex flex-col gap-4">
                <div class="flex justify-between items-center">
                  <div>
                    <p class="text-xl font-semibold">
                      {formatTime(outboundTrip.flights[0].departure.scheduled)}
                    </p>
                    <p class="text-sm text-gray-600">
                      {formatDate(outboundTrip.flights[0].departure.scheduled)}
                    </p>
                    <p class="text-sm text-gray-600">
                      {outboundTrip.flights[0].departure.iata}
                      {outboundTrip.flights[0].departure.terminal
                        ? ` Terminal ${outboundTrip.flights[0].departure.terminal}`
                        : ""}
                    </p>
                  </div>

                  <div class="flex-1 px-4">
                    <div class="w-full flex flex-col items-center">
                      <div class="text-xs text-gray-500 mb-2">
                        {formatDuration(outboundTrip.duration)}
                      </div>
                      <div class="w-full h-0.5 bg-gray-200 relative">
                        {#each outboundTrip.flights as flight, i}
                          {#if i < outboundTrip.flights.length - 1}
                            <div
                              class="absolute top-0 transform -translate-y-1/2"
                              style="left: {((i + 1) /
                                outboundTrip.flights.length) *
                                100}%"
                            >
                              <div
                                class="h-3 w-3 bg-blue-600 rounded-full"
                              ></div>
                            </div>
                          {/if}
                        {/each}
                      </div>
                      <div class="text-xs text-gray-500 mt-2">
                        {outboundTrip.flights.length > 1
                          ? `${outboundTrip.flights.length - 1} stop${outboundTrip.flights.length > 2 ? "s" : ""}`
                          : "Direct"}
                      </div>
                    </div>
                  </div>

                  <div class="text-right">
                    <p class="text-xl font-semibold">
                      {formatTime(
                        outboundTrip.flights[outboundTrip.flights.length - 1]
                          .arrival.scheduled
                      )}
                    </p>
                    <p class="text-sm text-gray-600">
                      {formatDate(
                        outboundTrip.flights[outboundTrip.flights.length - 1]
                          .arrival.scheduled
                      )}
                    </p>
                    <p class="text-sm text-gray-600">
                      {outboundTrip.flights[outboundTrip.flights.length - 1]
                        .arrival.iata}
                      {outboundTrip.flights[outboundTrip.flights.length - 1]
                        .arrival.terminal
                        ? ` Terminal ${outboundTrip.flights[outboundTrip.flights.length - 1].arrival.terminal}`
                        : ""}
                    </p>
                  </div>
                </div>

                {#if outboundTrip.flights.length > 1}
                  <div
                    class="text-sm text-gray-600 pl-4 border-l-2 border-gray-200"
                  >
                    <p class="font-medium">Connections:</p>
                    {#each outboundTrip.flights as flight, index}
                      {#if index < outboundTrip.flights.length - 1}
                        {@const nextFlight = outboundTrip.flights[index + 1]}
                        {@const connectionDuration =
                          new Date(nextFlight.departure.scheduled) -
                          new Date(flight.arrival.scheduled)}
                        {@const hours = Math.floor(
                          connectionDuration / (1000 * 60 * 60)
                        )}
                        {@const minutes = Math.floor(
                          (connectionDuration % (1000 * 60 * 60)) / (1000 * 60)
                        )}

                        <div class="mt-2">
                          <p>
                            {flight.arrival.iata} · Connection time: {hours > 0
                              ? `${hours}h `
                              : ""}{minutes}min
                          </p>
                        </div>
                      {/if}
                    {/each}
                  </div>
                {/if}
              </div>
            </div>
          {/if}

          <!-- Return trip (if exists) -->
          {#if returnTrip}
            <div class="p-4 border-b border-gray-100">
              <div class="flex items-center gap-2 mb-3">
                <span
                  class="bg-green-100 text-green-800 text-xs font-medium px-2.5 py-0.5 rounded"
                  >Return</span
                >
                <span class="text-sm text-gray-500">
                  {returnTrip.flights.length > 1
                    ? `${returnTrip.flights.length - 1} connection${returnTrip.flights.length > 2 ? "s" : ""}`
                    : "Direct flight"}
                </span>
                <span class="text-sm text-gray-500">·</span>
                <span class="text-sm text-gray-500"
                  >{formatDuration(returnTrip.duration)}</span
                >
              </div>

              <div class="flex flex-col gap-4">
                <div class="flex justify-between items-center">
                  <div>
                    <p class="text-xl font-semibold">
                      {formatTime(returnTrip.flights[0].departure.scheduled)}
                    </p>
                    <p class="text-sm text-gray-600">
                      {formatDate(returnTrip.flights[0].departure.scheduled)}
                    </p>
                    <p class="text-sm text-gray-600">
                      {returnTrip.flights[0].departure.iata}
                      {returnTrip.flights[0].departure.terminal
                        ? ` Terminal ${returnTrip.flights[0].departure.terminal}`
                        : ""}
                    </p>
                  </div>

                  <div class="flex-1 px-4">
                    <div class="w-full flex flex-col items-center">
                      <div class="text-xs text-gray-500 mb-2">
                        {formatDuration(returnTrip.duration)}
                      </div>
                      <div class="w-full h-0.5 bg-gray-200 relative">
                        {#each returnTrip.flights as flight, i}
                          {#if i < returnTrip.flights.length - 1}
                            <div
                              class="absolute top-0 transform -translate-y-1/2"
                              style="left: {((i + 1) /
                                returnTrip.flights.length) *
                                100}%"
                            >
                              <div
                                class="h-3 w-3 bg-green-600 rounded-full"
                              ></div>
                            </div>
                          {/if}
                        {/each}
                      </div>
                      <div class="text-xs text-gray-500 mt-2">
                        {returnTrip.flights.length > 1
                          ? `${returnTrip.flights.length - 1} stop${returnTrip.flights.length > 2 ? "s" : ""}`
                          : "Direct"}
                      </div>
                    </div>
                  </div>

                  <div class="text-right">
                    <p class="text-xl font-semibold">
                      {formatTime(
                        returnTrip.flights[returnTrip.flights.length - 1]
                          .arrival.scheduled
                      )}
                    </p>
                    <p class="text-sm text-gray-600">
                      {formatDate(
                        returnTrip.flights[returnTrip.flights.length - 1]
                          .arrival.scheduled
                      )}
                    </p>
                    <p class="text-sm text-gray-600">
                      {returnTrip.flights[returnTrip.flights.length - 1].arrival
                        .iata}
                      {returnTrip.flights[returnTrip.flights.length - 1].arrival
                        .terminal
                        ? ` Terminal ${returnTrip.flights[returnTrip.flights.length - 1].arrival.terminal}`
                        : ""}
                    </p>
                  </div>
                </div>

                <!-- Connection details if more than one flight -->
                {#if returnTrip.flights.length > 1}
                  <div
                    class="text-sm text-gray-600 pl-4 border-l-2 border-gray-200"
                  >
                    <p class="font-medium">Connections:</p>
                    {#each returnTrip.flights as flight, index}
                      {#if index < returnTrip.flights.length - 1}
                        {@const nextFlight = returnTrip.flights[index + 1]}
                        {@const connectionDuration =
                          new Date(nextFlight.departure.scheduled) -
                          new Date(flight.arrival.scheduled)}
                        {@const hours = Math.floor(
                          connectionDuration / (1000 * 60 * 60)
                        )}
                        {@const minutes = Math.floor(
                          (connectionDuration % (1000 * 60 * 60)) / (1000 * 60)
                        )}

                        <div class="mt-2">
                          <p>
                            {flight.arrival.iata} · Connection time: {hours > 0
                              ? `${hours}h `
                              : ""}{minutes}min
                          </p>
                        </div>
                      {/if}
                    {/each}
                  </div>
                {/if}
              </div>
            </div>
          {/if}

          <!-- Action buttons -->
          <div class="p-4 flex justify-between items-center">
            <div>
              <p class="text-sm text-gray-500">
                Operated by {getAirlineName(
                  outboundTrip?.flights[0]?.carrierCode
                )}
              </p>
            </div>
            <button
              on:click={() => openDetailModal(offer)}
              class="bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-5 rounded-lg"
            >
              Select
            </button>
          </div>
        </div>
      {/each}
    </div>
  {/if}

  <div class="mt-6">
    <a
      href="/home"
      class="inline-flex items-center bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-5 rounded-lg shadow-sm transition-all duration-200 transform hover:-translate-y-0.5"
    >
      <svg
        xmlns="http://www.w3.org/2000/svg"
        class="h-5 w-5 mr-2"
        viewBox="0 0 20 20"
        fill="currentColor"
      >
        <path
          fill-rule="evenodd"
          d="M9.707 16.707a1 1 0 01-1.414 0l-6-6a1 1 0 010-1.414l6-6a1 1 0 011.414 1.414L5.414 9H17a1 1 0 110 2H5.414l4.293 4.293a1 1 0 010 1.414z"
          clip-rule="evenodd"
        />
      </svg>
      Back to Home
    </a>
  </div>
</div>

<!-- Detailed Flight Modal -->
{#if showDetailModal && selectedOffer}
  {@const outboundTrip = selectedOffer.trips.find((trip) => trip.type === "outbound")}
  {@const returnTrip = selectedOffer.trips.find((trip) => trip.type === "return")}
  
  <div class="fixed inset-0 z-50 overflow-y-auto">
    <div class="fixed inset-0 bg-black/50 backdrop-blur-sm" on:click={closeModal}></div>
    
    <div class="relative min-h-screen flex items-center justify-center p-4">
      <div class="bg-white rounded-lg shadow-xl w-full max-w-4xl">
        <div class="bg-blue-50 p-4 rounded-t-lg border-b border-blue-100 flex justify-between items-center">
          <h3 class="text-xl font-bold text-blue-800">
            Flight Details
          </h3>
          <button 
            class="text-gray-400 hover:text-gray-600 transition-colors"
            on:click={closeModal}
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
        
        <!-- Price summary -->
        <div class="bg-gray-50 p-4 border-b border-gray-100">
          <div class="flex justify-between items-center">
            <div>
              <p class="text-sm text-gray-500">Total price</p>
              <p class="text-2xl font-bold text-blue-700">
                {selectedOffer.price.currency} {selectedOffer.price.total}
              </p>
              {#if parseInt(searchParams.adults) > 1 || parseInt(searchParams.infants) > 0}
                <p class="text-xs text-gray-500">
                  {parseInt(searchParams.adults)} adult{parseInt(searchParams.adults) !== 1 ? 's' : ''}
                  {#if parseInt(searchParams.infants) > 0}
                    , {parseInt(searchParams.infants)} infant{parseInt(searchParams.infants) !== 1 ? 's' : ''}
                  {/if}
                </p>
              {/if}
            </div>
            <div class="text-sm bg-green-50 text-green-700 px-3 py-1 rounded-full border border-green-200">
              {getTravelClassName(searchParams.travelClass)}
            </div>
          </div>
        </div>
        
        <!-- Content -->
        <div class="p-0 max-h-[60vh] overflow-y-auto">
          <div class="p-4 border-b border-gray-200">
            <div class="flex items-center gap-2 mb-4">
              <div class="bg-blue-100 p-2 rounded-full">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-blue-700" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M11.3 1.046A1 1 0 0112 2v5h4a1 1 0 01.82 1.573l-7 10A1 1 0 018 18v-5H4a1 1 0 01-.82-1.573l7-10a1 1 0 011.12-.38z" clip-rule="evenodd" />
                </svg>
              </div>
              <h4 class="text-lg font-semibold">
                Outbound Journey
                <span class="text-sm font-normal text-gray-500">
                  ({searchParams.originLocationCode} to {searchParams.destinationLocationCode})
                </span>
              </h4>
            </div>
            
            {#each outboundTrip.flights as flight, index}
              <div class="mb-6 {index < outboundTrip.flights.length - 1 ? 'border-b border-gray-100 pb-6' : ''}">
                <div class="flex items-center gap-2 mb-2">
                  <span class="text-xs font-medium text-gray-500">Flight {index + 1} of {outboundTrip.flights.length}</span>
                  {#if outboundTrip.flights.length > 1 && index < outboundTrip.flights.length - 1}
                    <span class="text-xs bg-yellow-100 text-yellow-800 px-2 py-0.5 rounded-full">Connection</span>
                  {/if}
                </div>
                
                <!-- Flight Card -->
                <div class="bg-gray-50 rounded-lg p-4">
                  <div class="flex justify-between items-center mb-4">
                    <div class="flex items-center">
                      <div class="w-10 h-10 bg-gray-200 rounded-full flex items-center justify-center mr-3">
                        <span class="font-medium">{getAirlineName(flight.carrierCode).substring(0, 2)}</span>
                      </div>
                      <div>
                        <p class="font-medium">{getAirlineName(flight.carrierCode)}</p>
                        <p class="text-xs text-gray-500">Flight {flight.carrierCode}{flight.number}</p>
                      </div>
                    </div>
                    <div class="text-right">
                      <span class="font-medium text-sm">{formatDuration(flight.duration)}</span>
                    </div>
                  </div>
                  
                  <div class="flex">
                    <div class="flex flex-col items-center mr-4">
                      <div class="w-3 h-3 bg-blue-600 rounded-full"></div>
                      <div class="w-0.5 bg-gray-300 flex-grow my-2"></div>
                      <div class="w-3 h-3 bg-green-600 rounded-full"></div>
                    </div>
                    
                    <div class="flex-grow">
                      <div class="mb-6">
                        <div class="flex justify-between">
                          <p class="text-lg font-semibold">{formatTime(flight.departure.scheduled)}</p>
                          <p class="text-sm text-gray-500">{formatDate(flight.departure.scheduled)}</p>
                        </div>
                        <p class="font-medium">{flight.departure.iata} {getLocationName(flight.departure.iata)}</p>
                        {#if flight.departure.terminal}
                          <p class="text-sm text-gray-600">Terminal {flight.departure.terminal}</p>
                        {/if}
                      </div>
                      
                      <!-- Arrival -->
                      <div>
                        <div class="flex justify-between">
                          <p class="text-lg font-semibold">{formatTime(flight.arrival.scheduled)}</p>
                          <p class="text-sm text-gray-500">{formatDate(flight.arrival.scheduled)}</p>
                        </div>
                        <p class="font-medium">{flight.arrival.iata} {getLocationName(flight.arrival.iata)}</p>
                        {#if flight.arrival.terminal}
                          <p class="text-sm text-gray-600">Terminal {flight.arrival.terminal}</p>
                        {/if}
                      </div>
                    </div>
                  </div>
                </div>
                
                <!-- Connection information -->
                {#if index < outboundTrip.flights.length - 1}
                  {@const nextFlight = outboundTrip.flights[index + 1]}
                  {@const connectionDuration = new Date(nextFlight.departure.scheduled) - new Date(flight.arrival.scheduled)}
                  {@const hours = Math.floor(connectionDuration / (1000 * 60 * 60))}
                  {@const minutes = Math.floor((connectionDuration % (1000 * 60 * 60)) / (1000 * 60))}
                  
                  <div class="mt-2 mb-2 flex items-center pl-6">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-orange-500 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    <div>
                      <p class="text-sm font-medium text-orange-700">
                        Connection time: {hours > 0 ? `${hours}h ` : ""}{minutes}min at {flight.arrival.iata}
                      </p>
                      <p class="text-xs text-gray-500">
                        You'll need to change planes at {getLocationName(flight.arrival.iata)} Airport
                      </p>
                    </div>
                  </div>
                {/if}
              </div>
            {/each}
          </div>
          
          <!-- Return Trip (if exists) -->
          {#if returnTrip}
            <div class="p-4 border-b border-gray-200">
              <div class="flex items-center gap-2 mb-4">
                <div class="bg-green-100 p-2 rounded-full">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-green-700" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M8.7 1.046A1 1 0 018 2v5H4a1 1 0 01-.82-1.573l-7-10A1 1 0 0112 0v5h4a1 1 0 01.82 1.573l-7 10A1 1 0 018 18z" transform="rotate(180 10 10)" clip-rule="evenodd" />
                  </svg>
                </div>
                <h4 class="text-lg font-semibold">
                  Return Journey
                  <span class="text-sm font-normal text-gray-500">
                    ({searchParams.destinationLocationCode} to {searchParams.originLocationCode})
                  </span>
                </h4>
              </div>
              
              {#each returnTrip.flights as flight, index}
                <div class="mb-6 {index < returnTrip.flights.length - 1 ? 'border-b border-gray-100 pb-6' : ''}">
                  <div class="flex items-center gap-2 mb-2">
                    <span class="text-xs font-medium text-gray-500">Flight {index + 1} of {returnTrip.flights.length}</span>
                    {#if returnTrip.flights.length > 1 && index < returnTrip.flights.length - 1}
                      <span class="text-xs bg-yellow-100 text-yellow-800 px-2 py-0.5 rounded-full">Connection</span>
                    {/if}
                  </div>
                  
                  <!-- Flight Card -->
                  <div class="bg-gray-50 rounded-lg p-4">
                    <div class="flex justify-between items-center mb-4">
                      <div class="flex items-center">
                        <div class="w-10 h-10 bg-gray-200 rounded-full flex items-center justify-center mr-3">
                          <span class="font-medium">{getAirlineName(flight.carrierCode).substring(0, 2)}</span>
                        </div>
                        <div>
                          <p class="font-medium">{getAirlineName(flight.carrierCode)}</p>
                          <p class="text-xs text-gray-500">Flight {flight.carrierCode}{flight.number}</p>
                        </div>
                      </div>
                      <div class="text-right">
                        <span class="font-medium text-sm">{formatDuration(flight.duration)}</span>
                      </div>
                    </div>
                    
                    <div class="flex">
                      <div class="flex flex-col items-center mr-4">
                        <div class="w-3 h-3 bg-blue-600 rounded-full"></div>
                        <div class="w-0.5 bg-gray-300 flex-grow my-2"></div>
                        <div class="w-3 h-3 bg-green-600 rounded-full"></div>
                      </div>
                      
                      <!-- Flight info -->
                      <div class="flex-grow">
                        <div class="mb-6">
                          <div class="flex justify-between">
                            <p class="text-lg font-semibold">{formatTime(flight.departure.scheduled)}</p>
                            <p class="text-sm text-gray-500">{formatDate(flight.departure.scheduled)}</p>
                          </div>
                          <p class="font-medium">{flight.departure.iata} {getLocationName(flight.departure.iata)}</p>
                          {#if flight.departure.terminal}
                            <p class="text-sm text-gray-600">Terminal {flight.departure.terminal}</p>
                          {/if}
                        </div>
                        
                        <!-- Arrival -->
                        <div>
                          <div class="flex justify-between">
                            <p class="text-lg font-semibold">{formatTime(flight.arrival.scheduled)}</p>
                            <p class="text-sm text-gray-500">{formatDate(flight.arrival.scheduled)}</p>
                          </div>
                          <p class="font-medium">{flight.arrival.iata} {getLocationName(flight.arrival.iata)}</p>
                          {#if flight.arrival.terminal}
                            <p class="text-sm text-gray-600">Terminal {flight.arrival.terminal}</p>
                          {/if}
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  <!-- Connection information -->
                  {#if index < returnTrip.flights.length - 1}
                    {@const nextFlight = returnTrip.flights[index + 1]}
                    {@const connectionDuration = new Date(nextFlight.departure.scheduled) - new Date(flight.arrival.scheduled)}
                    {@const hours = Math.floor(connectionDuration / (1000 * 60 * 60))}
                    {@const minutes = Math.floor((connectionDuration % (1000 * 60 * 60)) / (1000 * 60))}
                    
                    <div class="mt-2 mb-2 flex items-center pl-6">
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-orange-500 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                      <div>
                        <p class="text-sm font-medium text-orange-700">
                          Connection time: {hours > 0 ? `${hours}h ` : ""}{minutes}min at {flight.arrival.iata}
                        </p>
                        <p class="text-xs text-gray-500">
                          You'll need to change planes at {getLocationName(flight.arrival.iata)} Airport
                        </p>
                      </div>
                    </div>
                  {/if}
                </div>
              {/each}
            </div>
          {/if}
          
          <!-- Additional information -->
          <div class="p-4">
            <h4 class="font-medium text-gray-700 mb-3">Additional Information</h4>
            
            <div class="space-y-4">
              <div class="flex items-start">
                <div class="bg-blue-50 p-2 rounded-full mr-3">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                  </svg>
                </div>
                <div>
                  <p class="font-medium">Baggage Allowance</p>
                  <p class="text-sm text-gray-600">{getBaggageAllowance(searchParams.travelClass)}</p>
                </div>
              </div>
              
              <!-- Travel time -->
              <div class="flex items-start">
                <div class="bg-green-50 p-2 rounded-full mr-3">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <div>
                  <p class="font-medium">Total Travel Time</p>
                  <p class="text-sm text-gray-600">
                    Outbound: {formatDuration(outboundTrip.duration)}
                    {#if returnTrip}
                      <br />Return: {formatDuration(returnTrip.duration)}
                    {/if}
                  </p>
                </div>
              </div>
              
              <!-- Operated by -->
              <div class="flex items-start">
                <div class="bg-purple-50 p-2 rounded-full mr-3">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-purple-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z" />
                  </svg>
                </div>
                <div>
                  <p class="font-medium">Operated By</p>
                  <p class="text-sm text-gray-600">
                    {getAirlineName(outboundTrip.flights[0].carrierCode)}
                    {#if returnTrip && returnTrip.flights[0].carrierCode !== outboundTrip.flights[0].carrierCode}
                      <br />Return: {getAirlineName(returnTrip.flights[0].carrierCode)}
                    {/if}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Footer with action buttons -->
        <div class="bg-gray-50 p-4 rounded-b-lg border-t border-gray-200 flex justify-between items-center">
          <button
            class="text-gray-600 hover:text-gray-800 font-medium flex items-center"
            on:click={closeModal}
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M9.707 16.707a1 1 0 01-1.414 0l-6-6a1 1 0 010-1.414l6-6a1 1 0 011.414 1.414L5.414 9H17a1 1 0 110 2H5.414l4.293 4.293a1 1 0 010 1.414z" clip-rule="evenodd" />
            </svg>
            Back
          </button>
          
          <button
            class="bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-6 rounded-lg flex items-center transition-colors"
            on:click={() => {
              closeModal();
              selectOffer(selectedOffer);
            }}
          >
            Continue
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-1" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10.293 3.293a1 1 0 011.414 0l6 6a1 1 0 010 1.414l-6 6a1 1 0 01-1.414-1.414L14.586 11H3a1 1 0 110-2h11.586l-4.293-4.293a1 1 0 010-1.414z" clip-rule="evenodd" />
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
{/if}