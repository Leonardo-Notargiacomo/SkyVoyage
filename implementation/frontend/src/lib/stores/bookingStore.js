import { writable } from "svelte/store";

// Initial state for the booking
const initialState = {
	flight: null,
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

// Helper function to add a flight to the booking
export function addFlight(flight) {
	bookingStore.update((store) => ({
		...store,
		flight,
	}));
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
