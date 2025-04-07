<script>
  import { bookingStore } from "$lib/stores/bookingStore";
  import { get } from "svelte/store";
  import { goto } from "$app/navigation";

  let { flight, passengers, customers, discount } = get(bookingStore);
  let discountInput = discount;
  let discountReason = "";
  let isDank = false;

  function formatDateTime(date) {
    const dt = new Date(date);
    return dt.toLocaleString("en-GB", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  }

  function formatDuration(mins) {
    if (!mins) return "—";
    return mins < 60
      ? `${mins}min`
      : `${Math.floor(mins / 60)}h ${mins % 60}min`;
  }

  function applyDiscount() {
    const parsed = parseInt(discountInput);
    if (!isNaN(parsed) && parsed >= 0 && parsed <= 100) {
      bookingStore.update((b) => ({ ...b, discount: parsed }));
      isDank = parsed === 69;
    }
    bookingStore.update((b) => ({
      ...b,
      discount: parsed,
      discountReason: discountReason.trim(),
    }));
  }

  const discountedAmount = () => {
    return (flight.price * passengers * discount) / 100;
  };

  const finalPrice = () => {
    return (flight.price * passengers - discountedAmount()).toFixed(2);
  };

  function confirmBooking() {
    const booking = get(bookingStore);
    sessionStorage.setItem("confirmedBooking", JSON.stringify(booking));
    alert("Booking confirmed! ✅");
    goto("/home");
  }

  function reserveBooking() {
    const booking = get(bookingStore);
    sessionStorage.setItem("reservedBooking", JSON.stringify(booking));
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
    <div class="text-center mb-6 dank-banner">
      <p class="rainbow-text text-2xl mb-4">😎 69% Discount Activated 😎</p>

      <!-- Center TURKIYE -->
      <img
        src="https://media.discordapp.net/attachments/1338828511682498643/1358750787659894804/pala-baba.gif?ex=67f4fac8&is=67f3a948&hm=462ff63b17910d0066714d05a312e735db26814dd6a9f25b9472189f3ac423eb&=&width=330&height=708"
        alt="Turkiye"
        class="turkiye"
      />

      <!-- Flying Memes -->
      <img
        src="https://media2.giphy.com/media/v1.Y2lkPTc5MGI3NjExZWljajB0MzVyYjY4ZzYxM244YWVzaTVzZjZxNG45c2Q2MWh2MDU3OSZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/XqPOScfuIgn1C/giphy.gif"
        class="fly-gif one"
      />
      <img
        src="https://media.giphy.com/media/v9OLgAL8p8FOM/giphy.gif?cid=790b7611r2x7y06b18ksecmxdr1lotv3yiv6arsbaenpxnga&ep=v1_gifs_search&rid=giphy.gif&ct=g"
        class="fly-gif two"
      />
      <img
        src="https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExcHZwY3E4bmI5aGlldXZvbjJycHNjeWx3dGoyNnVpd2wzcGs5cTZzYiZlcD12MV9naWZzX3NlYXJjaCZjdD1n/3o6ZtdUnbbEEXexdGE/giphy.gif"
        class="fly-gif three"
      />
      <img
        src="/Barrelino-Sheeparino.png"
        alt="Barrelino Sheeparino"
        class="barrelino"
      />
    </div>
  {/if}

  <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
    <div class={`lg:col-span-2 space-y-6 ${isDank ? "shake" : ""}`}>
      <!-- Flight Info -->
      <div class="border p-4 rounded-lg shadow card">
        <h2 class="text-blue-600 font-semibold text-lg mb-2">
          Flight Overview
        </h2>
        <div class="flex justify-between text-sm">
          <div>
            <p class="text-gray-500">Airline</p>
            <p class="font-medium">{flight.airline}</p>
            <p class="text-gray-500 mt-2">Flight Number</p>
            <p class="font-medium">{flight.id}</p>
          </div>
          <div class="text-right">
            <p class="text-gray-500">Price</p>
            <p class="font-medium text-blue-600">€{flight.price}</p>
            <p class="text-gray-500 mt-2">Status</p>
            <p class="font-medium">{flight.status}</p>
          </div>
        </div>
        <hr class="my-4" />
        <div class="flex justify-between text-sm">
          <div>
            <p class="text-gray-500">From</p>
            <p class="font-semibold">
              {flight.departure.airport} ({flight.departure.iata})
            </p>
            <p class="text-sm">
              Terminal: {flight.departure.terminal || "TBA"}, Gate:
              {flight.departure.gate || "TBA"}
            </p>
            <p class="text-sm">
              Departure: {formatDateTime(flight.departure.scheduled)}
            </p>
          </div>
          <div class="text-right">
            <p class="text-gray-500">To</p>
            <p class="font-semibold">
              {flight.arrival.airport} ({flight.arrival.iata})
            </p>
            <p class="text-sm">
              Terminal: {flight.arrival.terminal || "TBA"}, Gate:
              {flight.arrival.gate || "TBA"}
            </p>
            <p class="text-sm">
              Arrival: {formatDateTime(flight.arrival.scheduled)}
            </p>
          </div>
        </div>
        <p class="text-xs text-gray-500 mt-2">
          Duration: {formatDuration(flight.duration)}
        </p>
      </div>

      <!-- Passenger Info -->
      <div class="border p-4 rounded-lg shadow card">
        <h2 class="text-blue-600 font-semibold text-sm mb-2">
          Passenger Information
        </h2>
        {#each customers as c, index}
          <div class="text-sm mb-2">
            <p class="font-semibold">Passenger {index + 1}:</p>
            <p>{c.firstName} {c.lastName}</p>
            <p>{c.email}</p>
            {#if c.phone}<p>{c.phone}</p>{/if}
          </div>
        {/each}
      </div>

      <!-- Price & Discount -->
      <div class="border p-4 rounded-lg shadow card">
        <h2 class="text-blue-600 font-semibold text-sm mb-2">
          Price & Discount
        </h2>
        <p>
          <strong>Base price:</strong>
          {passengers} × €{flight.price} = €{(
            passengers * flight.price
          ).toFixed(2)}
        </p>
        <p>
          <strong>Discount:</strong>
          {discount}% = − €{discountedAmount().toFixed(2)}
        </p>
        <p><strong class="text-lg">Total:</strong> €{finalPrice()}</p>
      </div>
    </div>

    <!-- Side Actions -->
    <div
      class={`border p-4 rounded-lg shadow card h-fit space-y-4 ${isDank ? "shake" : ""}`}
    >
      <h2 class="text-blue-600 font-semibold text-md mb-2">
        Complete Your Booking
      </h2>

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

      <label for="reason" class="text-sm text-gray-700"
        >Reason for Discount</label
      >
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

  .rainbow-text {
    font-size: 2rem;
    font-weight: bold;
    background: linear-gradient(
      to right,
      red,
      orange,
      yellow,
      green,
      blue,
      indigo,
      violet
    );
    -webkit-background-clip: text;
    color: transparent;
    animation: rainbow 2s infinite linear;
  }

  @keyframes rainbow {
    0% {
      filter: hue-rotate(0deg);
    }
    100% {
      filter: hue-rotate(360deg);
    }
  }

  .shake {
    animation: shake 0.6s infinite;
  }

  @keyframes shake {
    0% {
      transform: translateX(0);
    }
    25% {
      transform: translateX(-3px);
    }
    50% {
      transform: translateX(3px);
    }
    75% {
      transform: translateX(-2px);
    }
    100% {
      transform: translateX(0);
    }
  }

  .card {
    transition: all 0.3s ease;
  }
  .dank-banner {
    animation: fadeIn 1s ease-in-out;
  }

  @keyframes fadeIn {
    from {
      opacity: 0;
      transform: scale(0.95);
    }
    to {
      opacity: 1;
      transform: scale(1);
    }
  }

  .dank-mode {
    background: linear-gradient(to right, #111, #222);
    color: #0ff;
    transition: all 0.5s ease-in-out;
  }

  .rainbow-text {
    font-size: 2rem;
    font-weight: bold;
    background: linear-gradient(
      to right,
      red,
      orange,
      yellow,
      green,
      blue,
      indigo,
      violet
    );
    -webkit-background-clip: text;
    color: transparent;
    animation: rainbow 2s infinite linear;
  }

  @keyframes rainbow {
    0% {
      filter: hue-rotate(0deg);
    }
    100% {
      filter: hue-rotate(360deg);
    }
  }

  .turkiye {
    width: 180px;
    margin: 0 auto;
    border-radius: 12px;
    box-shadow: 0 0 15px limegreen;
    animation: popIn 1s ease-in-out;
  }

  .fly-gif {
    position: fixed;
    width: 100px;
    z-index: 50;
    opacity: 0.8;
    animation: float 10s linear infinite;
    pointer-events: none;
  }

  .fly-gif.one {
    top: 10%;
    left: -150px;
    animation-delay: 0s;
  }

  .fly-gif.two {
    top: 40%;
    left: -200px;
    animation-delay: 2s;
  }

  .fly-gif.three {
    top: 70%;
    left: -180px;
    animation-delay: 4s;
  }

  @keyframes float {
    0% {
      transform: translateX(0) rotate(0deg);
      opacity: 0.7;
    }
    50% {
      transform: translateX(110vw) rotate(360deg);
      opacity: 1;
    }
    100% {
      transform: translateX(0) rotate(720deg);
      opacity: 0.7;
    }
  }

  @keyframes popIn {
    from {
      opacity: 0;
      transform: scale(0.7);
    }
    to {
      opacity: 1;
      transform: scale(1);
    }
  }
  .barrelino {
    position: fixed;
    top: 16px;
    right: 32px;
    width: 240px;
    height: auto;
    z-index: 1000;
    pointer-events: none; /* so it doesn't block clicks */
  }
</style>
