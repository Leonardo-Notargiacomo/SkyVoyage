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
    <!-- Departure & Arrival -->
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
          class="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
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
          class="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>
    </div>

    <!-- Dates + Flexible -->
    <div class="flex flex-col md:flex-row gap-4 items-start">
      <div class="flex-1">
        <label class="block text-sm font-medium text-gray-700 mb-1"
          >Departure Date</label
        >
        <input
          type="date"
          bind:value={departureDate}
          required
          class="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
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
          class="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <label class="inline-flex items-center mt-2 text-sm text-gray-600">
          <input type="checkbox" class="mr-2 rounded" /> Flexible (+/- 2 days)
        </label>
      </div>
    </div>

    <!-- Stops + Passengers -->
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
            />
            Any number of stops
          </label>
          <label class="flex items-center gap-2">
            <input
              type="radio"
              name="stops"
              value="Direct"
              bind:group={stops}
              class="accent-blue-600"
            />
            Only direct flights
          </label>
          <label class="flex items-center gap-2">
            <input
              type="radio"
              name="stops"
              value="MaxOneStop"
              bind:group={stops}
              class="accent-blue-600"
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
          class="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          {#each Array(9) as _, i}
            <option value={i + 1}>{i + 1} Passenger{i > 0 ? "s" : ""}</option>
          {/each}
        </select>
      </div>
    </div>

    <!-- Submit -->
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

<!-- Flight Results -->
<div class="mt-6">
  {#if flights.length > 0}
    <div class="relative overflow-x-auto shadow-md sm:rounded-lg">
      <table class="w-full text-sm text-left rtl:text-right text-gray-900">
        <thead class="text-xs text-gray-700 uppercase bg-gray-50">
          <tr>
            <th class="px-6 py-3">Airline</th>
            <th class="px-6 py-3">Departure</th>
            <th class="px-6 py-3">Arrival</th>
            <th class="px-6 py-3">Duration</th>
            <th class="px-6 py-3">Price</th>
            <th class="px-6 py-3">Stops</th>
            <th class="px-6 py-3">Action</th>
          </tr>
        </thead>
        <tbody>
          {#each flights as flight}
            <tr
              class="odd:bg-white even:bg-gray-50 border-gray-200 font-medium"
            >
              <td class="px-6 py-4">{flight.airline}</td>
              <td class="px-6 py-4">{flight.departureTime}</td>
              <td class="px-6 py-4">{flight.arrivalTime}</td>
              <td class="px-6 py-4">{flight.duration}</td>
              <td class="px-6 py-4">${flight.price}</td>
              <td class="px-6 py-4">{flight.stops}</td>
              <td class="px-6 py-4">
                <button class="text-blue-600 hover:underline">Select</button>
              </td>
            </tr>
          {/each}
        </tbody>
      </table>
    </div>
  {:else}
    <p class="text-gray-500 text-center mt-4">No flights found.</p>
  {/if}
</div>
