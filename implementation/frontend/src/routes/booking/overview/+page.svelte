<script>
  import { bookingStore } from "$lib/stores/bookingStore";
  import { goto } from "$app/navigation";
  import DankMode from "$lib/components/DankMode.svelte";

  let discountInput;
  let discountReason = "";
  let isDank = false;

  // Reactive values from store
  $: booking = $bookingStore;
  $: discountInput = booking.discount;
  $: isDank = booking.discount === 69;

  const formatDateTime = (date) =>
          new Date(date).toLocaleString("en-GB", {
            day: "2-digit",
            month: "2-digit",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit",
          });

  const formatDuration = (mins) => {
    if (!mins) return "—";
    return mins < 60 ? `${mins}min` : `${Math.floor(mins / 60)}h ${mins % 60}min`;
  };

  function applyDiscount() {
    const parsed = parseInt(discountInput);
    if (!isNaN(parsed) && parsed >= 0 && parsed <= 100) {
      bookingStore.update((b) => ({
        ...b,
        discount: parsed,
        discountReason: discountReason.trim(),
      }));
    }
  }

  const discountedAmount = () =>
          (booking.flight.price * booking.passengers * booking.discount) / 100;

  const finalPrice = () =>
          (booking.flight.price * booking.passengers - discountedAmount()).toFixed(2);

  function confirmBooking() {
    sessionStorage.setItem("confirmedBooking", JSON.stringify($bookingStore));
    alert("Booking confirmed! ✅");
    goto("/home");
  }

  function reserveBooking() {
    sessionStorage.setItem("reservedBooking", JSON.stringify($bookingStore));
    alert("Booking reserved for later payment! ⏳");
    goto("/home");
  }

  function cancelBooking() {
    bookingStore.set({
      flight: null,
      passengers: 1,
      customers: [],
      discount: 0,
    });
    alert("Booking cancelled.");
    goto("/home");
  }
</script>

<div class={`max-w-7xl mx-auto px-4 py-8 ${isDank ? "dank-mode" : ""}`}>
  <h1 class="text-2xl font-bold mb-8 text-center">Booking Summary</h1>

  {#if isDank}
    <DankMode />
  {/if}

  <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
    <div class={`lg:col-span-2 space-y-6 ${isDank ? "shake" : ""}`}>
      <!-- Flight Info -->
      <div class="border p-4 rounded-lg shadow card">
        <h2 class="text-blue-600 font-semibold text-lg mb-2">Flight Overview</h2>
        <div class="flex justify-between text-sm">
          <div>
            <p class="text-gray-500">Airline</p>
            <p class="font-medium">{booking.flight.airline}</p>
            <p class="text-gray-500 mt-2">Flight Number</p>
            <p class="font-medium">{booking.flight.id}</p>
          </div>
          <div class="text-right">
            <p class="text-gray-500">Price</p>
            <p class="font-medium text-blue-600">€{booking.flight.price}</p>
            <p class="text-gray-500 mt-2">Status</p>
            <p class="font-medium">{booking.flight.status}</p>
          </div>
        </div>
        <hr class="my-4" />
        <div class="flex justify-between text-sm">
          <div>
            <p class="text-gray-500">From</p>
            <p class="font-semibold">
              {booking.flight.departure.airport} ({booking.flight.departure.iata})
            </p>
            <p class="text-sm">
              Terminal: {booking.flight.departure.terminal || "TBA"}, Gate: {booking.flight.departure.gate || "TBA"}
            </p>
            <p class="text-sm">Departure: {formatDateTime(booking.flight.departure.scheduled)}</p>
          </div>
          <div class="text-right">
            <p class="text-gray-500">To</p>
            <p class="font-semibold">
              {booking.flight.arrival.airport} ({booking.flight.arrival.iata})
            </p>
            <p class="text-sm">
              Terminal: {booking.flight.arrival.terminal || "TBA"}, Gate: {booking.flight.arrival.gate || "TBA"}
            </p>
            <p class="text-sm">Arrival: {formatDateTime(booking.flight.arrival.scheduled)}</p>
          </div>
        </div>
        <p class="text-xs text-gray-500 mt-2">Duration: {formatDuration(booking.flight.duration)}</p>
      </div>

      <!-- Passenger Info -->
      <div class="border p-4 rounded-lg shadow card">
        <h2 class="text-blue-600 font-semibold text-sm mb-2">Passenger Information</h2>

        {#if booking.customers.length > 0}
          <div class="space-y-2 text-sm">
            <!-- Adult Passengers -->
            <h3 class="text-gray-700 font-semibold mb-1">Adults</h3>
            {#each booking.customers.slice(0, booking.AdultPassengers) as c, index}
              <div class="mb-1">
                <p><strong>Passenger {index + 1}:</strong> {c.firstName} {c.lastName} <span class="text-gray-500">(Adult)</span></p>
                <p>{c.email}</p>
                {#if c.phone}<p>{c.phone}</p>{/if}
              </div>
            {/each}

            <!-- Infant Passengers -->
            {#if booking.customers.length > booking.AdultPassengers}
              <h3 class="text-gray-700 font-semibold mt-4 mb-1">Infants</h3>
              {#each booking.customers.slice(booking.AdultPassengers) as c, index}
                <div class="mb-1">
                  <p><strong>Passenger {booking.AdultPassengers + index + 1}:</strong> {c.firstName} {c.lastName} <span class="text-gray-500">(Infant)</span></p>
                </div>
              {/each}
            {/if}
          </div>
        {:else}
          <p class="text-sm text-gray-500">No passenger information available.</p>
        {/if}
      </div>


      <!-- Price & Discount -->
      <div class="border p-4 rounded-lg shadow card">
        <h2 class="text-blue-600 font-semibold text-sm mb-2">Price & Discount</h2>
        <p>
          <strong>Base price:</strong> {booking.passengers} × €{booking.flight.price} =
          €{(booking.passengers * booking.flight.price).toFixed(2)}
        </p>
        <p>
          <strong>Discount:</strong> {booking.discount}% = − €{discountedAmount().toFixed(2)}
        </p>
        <p><strong class="text-lg">Total:</strong> €{finalPrice()}</p>
      </div>
    </div>

    <!-- Side Actions -->
    <div class={`border p-4 rounded-lg shadow card h-fit space-y-4 ${isDank ? "shake" : ""}`}>
      <h2 class="text-blue-600 font-semibold text-md mb-2">Complete Your Booking</h2>

      <label for="discount" class="text-sm text-gray-700">Discount (%)</label>
      <input
              type="number"
              min="0"
              max="100"
              bind:value={discountInput}
              placeholder="e.g. 10"
              class="w-full p-2 border border-gray-300 rounded-md"
      />
      <button
              on:click={applyDiscount}
              class="w-full bg-gray-700 hover:bg-gray-800 text-white font-medium py-2 rounded-md"
      >
        Apply Discount
      </button>

      <label for="reason" class="text-sm text-gray-700">Reason for Discount</label>
      <input
              type="text"
              bind:value={discountReason}
              placeholder="e.g. loyal customer"
              class="w-full p-2 border border-gray-300 rounded-md"
      />

      <button
              on:click={confirmBooking}
              class="w-full bg-blue-600 hover:bg-blue-700 text-white py-2 rounded-md font-medium"
      >
        Confirm Booking
      </button>

      <button
              on:click={reserveBooking}
              class="w-full bg-yellow-400 hover:bg-yellow-500 text-black py-2 rounded-md font-medium"
      >
        Reserve (Pay Later)
      </button>

      <button
              on:click={cancelBooking}
              class="w-full bg-red-100 hover:bg-red-200 text-red-600 py-2 rounded-md font-medium"
      >
        Cancel Booking
      </button>
    </div>
  </div>
</div>

<style>
  .dank-mode {
    background: #111 !important;
    color: #0ff !important;
    transition: all 0.5s ease-in-out;
  }

  .shake {
    animation: shake 0.6s infinite;
  }

  @keyframes shake {
    0% { transform: translateX(0); }
    25% { transform: translateX(-3px); }
    50% { transform: translateX(3px); }
    75% { transform: translateX(-2px); }
    100% { transform: translateX(0); }
  }

  .card {
    transition: all 0.3s ease;
  }
</style>
