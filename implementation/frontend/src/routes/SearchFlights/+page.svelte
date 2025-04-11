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
  let carriers = {};
  let locations = {};

  onMount(async () => {
    // Parse search parameters from URL
    const urlParams = $page.url.searchParams;

    searchParams = {
      originLocationCode: urlParams.get("originLocationCode") || "",
      destinationLocationCode: urlParams.get("destinationLocationCode") || "",
      departureDate: urlParams.get("departureDate") || "",
      returnDate: urlParams.get("returnDate") || "",
      adults: urlParams.get("adults") || "1",
      travelClass: urlParams.get("travelClass") || "ECONOMY",
      nonStop: urlParams.get("nonStop") === "true",
      max: urlParams.get("max") || "5",
    };

    await searchFlights();
  });

  async function searchFlights() {
    loading = true;
    error = null;

    try {
      // Build API endpoint with query parameters
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

        // Store carriers and locations dictionaries if available
        if (response.dictionaries) {
          carriers = response.dictionaries.carriers || {};
          locations = response.dictionaries.locations || {};
        }

        if (flightOffers.length === 0) {
          error =
            "No flights found matching your criteria. Please try a different search.";
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

    // Parse ISO 8601 duration format (PT2H30M)
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
    // Format flight data for booking store
    const outboundTrip = offer.trips.find((trip) => trip.type === "outbound");
    const returnTrip = offer.trips.find((trip) => trip.type === "return");

    // Get first flight of outbound for departure details
    const firstOutboundFlight = outboundTrip?.flights[0];

    // Get last flight of outbound for arrival details
    const lastOutboundFlight =
      outboundTrip?.flights[outboundTrip.flights.length - 1];

    // Prepare a flight object in the format expected by the booking system
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

      // Store full offer data for reference
      fullOffer: offer,
    };

    // Save to booking store
    bookingStore.update((store) => ({
      ...store,
      flight: flightData,
      AdultPassengers: parseInt(searchParams.adults),
      infantsPassengers: parseInt(searchParams.infants) || 0,
      travelClass: searchParams.travelClass,
    }));

    // Go to customer details page
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
          fill="currentColor"
          viewBox="0 0 20 20"
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
  <div class="flex justify-between items-center mb-8">
    <h1 class="text-3xl font-bold text-gray-800">Flight Search Results</h1>
    <a
      href="/home"
      class="bg-blue-50 hover:bg-blue-100 text-blue-600 font-medium py-2 px-4 rounded-md transition-all"
    >
      New Search
    </a>
  </div>

  <!-- Search summary -->
  <div class="bg-gray-50 p-4 rounded-md mb-6 shadow-sm">
    <p class="text-gray-700">
      <strong>From:</strong>
      {searchParams.originLocationCode}
      <strong class="ml-4">To:</strong>
      {searchParams.destinationLocationCode}
      <strong class="ml-4">Date:</strong>
      {searchParams.departureDate}
      {#if searchParams.returnDate}
        <strong class="ml-4">Return:</strong> {searchParams.returnDate}
      {/if}
      <strong class="ml-4">Passengers:</strong>
      {searchParams.adults}
      <strong class="ml-4">Class:</strong>
      {searchParams.travelClass}
    </p>
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
              <span class="text-sm text-gray-500">Total price</span>
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
                <!-- First and last points of trip -->
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
                        ? `Terminal ${outboundTrip.flights[0].departure.terminal}`
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
                        ? `Terminal ${outboundTrip.flights[outboundTrip.flights.length - 1].arrival.terminal}`
                        : ""}
                    </p>
                  </div>
                </div>

                <!-- Connection details if more than one flight -->
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
                <!-- First and last points of return trip -->
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
                        ? `Terminal ${returnTrip.flights[0].departure.terminal}`
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
                        ? `Terminal ${returnTrip.flights[returnTrip.flights.length - 1].arrival.terminal}`
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
              on:click={() => selectOffer(offer)}
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
