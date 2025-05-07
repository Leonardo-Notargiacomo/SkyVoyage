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

<style>
    .dashboard {
        max-width: 1200px;
        margin: 0 auto;
        padding: 2rem 1rem;
        font-family: system-ui, sans-serif;
    }

    .title {
        font-size: 2rem;
        font-weight: 600;
        text-align: center;
        color: #1f2937;
        margin-bottom: 2rem;
    }

    .cards {
        display: grid;
        gap: 1.5rem;
        grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
    }

    .card {
        background: rgba(255, 255, 255, 0.85);
        backdrop-filter: blur(12px);
        border-radius: 16px;
        box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
        padding: 1.5rem;
        text-align: center;
        transition: transform 0.2s ease, box-shadow 0.2s ease;
    }

    .card:hover {
        transform: translateY(-4px);
        box-shadow: 0 12px 30px rgba(0, 0, 0, 0.1);
    }

    .card h2 {
        font-size: 1rem;
        font-weight: 700;
        color: #374151;
        margin-bottom: 0.5rem;
    }

    .card p {
        font-size: 1.8rem;
        font-weight: 600;
        margin: 0.5rem 0 0;
    }

    .green { color: #16a34a; }
    .red { color: #dc2626; }
    .blue { color: #2563eb; }
</style>

<div class="dashboard">
    <h1 class="title">KPI Dashboard</h1>

    <div class="cards">
        <div class="card">
            <h2>Total Revenue</h2>
            <p class="green">{formatNumber(totalRevenue)}€</p>
        </div>

        <div class="card">
            <h2>Most Booked Destination</h2>
            <p class="red">{mostBookedDestination}</p>
        </div>

        <div class="card">
            <h2>Total Kilometers Traveled</h2>
            <p class="blue">{formatNumber(totalKilometers)} Km</p>
        </div>
    </div>
</div>
