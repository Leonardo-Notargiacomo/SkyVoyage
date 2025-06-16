<script>
    import { api } from "$lib/api";
    import { onMount } from "svelte";

    let discounts = [];
    let newDiscount = {
        name: "",
        type: "",
        amount: 0,
        days: 0
    };
    let isSubmitting = false;
    let error = null;
    let successMessage = null;

    const discountTypes = [
        { value: "early_bird", label: "Early Bird" },
        { value: "last_minute", label: "Last Minute" },
    ];

    // Function to fetch all discounts
    async function fetchDiscounts() {
        try {
            const response = await api.fetchAPI("/discounts");
            discounts = response;
        } catch (error) {
            console.error("Failed to fetch discounts:", error);
            error = "Failed to load discounts";
        }
    }

    onMount(async () => {
        await fetchDiscounts();
    });

    async function addDiscount() {
        isSubmitting = true;
        successMessage = null;
        error = null;

        try {
            // Get employeeID from cookie
            const getCookie = (name) => {
                const value = `; ${document.cookie}`;
                const parts = value.split(`; ${name}=`);
                if (parts.length === 2) return parts.pop().split(";").shift();
                return null;
            };
            
            const employeeID = getCookie('employeeID');
            const discountData = {
                ...newDiscount,
                employeeID: employeeID ? parseInt(employeeID) : null
            };
            
            await api.fetchAPI("/discounts", {
                method: 'POST',
                body: JSON.stringify(discountData)
            });
            
            await fetchDiscounts();
            newDiscount = { name: "", type: "", amount: 0, days: 0 };
            successMessage = "Discount added successfully!";
        } catch (err) {
            // Extract the error message from the backend response
            if (err.errorData) {
                // Use the specific message from the backend if available
                error = err.errorData.message || err.errorData.error || "Failed to add discount";
            } else {
                error = err.message || "Failed to add discount";
            }
            console.error("Error adding discount:", err);
        } finally {
            isSubmitting = false;
        }
    }

    async function deleteDiscount(id) {
        if (!confirm("Are you sure you want to delete this discount?")) return;

        try {
            await api.fetchAPI(`/discounts/${id}`, {
                method: 'DELETE'
            });
            
            await fetchDiscounts();
            successMessage = "Discount deleted successfully!";
        } catch (err) {
            // Extract the error message from the backend response
            if (err.errorData) {
                // Use the specific message from the backend if available
                error = err.errorData.message || err.errorData.error || "Failed to delete discount";
            } else {
                error = err.message || "Failed to delete discount";
            }
            console.error("Error deleting discount:", err);
        }
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
    
    .btn-danger {
        background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
        transition: all 0.3s ease;
    }
    
    .btn-danger:hover {
        transform: translateY(-2px);
        box-shadow: 0 10px 15px rgba(220, 38, 38, 0.2);
    }
    
    .header-gradient {
        background: linear-gradient(135deg, #1e40af 0%, #3b82f6 100%);
        -webkit-background-clip: text;
        background-clip: text;
        color: transparent;
    }
    
    @keyframes pulse {
        0%, 100% { opacity: 1; }
        50% { opacity: 0.7; }
    }
    
    .loading-pulse {
        animation: pulse 1.5s infinite;
    }
    
    @keyframes fadeIn {
        from { opacity: 0; transform: translateY(10px); }
        to { opacity: 1; transform: translateY(0); }
    }
    
    .animate-fade-in {
        animation: fadeIn 0.3s ease-out forwards;
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
                Discount Management
            </h1>
            <p class="text-blue-700 ml-14">Create and manage discounts for flight bookings</p>
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
                <div>
                    <p class="font-medium">Error:</p>
                    <p>{error}</p>
                </div>
            </div>
        {/if}
        
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <!-- Add New Discount Form -->
            <div class="bg-white p-6 rounded-xl shadow-md border border-gray-100 card-hover">
                <h2 class="text-xl font-semibold text-gray-800 mb-6 flex items-center">
                    <div class="bg-blue-100 p-2 rounded-full mr-3">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                        </svg>
                    </div>
                    Add New Discount
                </h2>
                
                <form on:submit|preventDefault={addDiscount} class="space-y-5">
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1" for="name">
                            Discount Name
                        </label>
                        <div class="relative">
                            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
                                </svg>
                            </div>
                            <input
                                bind:value={newDiscount.name}
                                class="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                                id="name"
                                placeholder="e.g., Early Bird Special"
                                required
                                type="text"
                            />
                        </div>
                    </div>

                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1" for="type">
                            Discount Type
                        </label>
                        <div class="relative">
                            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                                </svg>
                            </div>
                            <select
                                bind:value={newDiscount.type}
                                class="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors appearance-none bg-white"
                                id="type"
                                required
                            >
                                <option value="">Select a discount type</option>
                                {#each discountTypes as type}
                                    <option value={type.value}>{type.label}</option>
                                {/each}
                            </select>
                            <div class="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
                                </svg>
                            </div>
                        </div>
                    </div>

                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1" for="amount">
                            Discount Amount (%)
                        </label>
                        <div class="relative">
                            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                                </svg>
                            </div>
                            <input
                                bind:value={newDiscount.amount}
                                class="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                                id="amount"
                                min="0"
                                max="100"
                                required
                                type="number"
                            />
                        </div>
                    </div>

                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1" for="days">
                            Days Before Flight
                        </label>
                        <div class="relative">
                            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                                </svg>
                            </div>
                            <input
                                bind:value={newDiscount.days}
                                class="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                                id="days"
                                min="1"
                                required
                                type="number"
                            />
                        </div>
                        <p class="text-sm text-gray-500 mt-1.5 ml-1">Number of days before the flight when this discount applies</p>
                    </div>

                    <div class="pt-4">
                        <button
                            class={`w-full py-2.5 rounded-lg text-white font-medium flex items-center justify-center ${
                                isSubmitting
                                    ? "bg-gray-400 cursor-not-allowed"
                                    : "btn-primary"
                            }`}
                            disabled={isSubmitting}
                            type="submit"
                        >
                            {#if isSubmitting}
                                <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                </svg>
                                Adding...
                            {:else}
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                                </svg>
                                Add Discount
                            {/if}
                        </button>
                    </div>
                </form>
            </div>

            <!-- Discount List -->
            <div class="bg-white p-6 rounded-xl shadow-md border border-gray-100 card-hover">
                <h2 class="text-xl font-semibold text-gray-800 mb-6 flex items-center">
                    <div class="bg-green-100 p-2 rounded-full mr-3">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                        </svg>
                    </div>
                    Current Discounts
                </h2>
                <div class="space-y-4">
                    {#if discounts.length === 0}
                        <div class="text-center py-12 bg-gray-50 rounded-lg border border-gray-200">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 text-gray-400 mx-auto mb-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M20 12H4M8 16l-4-4 4-4M4 12h16" />
                            </svg>
                            <p class="text-gray-500">No discounts available</p>
                            <p class="text-sm text-gray-400 mt-1">Add your first discount using the form</p>
                        </div>
                    {:else}
                        {#each discounts as discount (discount.id)}
                            <div class="border border-gray-200 rounded-lg p-4 hover:border-blue-300 hover:shadow-md transition-all duration-300 animate-fade-in">
                                <div class="flex justify-between items-start">
                                    <div>
                                        <h3 class="font-medium text-slate-800 flex items-center">
                                            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-blue-500 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
                                            </svg>
                                            {discount.name}
                                        </h3>
                                        <p class="text-sm text-gray-600 mt-2 flex items-center">
                                            <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                                                {discount.type === "early_bird" ? "Early Bird" : "Last Minute"}
                                            </span>
                                        </p>
                                        <p class="text-sm text-gray-600 mt-2">
                                            <span class="font-medium text-emerald-600">{discount.amount}% off</span> 
                                            when booking {discount.days} days before flight
                                        </p>
                                    </div>
                                    <div class="flex space-x-2">
                                        <button
                                            class="text-white px-3 py-1.5 rounded-lg bg-red-500 hover:bg-red-600 transition-colors flex items-center"
                                            on:click={() => deleteDiscount(discount.id)}
                                        >
                                            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                            </svg>
                                            Delete
                                        </button>
                                    </div>
                                </div>
                            </div>
                        {/each}
                    {/if}
                </div>
            </div>
        </div>
    </div>
</div>
