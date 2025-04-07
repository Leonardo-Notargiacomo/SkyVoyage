<script context="module">
  export const ssr = false;
</script>

<script>
  import { bookingStore } from "$lib/stores/bookingStore";
  import { get } from "svelte/store";
  import { CheckCircle, Users, PlaneTakeoff, Euro } from "lucide-svelte";
  import { goto } from "$app/navigation";

  const booking = get(bookingStore);
  const { flight, customers, discount } = booking;

  // ✅ Prevent direct access with no flight selected
  if (!flight || !customers?.length) {
    goto("/SearchFlights");
  }

  let totalPrice = customers.length * 100 - discount;
  let appliedDiscount = discount;

  function updateDiscount() {
    bookingStore.update((state) => {
      const newTotal = state.customers.length * 100 - appliedDiscount;
      totalPrice = newTotal > 0 ? newTotal : 0;
      return { ...state, discount: appliedDiscount };
    });
  }

  function reserveBooking() {
    alert("Booking reserved. User has 1 month to pay.");
  }

  function confirmBooking() {
    alert("Booking confirmed. User has 1 month to pay.");
  }
</script>

<div class="max-w-4xl mx-auto px-4 py-8">
  <h1 class="text-3xl font-bold text-gray-800 mb-6">Booking Summary</h1>

  <!-- Flight Card -->
  <div class="bg-white shadow-md rounded-xl p-6 mb-6 border">
    <div class="flex items-center gap-3 mb-4">
      <PlaneTakeoff class="text-blue-600" />
      <h2 class="text-xl font-semibold text-blue-700">Flight Information</h2>
    </div>
    <div class="grid grid-cols-2 gap-4 text-gray-700">
      <p><span class="font-medium">From:</span> {flight?.departure}</p>
      <p><span class="font-medium">To:</span> {flight?.arrival}</p>
      <p><span class="font-medium">Departure:</span> {flight?.departureDate}</p>
      <p><span class="font-medium">Arrival:</span> {flight?.arrivalDate}</p>
    </div>
  </div>

  <!-- Passenger Details -->
  <div class="bg-white shadow-md rounded-xl p-6 mb-6 border">
    <div class="flex items-center gap-3 mb-4">
      <Users class="text-blue-600" />
      <h2 class="text-xl font-semibold text-blue-700">Passenger Information</h2>
    </div>
    <div class="space-y-4">
      {#each customers as customer, index}
        <div class="border border-gray-200 rounded-lg p-4 bg-gray-50">
          <p class="font-medium text-gray-800">Passenger {index + 1}</p>
          <p>{customer.firstName} {customer.lastName}</p>
          <p>{customer.email}</p>
          {#if customer.phone}<p>{customer.phone}</p>{/if}
        </div>
      {/each}
    </div>
  </div>

  <!-- Price & Discount -->
  <div class="bg-white shadow-md rounded-xl p-6 mb-6 border">
    <div class="flex items-center gap-3 mb-4">
      <Euro class="text-blue-600" />
      <h2 class="text-xl font-semibold text-blue-700">Price & Discount</h2>
    </div>
    <p class="text-gray-700 mb-2">
      <strong>Base price:</strong>
      {customers.length} × 100€ = {customers.length * 100}€
    </p>
    <div class="flex gap-3 items-center mb-4">
      <input
        type="number"
        bind:value={appliedDiscount}
        min="0"
        class="w-32 border border-gray-300 rounded-md p-2"
        placeholder="Discount (€)"
      />
      <button
        on:click={updateDiscount}
        class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md shadow"
      >
        Apply Discount
      </button>
    </div>
    <p class="text-lg font-semibold text-gray-800">
      Total: <span class="text-green-600">{totalPrice}€</span>
    </p>
  </div>

  <!-- Action Buttons -->
  <div class="flex gap-4 mt-6">
    <button
      on:click={reserveBooking}
      class="w-full bg-yellow-500 hover:bg-yellow-600 text-white font-medium py-2 px-5 rounded-lg shadow-md"
    >
      Reserve
    </button>

    <button
      on:click={confirmBooking}
      class="w-full bg-green-600 hover:bg-green-700 text-white font-medium py-2 px-5 rounded-lg shadow-md"
    >
      Confirm
    </button>
  </div>
</div>
