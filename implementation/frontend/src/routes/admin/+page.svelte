<script>
    import { api } from "$lib/api";
    import { onMount } from "svelte";

    let pricePerKm = 15;
    let newPrice = pricePerKm;
    let isSubmitting = false;
    let error = null;
    let successMessage = null;

    onMount(async () => {
        try {
            const response = await api.fetchAPI("flights/price");
            pricePerKm = response.price;
        } catch (error) {
            console.error("Failed to fetch price:", error);
        }
    });

    async function updatePrice() {
        isSubmitting = true;
        successMessage = null;
        error = null;

        try {
            await api.create("/flights/price", { 
                price: newPrice 
            });
            pricePerKm = newPrice;
            successMessage = "Price updated successfully!";
        } catch (err) {
            error = "Failed to update price - " + (err.errorData?.error || err.message);
            console.error("Full error:", err);
        } finally {
            isSubmitting = false;
        }
    }

    function formatCurrency(value) {
        return `${value/100} €`;
    }
</script>

<div class="min-h-screen">
    <div class="container mx-auto px-4 py-8">
        <h1 class="text-3xl font-bold text-slate-800 mb-8">
            Sales Manager Dashboard
        </h1>
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <div class="bg-white p-6 rounded-lg shadow-sm">
                <h2 class="text-xl font-semibold text-slate-800 mb-4">
                    Update Pricing
                </h2>
                <form on:submit|preventDefault={updatePrice}>
                    <div class="mb-4">
                        <label
                            class="block text-sm font-medium text-gray-700 mb-1"
                            for="price-per-km"
                        >
                            Set Price Per Kilometer (cents)
                        </label>
                        <input
                            bind:value={newPrice}
                            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-sky-500"
                            id="price-per-km"
                            min="1"
                            placeholder="Enter price per kilometer"
                            required
                            step="1"
                            type="number"
                        />
                    </div>

                    <div
                        class="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-4"
                    >
                        <button
                            class={`px-6 py-2 rounded-md text-white font-medium ${
                                isSubmitting || newPrice === pricePerKm
                                    ? "bg-gray-400 cursor-not-allowed"
                                    : "bg-sky-600 hover:bg-sky-700 transition-colors"
                            }`}
                            disabled={isSubmitting || newPrice === pricePerKm}
                            type="submit"
                        >
                            {isSubmitting ? "Updating..." : "Update Price"}
                        </button>
                    </div>
                </form>
                {#if successMessage}
                    <p class="text-green-500 mt-4">{successMessage}</p>
                {/if}
                {#if error}
                    <p class="text-red-500 mt-4">{error}</p>
                {/if}
            </div>
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
                
                <div class="bg-white p-6 rounded-lg shadow-sm">
                    <h2 class="text-xl font-semibold text-slate-800 mb-4">
                        Current Pricing
                    </h2>
                    <div class="space-y-2">
                        <p class="text-gray-600">
                            Current price per kilometer: 
                            <span class="font-medium text-sky-600">
                                {formatCurrency(pricePerKm)}
                            </span>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
