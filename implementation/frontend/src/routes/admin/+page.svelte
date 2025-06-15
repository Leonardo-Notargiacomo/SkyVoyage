<script>
    import { api } from "$lib/api";
    import { onMount } from "svelte";

    let pricePerKm = 15;
    let newPrice = pricePerKm;
    let isSubmitting = false;
    let error = null;
    let successMessage = null;

    // Function to fetch the current price
    async function fetchCurrentPrice() {
        try {
            const response = await api.fetchAPI("/flights/price");
            pricePerKm = response.price;
            console.log("Current price fetched:", pricePerKm);
            return response.price;
        } catch (error) {
            console.error("Failed to fetch price:", error);
            return null;
        }
    }

    onMount(async () => {
        await fetchCurrentPrice();
    });

    async function updatePrice() {
        isSubmitting = true;
        successMessage = null;
        error = null;

        try {
            await api.fetchAPI(`/flights/price/create?price=${newPrice}`, {
                method: 'POST'
            });
            
            // Fetch the updated price after update
            const updatedPrice = await fetchCurrentPrice();
            if (updatedPrice !== null) {
                pricePerKm = updatedPrice;
                newPrice = updatedPrice;
            } else {
                pricePerKm = newPrice; // Fallback if fetch fails
            }
            
            await api.clearFlightCache();
            successMessage = "Price updated successfully!";
        } catch (err) {
            error = err.message || "Failed to update price";
            console.error("Error updating price:", err);
        } finally {
            isSubmitting = false;
        }
    }

    function formatCurrency(value) {
        return `${value/100} €`;
    }
</script>

<style>
    .card-hover {
        transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    }
    
    .card-hover:hover {
        transform: translateY(-5px);
        box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
    }
    
    .btn-primary {
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
        transition: all 0.3s ease;
    }
    
    .btn-primary:hover:not(:disabled) {
        transform: translateY(-2px);
        box-shadow: 0 10px 15px rgba(37, 99, 235, 0.2);
    }
    
    .header-gradient {
        background: linear-gradient(135deg, #1e40af 0%, #3b82f6 100%);
        -webkit-background-clip: text;
        background-clip: text;
        color: transparent;
    }
    
    @keyframes fadeIn {
        from { opacity: 0; transform: translateY(10px); }
        to { opacity: 1; transform: translateY(0); }
    }
    
    .animate-fade-in {
        animation: fadeIn 0.3s ease-out forwards;
    }
    
    .price-indicator {
        position: relative;
        overflow: hidden;
    }
    
    .price-indicator::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        height: 4px;
    }
    
    .price-pulse {
        animation: pricePulse 2s infinite;
    }
    
    @keyframes pricePulse {
        0% { transform: scale(1); }
        50% { transform: scale(1.05); }
        100% { transform: scale(1); }
    }
</style>

<div class="min-h-screen">
    <div class="container mx-auto px-4 py-8">
        <div class="mb-8 bg-gradient-to-r from-blue-50 to-indigo-100 rounded-lg shadow-sm border border-blue-200 overflow-hidden p-6">
            <h1 class="text-3xl font-bold header-gradient flex items-center">
                <div class="bg-blue-600 p-2 rounded-lg shadow-md mr-3">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                </div>
                Sales Manager Dashboard
            </h1>
            <p class="text-blue-700 ml-14">Manage flight pricing and view financial metrics</p>
        </div>
        
        {#if successMessage}
            <div class="mb-6 bg-green-50 border-l-4 border-green-500 text-green-700 p-4 rounded-md flex items-center shadow-md animate-fade-in">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-3 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                </svg>
                {successMessage}
            </div>
        {/if}
        
        {#if error}
            <div class="mb-6 bg-red-50 border-l-4 border-red-500 text-red-700 p-4 rounded-md flex items-center shadow-md animate-fade-in">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-3 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
                {error}
            </div>
        {/if}
        
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <div class="bg-white p-6 rounded-xl shadow-md border border-gray-100 card-hover">
                <h2 class="text-xl font-semibold text-gray-800 mb-6 flex items-center">
                    <div class="bg-blue-100 p-2 rounded-full mr-3">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                        </svg>
                    </div>
                    Update Pricing
                </h2>
                <form on:submit|preventDefault={updatePrice} class="space-y-5">
                    <div>
                        <label
                            class="block text-sm font-medium text-gray-700 mb-1"
                            for="price-per-km"
                        >
                            Set Price Per Kilometer (cents)
                        </label>
                        <div class="relative">
                            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                                </svg>
                            </div>
                            <input
                                bind:value={newPrice}
                                class="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                                id="price-per-km"
                                min="1"
                                placeholder="Enter price per kilometer"
                                required
                                step="1"
                                type="number"
                            />
                        </div>
                        <p class="text-sm text-gray-500 mt-1.5 ml-1">Enter the price in cents per kilometer (e.g., 15 = €0.15/km)</p>
                    </div>

                    <div class="pt-4">
                        <button
                            class={`w-full py-2.5 rounded-lg text-white font-medium flex items-center justify-center ${
                                isSubmitting || newPrice === pricePerKm
                                    ? "bg-gray-400 cursor-not-allowed"
                                    : "btn-primary"
                            }`}
                            disabled={isSubmitting || newPrice === pricePerKm}
                            type="submit"
                        >
                            {#if isSubmitting}
                                <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                </svg>
                                Updating...
                            {:else}
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                                </svg>
                                Update Price
                            {/if}
                        </button>
                    </div>
                </form>
            </div>
            
            <div class="grid grid-cols-1 gap-6">
                <div class="bg-white p-6 rounded-xl shadow-md border border-gray-100 card-hover price-indicator">
                    <h2 class="text-xl font-semibold text-gray-800 mb-6 flex items-center">
                        <div class="bg-green-100 p-2 rounded-full mr-3">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                            </svg>
                        </div>
                        Current Pricing
                    </h2>
                    <div class="text-center py-10">
                        <div class="inline-flex items-center justify-center p-4 bg-blue-50 rounded-full mb-4 price-pulse">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-10 w-10 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                            </svg>
                        </div>
                        <p class="text-gray-600 mb-2">
                            Current price per kilometer: 
                        </p>
                        <p class="text-3xl font-bold text-blue-700">
                            {formatCurrency(pricePerKm)}
                        </p>
                        <p class="text-sm text-gray-500 mt-2">Updated automatically when price changes</p>
                    </div>
                </div>
                
                <div class="bg-white p-6 rounded-xl shadow-md border border-gray-100 card-hover">
                    <h2 class="text-xl font-semibold text-gray-800 mb-6 flex items-center">
                        <div class="bg-purple-100 p-2 rounded-full mr-3">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-purple-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
                            </svg>
                        </div>
                        Quick Tips
                    </h2>
                    <div class="space-y-4">
                        <div class="flex items-start">
                            <div class="bg-blue-50 p-1 rounded-full mr-3 mt-0.5">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                                </svg>
                            </div>
                            <p class="text-sm text-gray-600">Flight prices are calculated automatically based on distance and this price per kilometer value.</p>
                        </div>
                        <div class="flex items-start">
                            <div class="bg-blue-50 p-1 rounded-full mr-3 mt-0.5">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                                </svg>
                            </div>
                            <p class="text-sm text-gray-600">After updating the price, all flight prices will be recalculated automatically.</p>
                        </div>
                        <div class="flex items-start">
                            <div class="bg-blue-50 p-1 rounded-full mr-3 mt-0.5">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                                </svg>
                            </div>
                            <p class="text-sm text-gray-600">Visit the Discount Management page to create special pricing offers.</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
