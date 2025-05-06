<script>
    import {onMount} from "svelte";
    let totalRevenue;
    let mostBookedDestination;
    let totalKilometers;

    // Format numbers with a dot as thousands separator
    function formatNumber(num) {
        return new Intl.NumberFormat('de-DE', {
            useGrouping: true,
            maximumFractionDigits: 0
        }).format(num);
    }

    onMount(async () => {
        const res = await fetch("http://localhost:8080/api/v1/kpi");
        const data = await res.json();

       totalRevenue = data.totalRevenue;
       mostBookedDestination = data.topDestination;
       totalKilometers = data.totalKilometer;
    });
</script>

<div>
    <h1 class="text-3xl font-semibold mb-6 text-center text-gray-800">KPI Dashboard</h1>

    <div class="grid gap-6 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3">

        <div class="bg-white p-6 rounded-2xl shadow hover:shadow-md transition text-center">
            <h2 class="text-lg font-medium text-gray-700">Total Revenue</h2>
            <p class="text-2xl font-semibold text-green-600 mt-2">{formatNumber(totalRevenue)}€</p>
        </div>

        <div class="bg-white p-6 rounded-2xl shadow hover:shadow-md transition text-center">
            <h2 class="text-lg font-medium text-gray-700">Most Booked Destination</h2>
                    <p class="text-2xl font-semibold text-red-600 mt-2">{mostBookedDestination}</p>
        </div>

        <div class="bg-white p-6 rounded-2xl shadow hover:shadow-md transition text-center">
            <h2 class="text-lg font-medium text-gray-700">Total Kilometers Traveled</h2>
            <p class="text-2xl font-semibold text-blue-600 mt-2">{formatNumber(totalKilometers)} Km</p>
        </div>

    </div>
</div>
