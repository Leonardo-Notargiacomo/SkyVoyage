// Functions for formatting dates, durations, and calculating discounts

/**
 * Formats a given date or date array into a human-readable string (dd/mm/yyyy, HH:MM).
 *
 * Supports both:
 * - ISO date strings
 * - PostgreSQL-style date arrays like [2025, 6, 16, 14, 30, 0]
 */
export function formatDateTime(date) {
    const parsed = Array.isArray(date)
        ? new Date(...date)
        : new Date(date);

    return parsed.toLocaleString("en-GB", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    });
}

/**
 * Converts a duration in minutes to a string format.
 * Examples:
 * - 45 → "45min"
 * - 135 → "2h 15min"
 */
export function formatDuration(mins) {
    if (!mins) return "—";
    return mins < 60
        ? `${mins}min`
        : `${Math.floor(mins / 60)}h ${mins % 60}min`;
}

/**
 * Calculates the total discount amount for a booking based on:
 * - Price of the first flight
 * - Number of customers (adults + infants)
 * - Discount percentage
 */
export function discountedAmount(booking) {
    return (
        booking.flights[0].price *
        booking.customers.length *
        booking.discount
    ) / 100;
}

/**
 * Calculates the final price after applying the discount.
 * Returns a string with two decimal places.
 */
export function finalPrice(booking) {
    return (
        booking.flights[0].price *
        booking.customers.length -
        discountedAmount(booking)
    ).toFixed(2);
}
