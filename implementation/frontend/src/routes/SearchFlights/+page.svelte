<script>
  import { onMount } from "svelte";

  let departure = "";
  let arrival = "";
  let departureDate = "";
  let returnDate = "";
  let stops = "Any";
  let passengers = 1;
  let flights = [];

  // Dummy flights
  const dummyFlights = [
    {
      id: "F1",
      departureAirportShort: "FRA",
      arrivalAirportShort: "JFK",
      departureScheduledTime: "2025-03-29T08:45:00",
      arrivalScheduledTime: "2025-03-29T12:00:00",
      duration: 495,
      departureGate: "A12",
      arrivalGate: "B5",
      departureDelay: 10,
      arrivalDelay: 5,
    },
    {
      id: "F2",
      departureAirportShort: "AMS",
      arrivalAirportShort: "LHR",
      departureScheduledTime: "2025-03-30T11:15:00",
      arrivalScheduledTime: "2025-03-30T12:10:00",
      duration: 55,
      departureGate: "B4",
      arrivalGate: "E5",
      departureDelay: 15,
      arrivalDelay: 0,
    },
    {
      id: "F3",
      departureAirportShort: "CDG",
      arrivalAirportShort: "DUB",
      departureScheduledTime: "2025-04-01T14:30:00",
      arrivalScheduledTime: "2025-04-01T15:50:00",
      duration: 80,
      departureGate: "C9",
      arrivalGate: "A3",
      departureDelay: 5,
      arrivalDelay: 2,
    },
    {
      id: "F4",
      departureAirportShort: "MAD",
      arrivalAirportShort: "BCN",
      departureScheduledTime: "2025-04-02T09:10:00",
      arrivalScheduledTime: "2025-04-02T10:25:00",
      duration: 75,
      departureGate: "D1",
      arrivalGate: "B8",
      departureDelay: 0,
      arrivalDelay: 0,
    },
    {
      id: "F5",
      departureAirportShort: "LAX",
      arrivalAirportShort: "SEA",
      departureScheduledTime: "2025-04-03T06:00:00",
      arrivalScheduledTime: "2025-04-03T08:25:00",
      duration: 145,
      departureGate: "F12",
      arrivalGate: "D2",
      departureDelay: 20,
      arrivalDelay: 10,
    },
    {
      id: "F6",
      departureAirportShort: "ZRH",
      arrivalAirportShort: "VIE",
      departureScheduledTime: "2025-04-05T16:45:00",
      arrivalScheduledTime: "2025-04-05T18:15:00",
      duration: 90,
      departureGate: "E1",
      arrivalGate: "C4",
      departureDelay: 0,
      arrivalDelay: 5,
    },
    {
      id: "F7",
      departureAirportShort: "IST",
      arrivalAirportShort: "FCO",
      departureScheduledTime: "2025-04-06T13:00:00",
      arrivalScheduledTime: "2025-04-06T15:45:00",
      duration: 165,
      departureGate: "G3",
      arrivalGate: "B2",
      departureDelay: 30,
      arrivalDelay: 25,
    },
    {
      id: "F8",
      departureAirportShort: "SFO",
      arrivalAirportShort: "ORD",
      departureScheduledTime: "2025-04-07T18:20:00",
      arrivalScheduledTime: "2025-04-07T23:00:00",
      duration: 280,
      departureGate: "C7",
      arrivalGate: "H1",
      departureDelay: 0,
      arrivalDelay: 0,
    },
    {
      id: "F9",
      departureAirportShort: "HEL",
      arrivalAirportShort: "CPH",
      departureScheduledTime: "2025-04-08T07:10:00",
      arrivalScheduledTime: "2025-04-08T08:35:00",
      duration: 85,
      departureGate: "T2",
      arrivalGate: "E7",
      departureDelay: 0,
      arrivalDelay: 3,
    },
    {
      id: "F10",
      departureAirportShort: "MUC",
      arrivalAirportShort: "ATH",
      departureScheduledTime: "2025-04-09T12:00:00",
      arrivalScheduledTime: "2025-04-09T15:10:00",
      duration: 190,
      departureGate: "B1",
      arrivalGate: "D6",
      departureDelay: 12,
      arrivalDelay: 8,
    },
  ];

  let selectedFlight = null;

  onMount(() => {
    // Load dummy flights
    flights = dummyFlights;
  });

  const searchFlights = async (e) => {
    e.preventDefault();
    // Once backend is ready, replace this with API call
    flights = dummyFlights;
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
          >Search Flights</a
        >
      </div>
    </li>
  </ol>
</nav>

<!-- Search Form -->
<div class="bg-white p-6 shadow-md rounded-lg">
  <h2 class="text-xl font-semibold mb-4">Search for Flights</h2>
  <form on:submit={searchFlights} class="space-y-4">
    <div class="flex flex-col md:flex-row gap-4">
      <div class="flex-1">
        <label class="block text-sm font-medium text-gray-700 mb-1"
          >Departure Airport</label
        >
        <input
          type="text"
          bind:value={departure}
          placeholder="e.g. FRA"
          class="w-full p-2 border border-gray-300 rounded-md"
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
          class="w-full p-2 border border-gray-300 rounded-md"
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
          class="w-full p-2 border border-gray-300 rounded-md"
        />
        <label class="inline-flex items-center mt-2 text-sm text-gray-600">
          <input type="checkbox" class="mr-2" /> Flexible (+/- 2 days)
        </label>
      </div>
      <div class="flex-1">
        <label class="block text-sm font-medium text-gray-700 mb-1"
          >Return Date (optional)</label
        >
        <input
          type="date"
          bind:value={returnDate}
          class="w-full p-2 border border-gray-300 rounded-md"
        />
        <label class="inline-flex items-center mt-2 text-sm text-gray-600">
          <input type="checkbox" class="mr-2" /> Flexible (+/- 2 days)
        </label>
      </div>
    </div>

    <div class="flex flex-col md:flex-row gap-4 items-start">
      <div class="flex-1">
        <label class="block text-sm font-medium text-gray-700 mb-1"
          >Flight Type</label
        >
        <div class="flex flex-col gap-1 text-sm">
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
        <label class="block text-sm font-medium text-gray-700 mb-1"
          >Passengers</label
        >
        <select
          bind:value={passengers}
          class="w-full p-2 border border-gray-300 rounded-md"
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
        class="w-full bg-blue-600 text-white py-2 rounded-md hover:bg-blue-700 transition"
      >
        Search Flights
      </button>
    </div>
  </form>
</div>

<!-- Flight Cards -->
<div class="mt-6 flex flex-wrap gap-4">
  {#each flights as flight}
    <div
      class="bg-white border border-blue-100 shadow-md rounded-md p-4 cursor-pointer hover:shadow-lg transition duration-300 w-full md:w-[48%]"
      on:click={() => (selectedFlight = flight)}
    >
      <h3 class="text-blue-600 text-sm font-semibold">
        {flight.departureAirportShort} → {flight.arrivalAirportShort}
      </h3>
      <p class="text-gray-800 text-sm">
        {formatDateTime(flight.departureScheduledTime)} – {formatDateTime(
          flight.arrivalScheduledTime
        )}
      </p>
      <p class="text-gray-600 text-sm">
        Duration: {formatDuration(flight.duration)}
      </p>
    </div>
  {/each}
</div>

<!-- Modal -->
{#if selectedFlight}
  <div class="fixed inset-0 flex items-center justify-center z-50">
    <div
      class="absolute inset-0 bg-black/40 backdrop-blur-sm"
      on:click={() => (selectedFlight = null)}
    ></div>
    <div class="bg-white p-6 rounded-lg z-50 shadow-xl max-w-md w-full">
      <div class="flex justify-between items-center mb-2">
        <h3 class="text-blue-600 font-bold text-lg">
          {selectedFlight.departureAirportShort} → {selectedFlight.arrivalAirportShort}
        </h3>
        <button
          on:click={() => (selectedFlight = null)}
          class="text-gray-500 hover:text-black">&times;</button
        >
      </div>
      <p>
        <strong>Departure:</strong>
        {formatDateTime(selectedFlight.departureScheduledTime)}
      </p>
      <p>
        <strong>Arrival:</strong>
        {formatDateTime(selectedFlight.arrivalScheduledTime)}
      </p>
      <p>
        <strong>Duration:</strong>
        {formatDuration(selectedFlight.duration)}
      </p>
      <p>
        <strong>Gate:</strong>
        {selectedFlight.departureGate} → {selectedFlight.arrivalGate}
      </p>
      <p>
        <strong>Delays:</strong>
        {selectedFlight.departureDelay} min dep. / {selectedFlight.arrivalDelay}
        min arr.
      </p>
    </div>
  </div>
{/if}
