-- Add this to your existing schema.sql file

-- Create bookings table
CREATE TABLE IF NOT EXISTS public.bookings (
    id SERIAL PRIMARY KEY,
    flight_id VARCHAR(255) NOT NULL,
    airline VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    adult_passengers INTEGER NOT NULL,
    infant_passengers INTEGER NOT NULL DEFAULT 0,
    travel_class VARCHAR(50) NOT NULL DEFAULT 'ECONOMY',
    discount INTEGER NOT NULL DEFAULT 0,
    discount_reason TEXT,
    booked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'CONFIRMED'
);

-- Create booking_customers join table
CREATE TABLE IF NOT EXISTS public.booking_customers (
    booking_id INTEGER NOT NULL REFERENCES public.bookings(id) ON DELETE CASCADE,
    customer_id INTEGER NOT NULL REFERENCES public.customer(id) ON DELETE CASCADE,
    PRIMARY KEY (booking_id, customer_id)
);

-- Index for faster queries
CREATE INDEX IF NOT EXISTS idx_booking_customers_booking_id ON public.booking_customers(booking_id);
CREATE INDEX IF NOT EXISTS idx_booking_customers_customer_id ON public.booking_customers(customer_id);
