<script>
  import { bookingStore } from "$lib/stores/bookingStore";
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

  // Function to create test round trip data
  function createTestRoundTrip() {
    // Create a basic outbound flight
    const outboundFlight = {
      id: "DUS-ERZ",
      airline: "SUNEXPRESS",
      price: 1180,
      departure: {
        iata: "DUS",
        airport: "Düsseldorf Airport",
        scheduled: "2025-06-04T06:05:00.000",
        terminal: "A",
        gate: "1"
      },
      arrival: {
        iata: "ERZ",
        airport: "Erzurum Airport",
        scheduled: "2025-06-04T11:20:00.000",
        terminal: "B",
        gate: "2"
      },
      duration: 255,
      status: "Scheduled"
    };

    // Create a return flight
    const returnFlight = {
      id: "ERZ-DUS",
      airline: "SUNEXPRESS",
      price: 1180,
      departure: {
        iata: "ERZ",
        airport: "Erzurum Airport",
        scheduled: "2025-06-10T12:15:00.000",
        terminal: "B",
        gate: "3"
      },
      arrival: {
        iata: "DUS",
        airport: "Düsseldorf Airport",
        scheduled: "2025-06-10T17:30:00.000",
        terminal: "A",
        gate: "4"
      },
      duration: 255,
      status: "Scheduled"
    };

    // Update the store with both flights
    bookingStore.update(store => ({
      ...store,
      flight: outboundFlight,
      returnFlight: returnFlight,
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
      AdultPassengers: 1
    }));
    
    console.log("Test round trip created!", $bookingStore);
  }

  // Function to debug the current booking state
  function debugBooking() {
    console.log("Current booking store:", $bookingStore);
    
    if ($bookingStore.returnFlight) {
      console.log("Return flight exists:", $bookingStore.returnFlight);
    } else {
      console.log("NO RETURN FLIGHT IN STORE");
    }
  }

  // Function to create round trip and navigate to booking overview
  function createRoundTripAndGoToOverview() {
    createTestRoundTrip();
    goto("/booking/overview");
  }
</script>

<div class="container mx-auto px-4 py-8">
  <h1 class="text-2xl font-bold mb-6">Booking Debug Tools</h1>
  
  <div class="bg-white p-6 rounded-lg shadow-md mb-8">
    <h2 class="text-xl font-semibold mb-4">Test Round Trip Booking</h2>
    <p class="mb-4">
      Use these tools to create a test round trip booking and verify it works correctly.
    </p>
    
    <div class="space-y-4">
      <button 
        class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded"
        on:click={createTestRoundTrip}
      >
        Create Test Round Trip Data
      </button>
      
      <button 
        class="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded"
        on:click={createRoundTripAndGoToOverview}
      >
        Go To Overview With Test Data
      </button>
      
      <button 
        class="bg-purple-600 hover:bg-purple-700 text-white px-4 py-2 rounded"
        on:click={debugBooking}
      >
        Debug Current Booking State
      </button>
    </div>
  </div>
  
  <div class="bg-gray-100 p-4 rounded-lg">
    <h3 class="font-bold mb-2">Current Booking Store:</h3>
    <pre class="whitespace-pre-wrap overflow-auto max-h-96 text-sm">{JSON.stringify($bookingStore, null, 2)}</pre>
  </div>
</div>
