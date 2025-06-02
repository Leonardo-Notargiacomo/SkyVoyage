<script>
  import { bookingStore, createTestRoundTrip, debugBookingStore } from "$lib/stores/bookingStore";
  import { goto } from "$app/navigation";
  import { onMount } from "svelte";

  // Initialize with some customers
  onMount(() => {
    // Test if the booking store has a return flight
    const hasReturn = $bookingStore.returnFlight !== undefined;
    console.log("Current booking has return:", hasReturn);
    
    // Add test customers if none exist
    if (!$bookingStore.customers || $bookingStore.customers.length === 0) {
      bookingStore.update(store => ({
        ...store,
        customers: [{
          firstName: "Test",
          lastName: "User",
          email: "test@example.com",
          phone: "+31612345678",
          street: "Test Street",
          houseNumber: "1",
          city: "Test City",
          country: "Test Country",
          isInfant: false
        }],
        AdultPassengers: 1,
        infantsPassengers: 0
      }));
    }
  });

  // Function to create round trip and navigate to booking overview
  function createRoundTripAndGoToOverview() {
    createTestRoundTrip();
    goto("/booking/overview");
  }
</script>

<div class="container mx-auto px-4 py-8">
  <h1 class="text-2xl font-bold mb-6">Booking Debug Page</h1>
  
  <div class="bg-white rounded-lg shadow-md p-6 mb-6">
    <h2 class="text-xl font-semibold mb-4">Test Round Trip Creation</h2>
    <p class="mb-4">This page helps you test round trip bookings by creating a sample booking with both outbound and return flights.</p>
    
    <div class="space-y-4">
      <button 
        class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 transition-colors"
        on:click={createTestRoundTrip}
      >
        Create Test Round Trip
      </button>
      
      <button 
        class="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 transition-colors"
        on:click={createRoundTripAndGoToOverview}
      >
        Create Round Trip and Go to Overview
      </button>
      
      <button 
        class="bg-gray-600 text-white px-4 py-2 rounded hover:bg-gray-700 transition-colors"
        on:click={debugBookingStore}
      >
        Debug Current Booking Store
      </button>
    </div>
  </div>
  
  <div class="bg-gray-100 rounded-lg p-4 font-mono text-sm">
    <h3 class="font-bold mb-2">Current Booking Store:</h3>
    <pre class="whitespace-pre-wrap overflow-auto max-h-96">{JSON.stringify($bookingStore, null, 2)}</pre>
  </div>
</div>
