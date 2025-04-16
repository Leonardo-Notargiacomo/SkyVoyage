<script>
  import { goto } from "$app/navigation";
  import { bookingStore } from "$lib/stores/bookingStore";
  import { get } from "svelte/store";

  const booking = get(bookingStore);
  let flight = booking.flight;
  let passengers = booking.AdultPassengers + booking.infantsPassengers || 1;

  // Redirect if no flight selected
  if (!flight) {
    goto("/SearchFlights");
  }

  let customers = Array.from({ length: passengers }, () => ({
    firstName: "",
    lastName: "",
    email: "",
    phone: "",
    street: "",
    houseNumber: "",
    city: "",
    country: "",
    isInfant: false,
  }));

  function continueToSummary() {
    // Save customers to sessionStorage
    sessionStorage.setItem("customers", JSON.stringify(customers));

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
  <!-- Passenger Form -->
  <h1 class="text-2xl font-semibold mb-6 text-gray-800">
    Enter Passenger Details
  </h1>

  <form on:submit|preventDefault={continueToSummary} class="space-y-8">
    {#each customers as customer, index}
      <fieldset class="border p-4 rounded-md border-gray-300">
        <legend class="text-lg font-medium text-blue-600 mb-2">
          Passenger {index + 1}
          {#if index === 0}
            (Lead Passenger)
          {/if}
          {#if index >= booking.AdultPassengers}
            (Infant, does not need seat)
          {/if}
        </legend>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label
              for="firstName-{index}"
              class="block text-sm font-medium text-gray-700 mb-1"
            >
              First Name
            </label>
            <input
              id="firstName-{index}"
              type="text"
              bind:value={customer.firstName}
              required
              class="w-full border p-2 rounded-md border-gray-300"
              placeholder="John"
            />
          </div>

          <div>
            <label
              for="lastName-{index}"
              class="block text-sm font-medium text-gray-700 mb-1"
            >
              Last Name
            </label>
            <input
              id="lastName-{index}"
              type="text"
              bind:value={customer.lastName}
              required
              class="w-full border p-2 rounded-md border-gray-300"
              placeholder="Doe"
            />
          </div>

          {#if index < booking.AdultPassengers}
            <div>
              <label
                for="email-{index}"
                class="block text-sm font-medium text-gray-700 mb-1"
              >
                Email
              </label>
              <input
                id="email-{index}"
                type="email"
                bind:value={customer.email}
                required={index === 0}
                class="w-full border p-2 rounded-md border-gray-300"
                placeholder="john@example.com"
              />
            </div>
          {/if}

          {#if index < booking.AdultPassengers}
            <div>
              <label
                for="phone-{index}"
                class="block text-sm font-medium text-gray-700 mb-1"
              >
                Phone Number
              </label>
              <input
                id="phone-{index}"
                type="tel"
                bind:value={customer.phone}
                required={index === 0}
                class="w-full border p-2 rounded-md border-gray-300"
                placeholder="+31 123 4567890"
              />
            </div>
          {/if}

          <div>
            <label
              for="street-{index}"
              class="block text-sm font-medium text-gray-700 mb-1"
            >
              Street Name
            </label>
            <input
              id="street-{index}"
              type="text"
              bind:value={customer.street}
              required={index === 0}
              class="w-full border p-2 rounded-md border-gray-300"
              placeholder="Borelstraat"
            />
          </div>

          <div>
            <label
              for="houseNumber-{index}"
              class="block text-sm font-medium text-gray-700 mb-1"
            >
              House Number
            </label>
            <input
              id="houseNumber-{index}"
              type="text"
              bind:value={customer.houseNumber}
              required={index === 0}
              class="w-full border p-2 rounded-md border-gray-300"
              placeholder="41"
            />
          </div>

          <div>
            <label
              for="city-{index}"
              class="block text-sm font-medium text-gray-700 mb-1"
            >
              City
            </label>
            <input
              id="city-{index}"
              type="text"
              bind:value={customer.city}
              required={index === 0}
              class="w-full border p-2 rounded-md border-gray-300"
              placeholder="Heerlen"
            />
          </div>

          <div>
            <label
              for="country-{index}"
              class="block text-sm font-medium text-gray-700 mb-1"
            >
              Country
            </label>
            <input
              id="country-{index}"
              type="text"
              bind:value={customer.country}
              required={index === 0}
              class="w-full border p-2 rounded-md border-gray-300"
              placeholder="Netherlands"
            />
          </div>

          {#if index >= booking.AdultPassengers}
            {customer.isInfant = true}
          {/if}

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
