import { writable } from "svelte/store";

// A writable Svelte store that holds the list of bookings (used across the app)
export const bookingListStore = writable([]);

/**
 * Groups raw booking entries (with duplicated booking IDs) into single logical bookings.
 *
 * Each booking ID can contain multiple flights and potentially duplicated customers.
 * This function:
 *  - Merges flights into an array per booking
 *  - Deduplicates customers
 *  - Computes passenger count based on unique customers
 */
function groupBookingsById(rawBookings) {
    const grouped = new Map();

    for (const b of rawBookings) {
        if (!grouped.has(b.id)) {
            // First time seeing this booking ID → add entry
            grouped.set(b.id, {
                ...b,
                flights: [b.flight],
                customers: b.customers || [],
                passengers: (b.customers || []).length
            });
        } else {
            // Already encountered → merge
            const existing = grouped.get(b.id);

            // Append the flight to existing flights array
            existing.flights.push(b.flight);

            // Merge and deduplicate customers based on email + last name
            const mergedCustomers = [
                ...(existing.customers || []),
                ...(b.customers || [])
            ];

            const uniqueCustomers = Array.from(
                new Map(mergedCustomers.map(c => [c.email + c.lastName, c])).values()
            );

            existing.customers = uniqueCustomers;
            existing.passengers = uniqueCustomers.length;
        }
    }

    // Convert map to array for Svelte consumption
    return Array.from(grouped.values());
}

/**
 * Fetches bookings from the backend, applies fallback/defaults where needed,
 * transforms the data structure, and sets the Svelte store.
 */
export async function fetchBookings() {
    try {
        const response = await fetch("http://localhost:8080/api/v1/bookings");
        if (!response.ok) throw new Error("Failed to fetch bookings");

        const data = await response.json();

        // Transform each booking into unified frontend shape
        const transformed = data.map((b, i) => {
            // Provide fallback/default flight info if missing
            const fallbackFlight = {
                airline: b.airline ?? "Lufthansa",
                id: b.flightId ?? `FL${100 + i}`,
                price: b.price ?? 0,
                duration: 120,
                status: "Scheduled",
                departureAirport: "Amsterdam Schiphol",
                departureAirportShort: "AMS",
                departureTerminal: "1",
                departureGate: "A1",
                departureScheduledTime: new Date().toISOString(),
                arrivalAirport: "London Heathrow",
                arrivalAirportShort: "LHR",
                arrivalTerminal: "3",
                arrivalGate: "B1",
                arrivalScheduledTime: new Date(Date.now() + 2 * 3600000).toISOString() // +2h
            };

            const f = b.flight ?? fallbackFlight;

            return {
                id: `B${(parseInt(b.id)).toString().padStart(3, "0")}`,
                flight: {
                    airline: f.airline,
                    flightNumber: f.id,
                    price: f.price,
                    status: f.status ?? ["Scheduled", "On Time", "Delayed"][i % 3],
                    duration: f.duration ?? 120,
                    departure: {
                        airport: f.departureAirport,
                        iata: f.departureAirportShort,
                        terminal: f.departureTerminal,
                        gate: f.departureGate,
                        scheduled: f.departureScheduledTime
                    },
                    arrival: {
                        airport: f.arrivalAirport,
                        iata: f.arrivalAirportShort,
                        terminal: f.arrivalTerminal,
                        gate: f.arrivalGate,
                        scheduled: f.arrivalScheduledTime
                    }
                },
                customers: (b.customers ?? []).map((c) => ({
                    firstName: c.firstName,
                    lastName: c.lastName,
                    email: c.email,
                    phone: c.phone
                })),
                passengers: (b.customers ?? []).length,
                discount: b.discount ?? 0,
                isActive: b.status === "ACTIVE"
            };
        });

        // Group duplicate IDs and update the store
        const grouped = groupBookingsById(transformed);
        bookingListStore.set(grouped);
    } catch (error) {
        console.error("Error loading bookings:", error);
    }
}
