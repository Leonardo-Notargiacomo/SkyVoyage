<script>
    import { bookingListStore } from "$lib/stores/bookingListStore.js";
    import { onMount } from "svelte";

    let bookings = [];
    let loading = true;
    let selectedBooking = null;
    let currentPage = 1;
    const bookingsPerPage = 18;

    let searchQuery = "";
    let showInactive = false;

    let lastDeletedId = null;
    let undoTimer;

    bookingListStore.subscribe((data) => {
        bookings = data;
    });

    import { fetchBookings } from "$lib/stores/bookingListStore.js";

    onMount(() => {
        fetchBookings().finally(() => {
            loading = false;
        });
        window.addEventListener("keydown", handleKey);
    });

    function handleKey(e) {
        if (e.key === "Escape") selectedBooking = null;
    }

    const formatDateTime = (date) =>
        new Date(date).toLocaleString("en-GB", {
            day: "2-digit",
            month: "2-digit",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit",
        });

    const formatDuration = (mins) => {
        if (!mins) return "—";
        return mins < 60 ? `${mins}min` : `${Math.floor(mins / 60)}h ${mins % 60}min`;
    };

    const discountedAmount = (booking) =>
        (booking.flight.price * booking.passengers * booking.discount) / 100;

    const finalPrice = (booking) =>
        (booking.flight.price * booking.passengers - discountedAmount(booking)).toFixed(2);

    function openModal(booking) {
        selectedBooking = booking;
    }

    function closeModal() {
        selectedBooking = null;
    }

    function softDelete(id) {
        bookingListStore.update((list) =>
            list.map((b) => (b.id === id ? { ...b, isActive: false } : b))
        );
        closeModal();
        lastDeletedId = id;
        clearTimeout(undoTimer);
        undoTimer = setTimeout(() => {
            lastDeletedId = null;
        }, 7000);
    }

    function undoDelete() {
        if (!lastDeletedId) return;
        bookingListStore.update((list) =>
            list.map((b) => (b.id === lastDeletedId ? { ...b, isActive: true } : b))
        );
        lastDeletedId = null;
        clearTimeout(undoTimer);
    }

    function scrollToTop() {
        window.scrollTo({ top: 0, behavior: "smooth" });
    }

    function nextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            scrollToTop();
        }
    }

    function prevPage() {
        if (currentPage > 1) {
            currentPage--;
            scrollToTop();
        }
    }

    async function permanentlyDelete(id) {
        const numericId = Number(id.replace(/^B/, ""));
        if (isNaN(numericId)) {
            alert("Invalid booking ID.");
            return;
        }

        const response = await fetch(`http://localhost:8080/api/v1/bookings/${numericId}`, {
            method: "DELETE"
        });

        if (response.ok || response.status === 204) {
            bookingListStore.update((list) => list.filter((b) => b.id !== id));
            closeModal();
        } else {
            alert("Failed to permanently delete booking.");
        }
    }

    $: filteredBookings = bookings.filter(
        (b) =>
            (showInactive || b.isActive !== false) &&
            b.id.toLowerCase().includes(searchQuery.toLowerCase())
    );

    $: totalPages = Math.ceil(filteredBookings.length / bookingsPerPage);
    $: paginatedBookings = filteredBookings.slice(
        (currentPage - 1) * bookingsPerPage,
        currentPage * bookingsPerPage
    );

    $: if (searchQuery) currentPage = 1;
</script>

<div class="min-h-screen flex flex-col justify-between max-w-7xl mx-auto px-4 py-8 relative">
    <div class={selectedBooking ? 'blur-sm pointer-events-none select-none' : ''}>
        <h1 class="text-2xl font-bold mb-6 text-center">Sales Employee Bookings</h1>

        <!-- Search + Toggle -->
        <div class="flex flex-col md:flex-row md:items-center gap-4 mb-6 max-w-2xl mx-auto">
            <input
                    type="text"
                    placeholder="Search Booking ID"
                    bind:value={searchQuery}
                    class="w-full border border-gray-300 rounded-md p-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
            />
            <label class="flex items-center gap-2 text-sm text-gray-700">
                <input type="checkbox" bind:checked={showInactive} />
                Show Inactive
            </label>
        </div>

        {#if loading}
            <p class="text-center font-semibold text-gray-500">Loading bookings...</p>
        {:else if filteredBookings.length === 0}
            <p class="text-center font-semibold text-gray-500">No bookings found.</p>
        {:else}
            <!-- Card Layout -->
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {#each paginatedBookings as booking}
                    <button
                            type="button"
                            on:click={() => openModal(booking)}
                            class={`w-full text-left bg-white border border-gray-200 rounded-xl shadow-sm hover:shadow-md transition p-4 ${
        booking.isActive === false ? 'opacity-50 grayscale' : ''
      }`}
                    >
                        <div class="flex items-center justify-between">
                            <!-- Booking Info -->
                            <div>
                                <h3 class="text-lg font-semibold text-gray-800">{booking.id}</h3>

                                {#if booking.flight.airline && booking.flight.flightNumber}
                                    <p class="text-sm text-gray-600">
                                        {booking.flight.airline} — {booking.flight.flightNumber}
                                    </p>
                                {/if}

                                {#if booking.flight.departure?.iata && booking.flight.arrival?.iata}
                                    <p class="text-sm text-gray-500">
                                        {booking.flight.departure.iata} → {booking.flight.arrival.iata}
                                    </p>
                                {/if}

                                {#if booking.flight.departure?.airport && booking.flight.arrival?.airport}
                                    <p class="text-xs text-gray-400">
                                        {booking.flight.departure.airport} → {booking.flight.arrival.airport}
                                    </p>
                                {/if}

                                <div class="mt-2 flex items-center gap-2 text-sm">
                                    <!-- Price -->
                                    <span class="inline-block px-2 py-0.5 bg-gray-100 rounded text-gray-700 font-medium text-xs">
        €{booking.flight.price}
      </span>

                                    <!-- Status Badge -->
                                    <span class={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium
        ${
          booking.flight.status === 'Scheduled'
            ? 'bg-green-100 text-green-700 ring-1 ring-green-300'
            : booking.flight.status === 'Delayed'
            ? 'bg-yellow-100 text-yellow-800 ring-1 ring-yellow-300'
            : 'bg-gray-100 text-gray-600 ring-1 ring-gray-300'
        }
      `}>
        {booking.flight.status || 'Scheduled'}
      </span>
                                </div>
                            </div>

                            <!-- Right Side: Passenger Info -->
                            <div class="text-right text-sm text-gray-500">
                                <p>
                                    {booking.passengers} passenger{booking.passengers !== 1 ? 's' : ''}
                                </p>
                                {#if booking.customers.length}
                                    <p class="text-xs text-gray-400">
                                        {booking.customers[0].firstName} {booking.customers[0].lastName}
                                    </p>
                                {/if}
                            </div>
                        </div>
                    </button>
                {/each}
            </div>
        {/if}
    </div>

    <!-- Pagination -->
    {#if !loading && filteredBookings.length > 0}
        <div class="mt-10 pt-4 border-t border-gray-200 sticky bottom-0 bg-white z-10">
            <div class="flex justify-center items-center gap-4">
                <button
                        class="px-4 py-2 bg-gray-200 hover:bg-gray-300 rounded disabled:opacity-50"
                        on:click={prevPage}
                        disabled={currentPage === 1}
                >
                    Previous
                </button>

                <span class="text-sm text-gray-600">
          Page {currentPage} of {totalPages}
        </span>

                <button
                        class="px-4 py-2 bg-gray-200 hover:bg-gray-300 rounded disabled:opacity-50"
                        on:click={nextPage}
                        disabled={currentPage === totalPages}
                >
                    Next
                </button>
            </div>
        </div>
    {/if}

    <!-- Modal -->
    {#if selectedBooking}
        <section
                class="fixed inset-0 bg-black/30 backdrop-blur-sm flex items-center justify-center z-50"
                role="dialog"
                aria-modal="true"
        >
            <!-- Clickable backdrop button -->
            <button
                    type="button"
                    aria-label="Close Modal"
                    class="absolute inset-0 w-full h-full bg-transparent cursor-default"
                    on:click={closeModal}
            ></button>

            <!-- Modal content -->
            <div
                    class="bg-white w-full max-w-2xl mx-auto rounded-lg shadow-lg p-6 space-y-4 overflow-y-auto max-h-[90vh] animate-fade-in z-10 relative"
            >
                <!-- Header -->
                <div class="flex justify-between items-start">
                    <div>
                        <p class="text-blue-600 font-semibold text-lg">{selectedBooking.id}</p>
                        <p class="text-sm text-gray-600">
                            {selectedBooking.flight.airline} — {selectedBooking.flight.flightNumber}
                        </p>
                    </div>
                    <button
                            type="button"
                            on:click={closeModal}
                            class="text-gray-400 hover:text-gray-600 text-2xl leading-none"
                            aria-label="Close modal"
                    >
                        &times;
                    </button>
                </div>

                <!-- Flight Details -->
                <div>
                    <h2 class="text-blue-600 font-semibold text-sm mb-1">Flight Details</h2>
                    <p><strong>From:</strong> {selectedBooking.flight.departure.airport} ({selectedBooking.flight.departure.iata})</p>
                    <p><strong>To:</strong> {selectedBooking.flight.arrival.airport} ({selectedBooking.flight.arrival.iata})</p>
                    <p><strong>Departure:</strong> {formatDateTime(selectedBooking.flight.departure.scheduled)}</p>
                    <p><strong>Arrival:</strong> {formatDateTime(selectedBooking.flight.arrival.scheduled)}</p>
                    <p><strong>Duration:</strong> {formatDuration(selectedBooking.flight.duration)}</p>
                </div>

                <!-- Passenger Info -->
                <div>
                    <h2 class="text-blue-600 font-semibold text-sm mb-1">Passengers</h2>
                    {#if selectedBooking.customers.length > 0}
                        {#each selectedBooking.customers as c, index}
                            <div class="mb-1">
                                <p><strong>Passenger {index + 1}:</strong> {c.firstName} {c.lastName}</p>
                                {#if c.email}
                                    <p class="text-gray-500 text-xs">{c.email}</p>
                                {/if}
                                {#if c.phone}
                                    <p class="text-gray-500 text-xs">{c.phone}</p>
                                {/if}
                            </div>
                        {/each}
                    {:else}
                        <p class="text-gray-500">No passenger info.</p>
                    {/if}
                </div>

                <!-- Price Info -->
                <div>
                    <h2 class="text-blue-600 font-semibold text-sm mb-1">Price & Discount</h2>
                    <p>
                        <strong>Base price:</strong> {selectedBooking.passengers} × €{selectedBooking.flight.price} =
                        €{(selectedBooking.passengers * selectedBooking.flight.price).toFixed(2)}
                    </p>
                    <p>
                        <strong>Discount:</strong> {selectedBooking.discount}% =
                        − €{discountedAmount(selectedBooking).toFixed(2)}
                    </p>
                    <p><strong>Total:</strong> €{finalPrice(selectedBooking)}</p>
                </div>

                <!-- Delete Booking -->
                {#if selectedBooking.isActive !== false}
                    <button
                            class="mt-4 w-full bg-red-500 text-white py-2 rounded hover:bg-red-600 font-semibold"
                            on:click={() => softDelete(selectedBooking.id)}
                    >
                        Delete Booking
                    </button>
                {:else}
                    <button
                            class="mt-4 w-full bg-red-700 text-white py-2 rounded hover:bg-red-800 font-semibold"
                            on:click={() => permanentlyDelete(selectedBooking.id)}
                    >
                        Permanently Delete
                    </button>
                {/if}
            </div>
        </section>
    {/if}

    <!-- Undo Snackbar -->
    {#if lastDeletedId}
        <div class="max-w-2xl mx-auto mb-6">
            <div class="bg-white shadow-md border border-gray-200 rounded-lg px-4 py-3 flex items-center justify-between gap-4 animate-fade-in">
                <span class="text-sm text-gray-700">Booking deleted.</span>
                <button
                        on:click={undoDelete}
                        class="text-sm font-medium text-blue-600 hover:text-blue-800 transition"
                >
                    Undo
                </button>
            </div>
        </div>
    {/if}
</div>

<style>
    @keyframes fadeIn {
        0% {
            opacity: 0;
            transform: scale(0.95);
        }
        100% {
            opacity: 1;
            transform: scale(1);
        }
    }

    .animate-fade-in {
        animation: fadeIn 0.2s ease-out;
    }
</style>