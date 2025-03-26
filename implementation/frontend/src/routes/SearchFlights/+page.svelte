<script>
  import { onMount } from "svelte";
  import { api } from "$lib/api";

  let departure = "";
  let arrival = "";
  let departureDate = "";
  let returnDate = "";
  let stops = "Any";
  let passengers = 1;
  let flights = [];

  // Dummy data for testing
  flights = [
    {
      id: "1",
      departureAirport: "Frankfurt International",
      departureAirportShort: "FRA",
      departureScheduledTime: "2025-03-29T08:45:00",
      departureDelay: 0,
      departureGate: "A3",
      departureTerminal: "1",
      arrivalAirport: "New York JFK",
      arrivalAirportShort: "JFK",
      arrivalScheduledTime: "2025-03-29T12:00:00",
      arrivalDelay: 10,
      arrivalGate: "C2",
      arrivalTerminal: "4",
      duration: 495,
    },
    {
      id: "2",
      departureAirport: "Amsterdam Schiphol",
      departureAirportShort: "AMS",
      departureScheduledTime: "2025-03-30T11:15:00",
      departureDelay: 15,
      departureGate: "B4",
      departureTerminal: "2",
      arrivalAirport: "London Heathrow",
      arrivalAirportShort: "LHR",
      arrivalScheduledTime: "2025-03-30T12:10:00",
      arrivalDelay: 0,
      arrivalGate: "E5",
      arrivalTerminal: "5",
      duration: 55,
    },
  ];

  onMount(async () => {
    loadFlights();
  });

  const loadFlights = async () => {
    const params = new URLSearchParams({
      departure,
      arrival,
      departureDate,
      returnDate,
      stops,
      passengers,
    });
    flights = (await api.all(`/flights/search?${params.toString()}`)) || [];
  };

  const searchFlights = async (e) => {
    e.preventDefault();
    await loadFlights();
  };

  function formatDateTime(dt) {
    if (!dt) return "-";
    const date = new Date(dt);
    return (
      date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }) +
      " · " +
      date.toLocaleDateString()
    );
  }

  function formatDuration(minutes) {
    const h = Math.floor(minutes / 60);
    const m = minutes % 60;
    return `${h}h ${m}m`;
  }
</script>

<!-- Breadcrumb Navigation -->
<nav class="flex mt-2 mb-2" aria-label="Breadcrumb">
  <ol
    class="inline-flex items-center space-x-1 md:space-x-2 rtl:space-x-reverse"
  >
    <li class="inline-flex items-center">
      <a
        href="/"
        class="inline-flex items-center text-sm font-medium text-gray-700 hover:text-blue-600"
      >
        <svg
          class="w-3 h-3 me-2.5"
          xmlns="http://www.w3.org/2000/svg"
          fill="currentColor"
          viewBox="0 0 20 20"
        >
          <path
            d="m19.707 9.293-2-2-7-7a1 1 0 0 0-1.414 0l-7 7-2 2a1 1 0 0 0 1.414 1.414L2 10.414V18a2 2 0 0 0 2 2h3a1 1 0 0 0 1-1v-4a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1v4a1 1 0 0 0 1 1h3a2 2 0 0 0 2-2v-7.586l.293.293a1 1 0 0 0 1.414-1.414Z"
          />
        </svg>
        Home
      </a>
    </li>
    <li>
      <div class="flex items-center">
        <svg
          class="rtl:rotate-180 w-3 h-3 text-gray-400 mx-1"
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
        <a
          href="/flights/search"
          class="ms-1 text-sm font-medium text-gray-700 hover:text-blue-600 md:ms-2"
        >
          Search Flights
        </a>
      </div>
    </li>
  </ol>
</nav>

<!-- Search Form -->
<div class="bg-gray-50 p-6 shadow-md rounded-xl">
  <h2 class="text-xl font-semibold mb-4">Search for Flights</h2>
  <form on:submit={searchFlights} class="space-y-6">
    <div class="flex flex-col md:flex-row gap-4">
      <div class="flex-1">
        <label class="block text-sm font-medium text-gray-700 mb-1"
          >Departure Airport</label
        >
        <input
          type="text"
          bind:value={departure}
          placeholder="e.g. FRA"
          required
          class="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>
      <div class="flex-1">
        <label class="block text-sm font-medium text-gray-700 mb-1"
          >Arrival Airport</label
        >
        <input
          type="text"
          bind:value={arrival}
          placeholder="e.g. JFK"
          required
          class="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>
    </div>

    <div class="flex flex-col md:flex-row gap-4 items-start">
      <div class="flex-1">
        <label class="block text-sm font-medium text-gray-700 mb-1"
          >Departure Date</label
        >
        <input
          type="date"
          bind:value={departureDate}
          required
          class="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <label class="inline-flex items-center mt-2 text-sm text-gray-600">
          <input type="checkbox" class="mr-2 rounded" /> Flexible (+/- 2 days)
        </label>
      </div>
      <div class="flex-1">
        <label class="block text-sm font-medium text-gray-700 mb-1"
          >Return Date (optional)</label
        >
        <input
          type="date"
          bind:value={returnDate}
          class="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <label class="inline-flex items-center mt-2 text-sm text-gray-600">
          <input type="checkbox" class="mr-2 rounded" /> Flexible (+/- 2 days)
        </label>
      </div>
    </div>

    <div class="flex flex-col md:flex-row gap-4 items-start">
      <div class="flex-1">
        <label class="block text-sm font-medium text-gray-700 mb-1"
          >Flight Type</label
        >
        <div class="flex flex-col gap-1 text-sm text-gray-700">
          <label class="flex items-center gap-2">
            <input
              type="radio"
              name="stops"
              value="Any"
              bind:group={stops}
              checked
              class="accent-blue-600"
            /> Any number of stops
          </label>
          <label class="flex items-center gap-2">
            <input
              type="radio"
              name="stops"
              value="Direct"
              bind:group={stops}
              class="accent-blue-600"
            /> Only direct flights
          </label>
          <label class="flex items-center gap-2">
            <input
              type="radio"
              name="stops"
              value="MaxOneStop"
              bind:group={stops}
              class="accent-blue-600"
            /> Max. one stop
          </label>
        </div>
      </div>
      <div class="flex-1">
        <label class="block text-sm font-medium text-gray-700 mb-1"
          >Passengers</label
        >
        <select
          bind:value={passengers}
          class="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          {#each Array(9) as _, i}
            <option value={i + 1}>{i + 1} Passenger{i > 0 ? "s" : ""}</option>
          {/each}
        </select>
      </div>
    </div>

    <div class="pt-2">
      <button
        type="submit"
        class="w-full bg-blue-600 text-white py-2 rounded-md font-semibold shadow-sm hover:bg-blue-700 transition duration-200"
      >
        Search Flights
      </button>
    </div>
  </form>
</div>

<!-- Flight Cards Grid -->
<div class="mt-8">
  {#if flights.length > 0}
    <div class="grid gap-6 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3">
      {#each flights as flight}
        <div class="bg-white rounded-xl shadow-md p-5 border border-gray-200">
          <div class="mb-2 text-lg font-semibold text-blue-600">
            {flight.departureAirportShort} → {flight.arrivalAirportShort}
          </div>
          <div class="text-sm text-gray-700 mb-1">
            {flight.departureAirport} → {flight.arrivalAirport}
          </div>
          <div class="text-sm text-gray-700">
            Departure: <strong
              >{formatDateTime(flight.departureScheduledTime)}</strong
            >
          </div>
          <div class="text-sm text-gray-700">
            Arrival: <strong
              >{formatDateTime(flight.arrivalScheduledTime)}</strong
            >
          </div>
          <div class="text-sm text-gray-700 mt-1">
            Duration: <strong>{formatDuration(flight.duration)}</strong>
          </div>
          {#if flight.departureDelay || flight.arrivalDelay}
            <div class="text-sm text-red-500 mt-1">
              Delay: {flight.departureDelay || 0} min dep / {flight.arrivalDelay ||
                0} min arr
            </div>
          {/if}
          <button
            class="mt-4 w-full py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 text-sm"
          >
            Select
          </button>
        </div>
      {/each}
    </div>
  {:else}
    <p class="text-gray-500 text-center mt-4">No flights found.</p>
  {/if}
</div>
