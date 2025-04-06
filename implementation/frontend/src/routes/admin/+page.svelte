<script>
    let pricePerKm = 0.15;
    let priceHistory = [
        {date: '2023-01-15', price: 0.12},
        {date: '2023-03-22', price: 0.14},
        {date: '2023-06-10', price: 0.15}
    ];

    let customDistance = 1000;
    let customDistanceOptions = [
        {value: 500, label: '500 km'},
        {value: 1000, label: '1,000 km'},
        {value: 2500, label: '2,500 km'},
        {value: 5000, label: '5,000 km'},
        {value: 10000, label: '10,000 km'}
    ];

    let newPrice = pricePerKm;
    let isSubmitting = false;

    function updatePrice() {
        isSubmitting = true;

        // Simulate API call
        setTimeout(() => {
            pricePerKm = newPrice;
            const today = new Date();
            const dateStr = today.toISOString().split('T')[0];
            priceHistory = [...priceHistory, {date: dateStr, price: newPrice}];
            isSubmitting = false;
        }, 800);
    }

    function formatDate(dateStr) {
        const date = new Date(dateStr);
        return new Intl.DateTimeFormat('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        }).format(date);
    }

    function formatCurrency(value) {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'EUR',
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        }).format(value);
    }
</script>

<div class="min-h-screen">

    <div class="container mx-auto px-4 py-8">
        <h1 class="text-3xl font-bold text-slate-800 mb-8">Sales Manager Dashboard</h1>
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <div class="bg-white p-6 rounded-lg shadow-sm">
                <h2 class="text-xl font-semibold text-slate-800 mb-4">Update Pricing</h2>
                <form on:submit|preventDefault={updatePrice}>
                    <div class="mb-4">
                        <label class="block text-sm font-medium text-gray-700 mb-1" for="price-per-km">
                            Set Price Per Kilometer (EUR)
                        </label>
                        <input
                                bind:value={newPrice}
                                class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-sky-500"
                                id="price-per-km"
                                min="0.01"
                                placeholder="Enter price per kilometer"
                                required
                                step="0.01"
                                type="number"
                        />
                    </div>

                    <div class="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-4">
                        <button
                                class={`px-6 py-2 rounded-md text-white font-medium ${ isSubmitting || newPrice === pricePerKm
        ? 'bg-gray-400 cursor-not-allowed'
        : 'bg-sky-600 hover:bg-sky-700 transition-colors'
    }`}
                                disabled={isSubmitting || newPrice === pricePerKm}
                                type="submit"
                        >
                            {isSubmitting ? 'Updating...' : 'Update Price'}
                        </button>
                    </div>
                </form>
            </div>

            <div class="bg-white p-6 rounded-lg shadow-sm">
                <h2 class="text-xl font-semibold text-slate-800 mb-4">Pricing History</h2>

                {#if priceHistory.length === 0}
                    <p class="text-gray-500 text-center py-6">No pricing history available</p>
                {:else}
                    <div class="space-y-4">
                        {#each priceHistory as record, i}
                            <div class={`flex justify-between items-center py-2 ${
                i !== priceHistory.length - 1 ? 'border-b border-gray-100' : ''
              }`}>
                                <div>
                                    <p class="text-sm text-gray-500">{formatDate(record.date)}</p>
                                </div>
                                <div>
                  <span class={`text-sm font-medium ${
                    i === priceHistory.length - 1 ? 'text-sky-600' : 'text-gray-700'
                  }`}>
                    {formatCurrency(record.price)}
                  </span>
                                </div>
                            </div>
                        {/each}
                    </div>
                {/if}
            </div>
        </div>
    </div>
</div>
