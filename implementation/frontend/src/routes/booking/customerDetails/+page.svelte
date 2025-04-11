<script>
  import { goto } from "$app/navigation";
  import { bookingStore } from "$lib/stores/bookingStore";
  import { get } from "svelte/store";

  // const booking = get(bookingStore);
  // let flight = booking.flight;
  let passengers = 2;

  // Redirect if no flight selected

  let customers = Array.from({ length: passengers }, () => ({
    firstName: "",
    lastName: "",
    email: "",
    phone: "",
    isInfant: false,
  }));

  function continueToSummary() {
    console.log("Updating booking store with customers:", customers);
    bookingStore.update((state) => ({
      ...state,
      customers: customers,
    }));
    goto("/booking/overview");
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
        </legend>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label for="firstname" class="block text-sm font-medium text-gray-700 mb-1">
              First Name
            </label>
            <input
              id="firstname"
              name="firstname"
              type="text"
              bind:value={customer.firstName}
              required
              class="w-full border p-2 rounded-md border-gray-300"
              placeholder="John"
            />
          </div>

          <div>
            <label for="lastname" class="block text-sm font-medium text-gray-700 mb-1">
              Last Name
            </label>
            <input
              id="lastname"
              type="text"
              bind:value={customer.lastName}
              required
              class="w-full border p-2 rounded-md border-gray-300"
              placeholder="Doe"
            />
          </div>

          <div>
            <label for="email" class="block text-sm font-medium text-gray-700 mb-1">
              Email
            </label>
            <input
              id="email"
              type="email"
              bind:value={customer.email}
              required
              class="w-full border p-2 rounded-md border-gray-300"
              placeholder="john@example.com"
            />
          </div>

          <div>
            <label for="phone" class="block text-sm font-medium text-gray-700 mb-1">
              Phone {index === 0? "(required)" : "(optional)"}
            </label>
            <input
              id="phone"
              type="tel"
              bind:value={customer.phone}
              required={index===0}
              class="w-full border p-2 rounded-md border-gray-300"
              placeholder="+49 123 4567890"
            />
          </div>

          {#if index !== 0}
            <div class="col-span-2">
              <label for="isInfant-{index}" class="inline-flex items-center text-sm font-medium text-gray-700 mb-1">
                <input
                  id="isInfant-{index}"
                  type="checkbox"
                  bind:checked={customer.isInfant}
                  class="h-4 w-4 text-blue-600 border-gray-300 rounded"
                />
                Does not need a seat (Infant)
              </label>
            </div>
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
