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
            error = err.message || "Failed to add discount";
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
            error = err.message || "Failed to delete discount";
            console.error("Error deleting discount:", err);
        }
    }

    async function updateDiscount(discount) {
        isSubmitting = true;
        successMessage = null;
        error = null;

        try {

            await api.fetchAPI(`/discounts/${discount.id}`, {
                method: 'PUT',
                body: JSON.stringify(newDiscount)
            });
            
            await fetchDiscounts();
            successMessage = "Discount updated successfully!";
        } catch (err) {
            error = err.message || "Failed to update discount";
            console.error("Error updating discount:", err);
        } finally {
            isSubmitting = false;
        }
    }
</script>

<div class="min-h-screen">
    <div class="container mx-auto px-4 py-8">
        <h1 class="text-3xl font-bold text-slate-800 mb-8">
            Discount Management
        </h1>
        
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <!-- Add New Discount Form -->
            <div class="bg-white p-6 rounded-lg shadow-sm">
                <h2 class="text-xl font-semibold text-slate-800 mb-4">
                    Add New Discount
                </h2>
                <form on:submit|preventDefault={addDiscount}>
                    <div class="space-y-4">
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1" for="name">
                                Discount Name
                            </label>
                            <input
                                bind:value={newDiscount.name}
                                class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-sky-500"
                                id="name"
                                placeholder="e.g., Early Bird Special"
                                required
                                type="text"
                            />
                        </div>

                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1" for="type">
                                Discount Type
                            </label>
                            <select
                                bind:value={newDiscount.type}
                                class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-sky-500"
                                id="type"
                                required
                            >
                                <option value="">Select a discount type</option>
                                {#each discountTypes as type}
                                    <option value={type.value}>{type.label}</option>
                                {/each}
                            </select>
                        </div>

                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1" for="amount">
                                Discount Amount (%)
                            </label>
                            <input
                                bind:value={newDiscount.amount}
                                class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-sky-500"
                                id="amount"
                                min="0"
                                max="100"
                                required
                                type="number"
                            />
                        </div>

                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1" for="days">
                                Days Before Flight
                            </label>
                            <input
                                bind:value={newDiscount.days}
                                class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-sky-500"
                                id="days"
                                min="1"
                                required
                                type="number"
                            />
                            <p class="text-sm text-gray-500 mt-1">Number of days before the flight when this discount applies</p>
                        </div>

                        <button
                            class={`w-full px-6 py-2 rounded-md text-white font-medium ${
                                isSubmitting
                                    ? "bg-gray-400 cursor-not-allowed"
                                    : "bg-sky-600 hover:bg-sky-700 transition-colors"
                            }`}
                            disabled={isSubmitting}
                            type="submit"
                        >
                            {isSubmitting ? "Adding..." : "Add Discount"}
                        </button>
                    </div>
                </form>
            </div>

            <!-- Discount List -->
            <div class="bg-white p-6 rounded-lg shadow-sm">
                <h2 class="text-xl font-semibold text-slate-800 mb-4">
                    Current Discounts
                </h2>
                <div class="space-y-4">
                    {#if discounts.length === 0}
                        <p class="text-gray-500">No discounts available</p>
                    {:else}
                        {#each discounts as discount (discount.id)}
                            <div class="border border-gray-200 rounded-lg p-4">
                                <div class="flex justify-between items-start">
                                    <div>
                                        <h3 class="font-medium text-slate-800">{discount.name}</h3>
                                        <p class="text-sm text-gray-600">Type: {discount.type}</p>
                                        <p class="text-sm text-gray-600">{discount.amount}% off</p>
                                        <p class="text-sm text-gray-600">Applies {discount.days} days before flight</p>
                                    </div>
                                    <div class="flex space-x-2">
                                        <button
                                            class="text-red-600 hover:text-red-800"
                                            on:click={() => deleteDiscount(discount.id)}
                                        >
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

        {#if successMessage}
            <p class="text-green-500 mt-4">{successMessage}</p>
        {/if}
        {#if error}
            <p class="text-red-500 mt-4">{error}</p>
        {/if}
    </div>
</div> 