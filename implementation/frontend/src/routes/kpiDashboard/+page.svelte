<script>
    let totalRevenue = "$1,234,567.89";
    let mostBookedDestinations = ["Paris", "Tokyo", "New York"];
    let totalKilometersTraveled = "987,654 km";
</script>

<div>
    <h1 class="text-3xl font-semibold mb-6 text-center text-gray-800">KPI Dashboard</h1>

    <div class="grid gap-6 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3">

        <div class="bg-white p-6 rounded-2xl shadow hover:shadow-md transition text-center">
            <h2 class="text-lg font-medium text-gray-700">Total Revenue</h2>
            <p class="text-2xl font-semibold text-green-600 mt-2">{totalRevenue}</p>
        </div>

        <div class="bg-white p-6 rounded-2xl shadow hover:shadow-md transition text-center">
            <h2 class="text-lg font-medium text-gray-700">Most Booked Destinations</h2>
            <ul class="mt-3 space-y-1 text-gray-600 list-disc list-inside inline-block text-left">
                {#each mostBookedDestinations as destination}
                    <li>{destination}</li>
                {/each}
            </ul>
        </div>

        <div class="bg-white p-6 rounded-2xl shadow hover:shadow-md transition text-center">
            <h2 class="text-lg font-medium text-gray-700">Total Kilometers Traveled</h2>
            <p class="text-2xl font-semibold text-blue-600 mt-2">{totalKilometersTraveled}</p>
        </div>

    </div>
</div>
