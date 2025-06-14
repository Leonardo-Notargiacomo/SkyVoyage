import { writable } from "svelte/store";

// Store starts empty — will be filled by backend
export const bookingListStore = writable([]);

// Load bookings from backend
export async function fetchBookings() {
    try {
        const response = await fetch("http://localhost:8080/api/v1/bookings");
        if (!response.ok) throw new Error("Failed to fetch bookings");

        const data = await response.json();

        const transformed = data.map((b, i) => {
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
                arrivalScheduledTime: new Date(Date.now() + 2 * 3600000).toISOString(), // +2h
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
                        scheduled: f.departureScheduledTime,
                    },
                    arrival: {
                        airport: f.arrivalAirport,
                        iata: f.arrivalAirportShort,
                        terminal: f.arrivalTerminal,
                        gate: f.arrivalGate,
                        scheduled: f.arrivalScheduledTime,
                    },
                },
                customers: (b.customers ?? []).map((c) => ({
                    firstName: c.firstName,
                    lastName: c.lastName,
                    email: c.email,
                    phone: c.phone,
                })),
                passengers: b.adultPassengers + b.infantPassengers,
                discount: b.discount ?? 0,
                isActive: b.status === "ACTIVE",
            };
        });

        bookingListStore.set(transformed);
    } catch (error) {
        console.error("Error loading bookings:", error);
    }
}
