<script>
  import { bookingStore } from "$lib/stores/bookingStore";
  import { goto } from "$app/navigation";
  import { api } from "$lib/api.js"; 

  let discountInput = 0;
  let discountReason = "";

  // Reactive values from store
  $: booking = $bookingStore;
  $: flight = booking?.flight;
  
  // Sync discountInput with store value when loaded
  $: {
    if (booking?.discount !== undefined && discountInput !== booking.discount) {
      discountInput = booking.discount;
    }
  }

  // Format helpers
  function formatDateTime(date) {
    if (!date) return "";
    return new Date(date).toLocaleString("en-GB", {
      day: "2-digit", month: "2-digit", year: "numeric",
      hour: "2-digit", minute: "2-digit"
    });
  }

  function formatDuration(mins) {
    if (!mins) return "—";
    const hours = Math.floor(mins / 60);
    const minutes = mins % 60;
    return hours > 0 
      ? (minutes > 0 ? `${hours}h ${minutes}min` : `${hours}h`) 
      : `${minutes}min`;
  }
  
  function formatTravelClass() {
    const travelClass = booking.travelClass || "ECONOMY";
    return travelClass.charAt(0) + travelClass.slice(1).toLowerCase();
  }

  function formatDate(dateString) {
    if (!dateString) return "N/A";
    return new Date(dateString).toLocaleString("en-GB", {
      day: "2-digit", month: "2-digit", year: "numeric"
    });
  }

  function formatTime(dateString) {
    if (!dateString) return "N/A";
    return new Date(dateString).toLocaleString("en-GB", {
      hour: "2-digit", minute: "2-digit"
    });
  }

  // Helper to check if we have an itinerary with connection flights
  function hasFullItinerary() {
    return booking.flight?.fullOffer?.trips && booking.flight.fullOffer.trips.length > 0;
  }

  // Format duration in ISO format PT2H30M to readable string
  function formatIsoDuration(isoDuration) {
    if (!isoDuration) return "—";
    
    const hourMatch = isoDuration.match(/(\d+)H/);
    const minuteMatch = isoDuration.match(/(\d+)M/);
    
    const hours = hourMatch ? parseInt(hourMatch[1]) : 0;
    const minutes = minuteMatch ? parseInt(minuteMatch[1]) : 0;
    
    if (hours === 0) return `${minutes}min`;
    return minutes === 0 ? `${hours}h` : `${hours}h ${minutes}min`;
  }

  function formatConnectionTime(minutes) {
    return formatDuration(minutes);
  }
  
  function formatTripType(type) {
    return type.charAt(0).toUpperCase() + type.slice(1);
  }
  
  // Business logic
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

  function discountedAmount() {
    const passengers = booking.AdultPassengers || 1;
    return (booking.flight.price * passengers * booking.discount) / 100;
  }

  function finalPrice() {
    const passengers = booking.AdultPassengers || 1;
    return (booking.flight.price * passengers - discountedAmount()).toFixed(2);
  }

  // Creates a standardized format for all flights from fullOffer
  function prepareFlightsForBooking() {
    const flights = [];
    const flightOffer = booking.flight;
    
    // Function to convert flight to standard format
    function standardizeFlightFormat(flight, tripType) {
      const number = flight.number || "0";
      const iataFrom = flight.departure.iata;
      const iataTo = flight.arrival.iata;
      
      return {
        id: `${number}-${iataFrom}-${iataTo}`,
        number: number,
        airline: flight.carrierCode || booking.flight.airline,
        departureAirport: flight.departure.airport || `Airport ${iataFrom}`,
        departureAirportShort: iataFrom,
        departureTerminal: flight.departure.terminal,
        departureGate: flight.departure.gate,
        departureScheduledTime: flight.departure.scheduled,
        arrivalAirport: flight.arrival.airport || `Airport ${iataTo}`,
        arrivalAirportShort: iataTo,
        arrivalTerminal: flight.arrival.terminal,
        arrivalGate: flight.arrival.gate,
        arrivalScheduledTime: flight.arrival.scheduled,
        duration: parseDurationToMinutes(flight.duration) || 0,
        status: "SCHEDULED",
        tripType: tripType
      };
    }
    
    // First try to extract from fullOffer if available
    if (flightOffer.fullOffer?.trips) {
      flightOffer.fullOffer.trips.forEach(trip => {
        if (trip.flights && trip.flights.length > 0) {
          trip.flights.forEach(flight => {
            flights.push(standardizeFlightFormat(flight, trip.type));
          });
        }
      });
    }
    
    // If no flights were found in fullOffer, use the main flight data
    if (flights.length === 0) {
      flights.push({
        id: `${flightOffer.departure.iata}-${flightOffer.arrival.iata}`,
        airline: flightOffer.airline,
        price: flightOffer.price,
        departureAirport: flightOffer.departure.airport,
        departureAirportShort: flightOffer.departure.iata,
        departureTerminal: flightOffer.departure.terminal,
        departureGate: flightOffer.departure.gate,
        departureScheduledTime: flightOffer.departure.scheduled,
        arrivalAirport: flightOffer.arrival.airport,
        arrivalAirportShort: flightOffer.arrival.iata,
        arrivalTerminal: flightOffer.arrival.terminal,
        arrivalGate: flightOffer.arrival.gate,
        arrivalScheduledTime: flightOffer.arrival.scheduled,
        duration: flightOffer.duration || 0,
        status: flightOffer.status || "SCHEDULED",
        tripType: "outbound"
      });
    }
    
    return flights;
  }

  // Parse duration strings like PT2H30M to minutes
  function parseDurationToMinutes(duration) {
    if (!duration || typeof duration !== 'string') return 0;
    
    let minutes = 0;
    
    try {
      if (duration.startsWith('PT')) {
        const timeStr = duration.substring(2);
        
        const hourMatch = timeStr.match(/(\d+)H/);
        if (hourMatch && hourMatch[1]) {
          minutes += parseInt(hourMatch[1], 10) * 60;
        }
        
        const minMatch = timeStr.match(/(\d+)M/);
        if (minMatch && minMatch[1]) {
          minutes += parseInt(minMatch[1], 10);
        }
      }
    } catch (e) {
      console.error("Error parsing duration:", e);
    }
    
    return minutes || 0;
  }

  // Booking actions
  async function confirmBooking() {
    try {
      // Get all flights in standardized format
      const allFlights = prepareFlightsForBooking();
      
      // Build the booking data object
      const bookingData = {
        flightId: allFlights[0].id, // First flight ID for compatibility
        airline: booking.flight.airline,
        price: booking.flight.price,
        adultPassengers: booking.AdultPassengers || 1,
        infantPassengers: booking.infantsPassengers || 0,
        travelClass: booking.travelClass || "ECONOMY",
        discount: booking.discount || 0,
        discountReason: booking.discountReason || "",
        status: "CONFIRMED", 
        customers: booking.customers || [],
        mainFlights: allFlights // Send all flights as mainFlights
      };
      
      // Send booking to API
      const result = await api.create("bookings", JSON.stringify(bookingData));
      
      // Save confirmation and redirect
      sessionStorage.setItem("confirmedBooking", JSON.stringify($bookingStore));
      
      // Check if we have a booking ID in the result
      if (result && result.id) {
        // Redirect to tickets page with the booking ID
        goto(`/tickets/${result.id}`);
      } else {
        // Fallback if no ID is returned
        alert("Booking confirmed! ✅");
        goto("/home");
      }
    } catch (error) {
      console.error("Error confirming booking:", error);
      alert("Failed to confirm booking. Please try again.");
    }
  }

  function cancelBooking() {
    if (confirm("Are you sure you want to cancel this booking?")) {
      bookingStore.set({
        flight: null,
        customers: [],
        AdultPassengers: 1,
        infantsPassengers: 0,
        travelClass: "ECONOMY",
        discount: 0,
        discountReason: ""
      });
      alert("Booking has been cancelled.");
      goto("/home");
    }
  }
</script>

<nav class="flex my-4 px-4 md:px-6 max-w-7xl mx-auto" aria-label="Breadcrumb">
  <ol class="inline-flex items-center space-x-2">
    <li class="inline-flex items-center">
      <a href="/home" class="inline-flex items-center text-sm font-medium text-gray-600 hover:text-blue-500 transition-colors">
        <svg class="w-4 h-4 me-2" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
          <path d="m19.707 9.293-2-2-7-7a1 1 0 0 0-1.414 0l-7 7-2 2a1 1 0 0 0 1.414 1.414L2 10.414V18a2 2 0 0 0 2 2h3a1 1 0 0 0 1-1v-4a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1v4a1 1 0 0 0 1 1h3a2 2 0 0 0 2-2v-7.586l.293.293a1 1 0 0 0 1.414-1.414Z" />
        </svg>
        Dashboard
      </a>
    </li>
    <li>
      <div class="flex items-center">
        <svg class="w-4 h-4 text-gray-400 mx-1" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 6 10">
          <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="m1 9 4-4-4-4" />
        </svg>
        <span class="ms-1 text-sm font-medium text-gray-400">Booking Summary</span>
      </div>
    </li>
  </ol>
</nav>

<div class="max-w-7xl mx-auto px-4 py-4 md:px-6">
  <div class="mb-6">
    <h1 class="text-2xl font-bold text-center md:text-left text-gray-800">Booking Summary</h1>
    <p class="text-gray-500 text-center md:text-left">Review your booking details before confirming</p>
  </div>

  <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
    <div class="lg:col-span-2 space-y-6">
      <!-- Flight Info -->
      <div class="bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden">
        <div class="bg-blue-50 p-4 border-b border-gray-200">
          <div class="flex items-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-blue-700 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
            <h2 class="text-lg font-medium text-blue-800">Flight Overview</h2>
          </div>
        </div>
        
        <div class="p-4">
          <div class="grid grid-cols-2 gap-6 mb-4">
            <div class="flex items-center">
              <div class="bg-blue-100 p-2 rounded-full mr-3">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-blue-700" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 9a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 17.75a6.5 6.5 0 10-13 0h13z" />
                </svg>
              </div>
              <div>
                <p class="text-sm text-gray-500">Airline</p>
                <p class="font-medium">{booking.flight.airline}</p>
              </div>
            </div>
            <div class="flex items-center">
              <div class="bg-green-100 p-2 rounded-full mr-3">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-green-700" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <div>
                <p class="text-sm text-gray-500">Price</p>
                <p class="font-medium text-blue-700">€{booking.flight.price}</p>
              </div>
            </div>
            <div class="flex items-center">
              <div class="bg-purple-100 p-2 rounded-full mr-3">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-purple-700" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z" />
                </svg>
              </div>
              <div>
                <p class="text-sm text-gray-500">Flight Status</p>
                <p class="font-medium">{booking.flight.status || 'SCHEDULED'}</p>
              </div>
            </div>
            <div class="flex items-center">
              <div class="bg-yellow-100 p-2 rounded-full mr-3">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-yellow-700" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
                </svg>
              </div>
              <div>
                <p class="text-sm text-gray-500">Travel Class</p>
                <p class="font-medium">{formatTravelClass()}</p>
              </div>
            </div>
          </div>

          <hr class="my-4" />

          <!-- Direct flight info (fallback if no full itinerary) -->
          {#if !hasFullItinerary()}
            <div class="my-4">
              <div class="flex flex-col md:flex-row md:justify-between md:items-center">
                <div class="mb-4 md:mb-0">
                  <p class="text-sm text-gray-500">From</p>
                  <p class="text-xl font-semibold">
                    {booking.flight.departure.airport} ({booking.flight.departure.iata})
                  </p>
                  <p class="text-sm text-gray-600">
                    Terminal: {booking.flight.departure.terminal || "TBA"}, Gate: {booking.flight.departure.gate || "TBA"}
                  </p>
                  <p class="text-sm text-gray-600">Departure: {formatDateTime(booking.flight.departure.scheduled)}</p>
                </div>

                <div class="flex-1 px-4 flex flex-col items-center my-2">
                  <div class="text-xs text-gray-500 mb-2">{formatDuration(booking.flight.duration)}</div>
                  <div class="w-full h-0.5 bg-gray-200 relative">
                    <div class="absolute top-0 transform -translate-y-1/2 -translate-x-1/2 left-0">
                      <div class="h-3 w-3 bg-blue-600 rounded-full"></div>
                    </div>
                    <div class="absolute top-0 transform -translate-y-1/2 translate-x-1/2 right-0">
                      <div class="h-3 w-3 bg-green-600 rounded-full"></div>
                    </div>
                  </div>
                  <div class="text-xs text-gray-500 mt-2">Direct Flight</div>
                </div>

                <div class="text-right">
                  <p class="text-sm text-gray-500">To</p>
                  <p class="text-xl font-semibold">
                    {booking.flight.arrival.airport} ({booking.flight.arrival.iata})
                  </p>
                  <p class="text-sm text-gray-600">
                    Terminal: {booking.flight.arrival.terminal || "TBA"}, Gate: {booking.flight.arrival.gate || "TBA"}
                  </p>
                  <p class="text-sm text-gray-600">Arrival: {formatDateTime(booking.flight.arrival.scheduled)}</p>
                </div>
              </div>
            </div>
          {:else}
            <!-- Full itinerary with connections -->
            {#each booking.flight.fullOffer.trips as trip}
              <div class="mb-6">
                <div class="flex items-center gap-2 mb-3">
                  <span class={`${trip.type === 'outbound' ? 'bg-blue-100 text-blue-800' : 'bg-green-100 text-green-800'} text-xs font-medium px-2.5 py-0.5 rounded`}>
                    {formatTripType(trip.type)}
                  </span>
                  <span class="text-sm text-gray-500">
                    {trip.flights.length > 1 ? `${trip.flights.length - 1} connection${trip.flights.length > 2 ? 's' : ''}` : 'Direct flight'}
                  </span>
                  <span class="text-sm text-gray-500">·</span>
                  <span class="text-sm text-gray-500">{formatIsoDuration(trip.duration)}</span>
                </div>
                
                {#each trip.flights as flight, idx}
                  <div class="mb-3 pb-3 {idx !== trip.flights.length - 1 ? 'border-b border-dashed border-gray-200' : ''}">
                    <div class="flex justify-between items-start text-sm">
                      <div>
                        <div class="flex items-center mb-2">
                          <div class="bg-gray-200 w-8 h-8 rounded-full flex items-center justify-center mr-2">
                            <span class="font-medium">{flight.carrierCode}</span>
                          </div>
                          <p class="font-medium">Flight {flight.carrierCode} {flight.number}</p>
                        </div>
                        <p class="text-xs text-gray-500 mb-2">Duration: {formatIsoDuration(flight.duration)}</p>
                        
                        <div class="flex mt-3">
                          <div class="flex flex-col items-center mr-3">
                            <div class="w-3 h-3 bg-blue-600 rounded-full"></div>
                            <div class="w-0.5 bg-gray-300 flex-grow my-2"></div>
                            <div class="w-3 h-3 bg-green-600 rounded-full"></div>
                          </div>
                          
                          <div class="flex-1">
                            <div class="mb-4">
                              <p class="text-lg font-semibold">{formatTime(flight.departure.scheduled)}</p>
                              <p class="text-sm text-gray-600">{formatDate(flight.departure.scheduled)}</p>
                              <p class="text-xs text-gray-500">{flight.departure.iata} {#if flight.departure.terminal}Terminal {flight.departure.terminal}{/if}</p>
                            </div>
                            
                            <div>
                              <p class="text-lg font-semibold">{formatTime(flight.arrival.scheduled)}</p>
                              <p class="text-sm text-gray-600">{formatDate(flight.arrival.scheduled)}</p>
                              <p class="text-xs text-gray-500">{flight.arrival.iata} {#if flight.arrival.terminal}Terminal {flight.arrival.terminal}{/if}</p>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  {#if idx !== trip.flights.length - 1}
                    <div class="my-2 flex justify-center text-sm text-orange-700 bg-orange-50 p-2 rounded-md">
                      <div class="flex items-center">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                        <span>
                          Connection time: 
                          {formatConnectionTime((new Date(trip.flights[idx + 1].departure.scheduled).getTime() - 
                          new Date(trip.flights[idx].arrival.scheduled).getTime()) / (1000 * 60))}
                          at {trip.flights[idx].arrival.iata}
                        </span>
                      </div>
                    </div>
                  {/if}
                {/each}
              </div>
            {/each}
          {/if}
        </div>
      </div>

      <!-- Passenger Info -->
      <div class="bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden">
        <div class="bg-blue-50 p-4 border-b border-gray-200">
          <div class="flex items-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-blue-700 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
            </svg>
            <h2 class="text-lg font-medium text-blue-800">Passenger Information</h2>
          </div>
        </div>
        
        <div class="p-4">
          {#if booking.customers?.length > 0}
            <div class="space-y-4">
              <!-- Adult Passengers -->
              <div class="mb-4">
                <div class="flex items-center mb-3">
                  <div class="bg-blue-100 p-1.5 rounded-full mr-2">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-blue-700" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                    </svg>
                  </div>
                  <h3 class="font-medium text-gray-800">Adults ({booking.AdultPassengers || 1})</h3>
                </div>
                
                <div class="space-y-4">
                  {#each booking.customers.filter(c => !c.isInfant) as c}
                    <div class="bg-gray-50 p-3 rounded-md border border-gray-200">
                      <div class="flex flex-col md:flex-row md:justify-between">
                        <div class="mb-2 md:mb-0">
                          <p class="font-semibold">{c.firstName} {c.lastName}</p>
                          <div class="text-xs text-gray-500">Adult passenger</div>
                        </div>
                        
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 w-full md:w-2/3">
                          <div>
                            {#if c.email}<p class="text-sm"><span class="text-gray-500">Email:</span> {c.email}</p>{/if}
                            {#if c.phone}<p class="text-sm"><span class="text-gray-500">Phone:</span> {c.phone}</p>{/if}
                          </div>
                          <div>
                            {#if c.street && c.houseNumber}
                              <p class="text-sm"><span class="text-gray-500">Address:</span> {c.street} {c.houseNumber}</p>
                              <p class="text-sm">{c.city}, {c.country}</p>
                            {/if}
                          </div>
                        </div>
                      </div>
                    </div>
                  {/each}
                </div>
              </div>

              <!-- Infant Passengers -->
              {#if booking.infantsPassengers > 0}
                <div>
                  <div class="flex items-center mb-3">
                    <div class="bg-pink-100 p-1.5 rounded-full mr-2">
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-pink-700" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
                      </svg>
                    </div>
                    <h3 class="font-medium text-gray-800">Infants ({booking.infantsPassengers})</h3>
                  </div>
                  
                  <div class="space-y-2">
                    {#each booking.customers.filter(c => c.isInfant) as c}
                      <div class="bg-gray-50 p-3 rounded-md border border-gray-200">
                        <p class="font-semibold">{c.firstName} {c.lastName}</p>
                        <div class="text-xs text-gray-500">Infant passenger</div>
                        {#if c.street && c.houseNumber}
                          <p class="text-xs text-gray-500 mt-1">{c.street} {c.houseNumber}, {c.city}, {c.country}</p>
                        {/if}
                      </div>
                    {/each}
                  </div>
                </div>
              {/if}
            </div>
          {:else}
            <div class="p-4 bg-yellow-50 text-yellow-800 border border-yellow-200 rounded-md">
              <div class="flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
                <p>No passenger information available.</p>
              </div>
            </div>
          {/if}
        </div>
      </div>

      <!-- Price & Discount -->
      <div class="bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden">
        <div class="bg-blue-50 p-4 border-b border-gray-200">
          <div class="flex items-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-blue-700 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <h2 class="text-lg font-medium text-blue-800">Price Summary</h2>
          </div>
        </div>
        
        <div class="p-4">
          <div class="space-y-2">
            <div class="flex justify-between items-center py-2">
              <span>Base price ({booking.AdultPassengers || 1} {(booking.AdultPassengers || 1) > 1 ? 'passengers' : 'passenger'})</span>
              <span>€{((booking.AdultPassengers || 1) * booking.flight.price).toFixed(2)}</span>
            </div>
            
            {#if booking.discount > 0}
              <div class="flex justify-between items-center py-2 text-green-600">
                <span>Discount ({booking.discount}%)</span>
                <span>-€{discountedAmount().toFixed(2)}</span>
              </div>
              
              {#if booking.discountReason}
                <div class="text-xs text-gray-500 italic pl-4">
                  Reason: {booking.discountReason}
                </div>
              {/if}
              
              <hr class="my-2" />
            {/if}
            
            <div class="flex justify-between items-center py-2 font-bold text-lg">
              <span>Total Price</span>
              <span class="text-blue-700">€{finalPrice()}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Side Actions -->
    <div class="h-fit">
      <div class="bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden sticky top-4">
        <div class="bg-blue-50 p-4 border-b border-gray-200">
          <h2 class="font-medium text-blue-800">Complete Your Booking</h2>
        </div>
        
        <div class="p-4 space-y-4">
          <!-- <div>
            <label for="discount" class="block text-sm text-gray-700 mb-1">Discount (%)</label>
            <input
              type="number"
              min="0"
              max="100"
              bind:value={discountInput}
              placeholder="e.g. 10"
              class="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
            />
          </div> -->
          
          <!-- <div>
            <label for="reason" class="block text-sm text-gray-700 mb-1">Reason for Discount</label>
            <input
              type="text"
              bind:value={discountReason}
              placeholder="e.g. loyal customer"
              class="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
            />
          </div> -->
          
          <!-- <button
            on:click={applyDiscount}
            class="w-full bg-gray-700 hover:bg-gray-800 text-white font-medium py-2 rounded-md transition-colors flex items-center justify-center"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            Apply Discount
          </button> -->
          
          <!-- re-add this div if discount options get re-added -->
          <!-- <div class="border-t border-gray-200 my-4 pt-4"> -->
            <div>
            <button
              on:click={confirmBooking}
              class="w-full bg-green-600 hover:bg-green-700 text-white py-3 rounded-md font-medium mb-4 transition-all duration-200 flex items-center justify-center shadow-sm hover:shadow-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-opacity-50 transform hover:-translate-y-0.5"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              Confirm Booking
            </button>
            <button
              on:click={cancelBooking}
              class="w-full bg-red-100 hover:bg-red-200 text-red-600 py-3 rounded-md font-medium transition-all duration-200 flex items-center justify-center shadow-sm hover:shadow-md focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-opacity-50 transform hover:-translate-y-0.5"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
              Cancel Booking
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
