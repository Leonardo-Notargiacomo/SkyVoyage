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
</script>

<div class="max-w-4xl mx-auto p-6">
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
