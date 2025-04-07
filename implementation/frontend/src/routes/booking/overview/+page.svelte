<script>
  import { bookingStore } from "../../../lib/stores/bookingStore";
  import { get } from "svelte/store";

  const booking = get(bookingStore);
  const { flight, passengers, customers, discount } = booking;

  const basePricePerPerson = 100;
  const basePrice = passengers * basePricePerPerson;
  const finalPrice = Math.max(0, basePrice - discount);
</script>

<div class="max-w-4xl mx-auto mt-8 space-y-6">
  <h1 class="text-2xl font-semibold text-gray-800">Booking Summary</h1>

  <div class="border rounded-lg p-6 shadow-sm bg-white">
    <h2 class="text-lg font-medium text-blue-600 mb-4">Flight Information</h2>
    <div class="grid grid-cols-2 gap-4 text-gray-700">
      <div><strong>From:</strong> {flight?.from || "—"}</div>
      <div><strong>To:</strong> {flight?.to || "—"}</div>
      <div><strong>Departure Date:</strong> {flight?.departure || "—"}</div>
      <div><strong>Arrival Date:</strong> {flight?.arrival || "—"}</div>
    </div>
  </div>

  <div class="border rounded-lg p-6 shadow-sm bg-white">
    <h2 class="text-lg font-medium text-blue-600 mb-4">
      Passenger Information
    </h2>
    {#each customers as customer, i}
      <div class="mb-4">
        <p><strong>Passenger {i + 1}:</strong></p>
        <p>{customer.firstName} {customer.lastName}</p>
        <p>{customer.email}</p>
        {#if customer.phone}
          <p>{customer.phone}</p>
        {/if}
      </div>
    {/each}
  </div>

  <div class="border rounded-lg p-6 shadow-sm bg-white">
    <h2 class="text-lg font-medium text-blue-600 mb-4">Price & Discount</h2>
    <p>
      <strong>Base price:</strong>
      {passengers} × {basePricePerPerson}€ = {basePrice}€
    </p>
    <p><strong>Discount:</strong> {discount}€</p>
    <p class="text-xl font-semibold mt-2">
      <strong>Total:</strong>
      {finalPrice}€
    </p>
  </div>
</div>
