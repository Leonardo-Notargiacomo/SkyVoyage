import { writable } from "svelte/store";

// Initial state for the booking
const initialState = {
	flight: null,
	returnFlight: null,  // Add explicit returnFlight property
	isRoundTrip: false,  // Add flag to track if this is a round trip
	passengers: 1,
	AdultPassengers: 1,
	infantsPassengers: 0,
	travelClass: "ECONOMY",
	customers: [],
	discount: 0,
	discountReason: "",
};

// Create the store
export const bookingStore = writable(initialState);

// Reset function to clear the booking data
export function resetBooking() {
	bookingStore.set(initialState);
}

// Enhanced helper function to add a flight to the booking
export function addFlight(flight, isReturn = false) {
	bookingStore.update((store) => {
		const updatedStore = { ...store };
		
		// Set the appropriate flight based on isReturn flag
		if (isReturn) {
			console.log("Setting return flight:", flight);
			updatedStore.returnFlight = flight;
			updatedStore.isRoundTrip = true;
		} else {
			console.log("Setting outbound flight:", flight);
			updatedStore.flight = flight;
		}
		
		return updatedStore;
	});
}

// Explicitly set both outbound and return flights
export function setRoundTrip(outboundFlight, returnFlight) {
	bookingStore.update((store) => ({
		...store,
		flight: outboundFlight,
		returnFlight: returnFlight,
		isRoundTrip: true
	}));
	
	console.log("Set round trip flights:", { outbound: outboundFlight, return: returnFlight });
}

// Helper function to debug the store
export function debugBookingStore() {
  let currentStore;
  bookingStore.subscribe(store => {
    currentStore = store;
  })();
  
  console.log("BOOKING STORE DEBUG:");
  console.log("Full store:", currentStore);
  console.log("Has return flight:", currentStore.returnFlight !== undefined);
  console.log("Return flight:", currentStore.returnFlight);
  console.log("Is round trip:", currentStore.isRoundTrip);
  
  return currentStore;
}

// Fix for setting return flight to ensure it's stored correctly
export function setReturnFlight(returnFlight) {
  if (!returnFlight) {
    console.error("Attempted to set null return flight");
    return;
  }
  
  console.log("Setting return flight:", returnFlight);
  
  bookingStore.update((store) => {
    // Create a new store object with the return flight
    const updatedStore = {
      ...store,
      returnFlight: returnFlight,
      isRoundTrip: true
    };
    
    console.log("Updated store with return flight:", updatedStore);
    
    return updatedStore;
  });
  
  // Verify that the return flight was set
  debugBookingStore();
}

// Function to create a test round trip booking
export function createTestRoundTrip() {
  // Create outbound flight
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

  // Create return flight
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

  // Update store with both flights
  bookingStore.update(store => ({
    ...store,
    flight: outboundFlight,
    returnFlight: returnFlight,
    isRoundTrip: true
  }));
  
  console.log("Test round trip created!");
  
  // Verify store was updated
  debugBookingStore();
}

// Helper function to set passengers
export function setPassengers(adultPassengers, infantPassengers = 0) {
	bookingStore.update((store) => ({
		...store,
		passengers: adultPassengers + infantPassengers,
		AdultPassengers: adultPassengers,
		infantsPassengers: infantPassengers,
	}));
}

// Helper function to set travel class
export function setTravelClass(travelClass) {
	bookingStore.update((store) => ({
		...store,
		travelClass,
	}));
}

// Helper function to add customers
export function setCustomers(customers) {
	bookingStore.update((store) => ({
		...store,
		customers,
	}));
}

// Helper function to apply discount
export function applyDiscount(discount, reason = "") {
	bookingStore.update((store) => ({
		...store,
		discount,
		discountReason: reason,
	}));
}
