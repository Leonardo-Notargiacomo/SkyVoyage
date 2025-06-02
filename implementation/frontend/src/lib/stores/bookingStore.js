import { writable } from "svelte/store";

export const bookingStore = writable({
  flight: null,
  passengers: 1,
  customers: [],
  discount: 0,
});
