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

    // Define icons for each card
    const icons = {
        revenue: `<svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>`,
        destination: `<svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
        </svg>`,
        kilometers: `<svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
        </svg>`
    };
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
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: space-between;
        min-height: 180px;
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

    .icon-container {
        margin-bottom: 0.5rem;
    }

    .green-icon { color: #16a34a; }
    .red-icon { color: #dc2626; }
    .blue-icon { color: #2563eb; }

    .trend {
        font-size: 0.9rem;
        margin-top: 0.5rem;
        color: #4b5563;
    }

    .comparison-section {
        margin-top: 3rem;
        padding: 1.5rem;
        background: rgba(255, 255, 255, 0.85);
        backdrop-filter: blur(12px);
        border-radius: 16px;
        box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
    }

    .section-title {
        font-size: 1.5rem;
        font-weight: 600;
        margin-bottom: 1.5rem;
        text-align: center;
        color: #1f2937;
    }

    .simple-chart {
        display: flex;
        align-items: flex-end;
        justify-content: space-around;
        height: 200px;
        gap: 2rem;
        padding: 1rem;
    }

    .bar {
        width: 80px;
        background: linear-gradient(to top,  #10b981, #34d399);
        border-radius: 8px 8px 0 0;
        position: relative;
        min-height: 20%;
        transition: height 1s ease-out;
    }

    .bar:nth-child(2) {
        background: linear-gradient(to top, #3b82f6, #60a5fa);
    }

    .bar-label {
        position: absolute;
        bottom: -25px;
        left: 0;
        right: 0;
        text-align: center;
        font-weight: 500;
    }

    .stats-summary {
        margin-top: 3rem;
        padding: 1.5rem;
        background: rgba(255, 255, 255, 0.85);
        backdrop-filter: blur(12px);
        border-radius: 16px;
        box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
    }

    .summary-title {
        font-size: 1.2rem;
        font-weight: 600;
        margin-bottom: 1rem;
        color: #1f2937;
    }

    .summary-text {
        line-height: 1.6;
        color: #4b5563;
    }
</style>

<div class="dashboard">
    <h1 class="title">KPI Dashboard</h1>

    <div class="cards">
        <div class="card">
            <div class="icon-container green-icon">
                {@html icons.revenue}
            </div>
            <div>
                <h2>Total Revenue</h2>
                <p class="green">{formatNumber(totalRevenue)}€</p>
                <div class="trend">↑ Growing metric</div>
            </div>
        </div>

        <div class="card">
            <div class="icon-container red-icon">
                {@html icons.destination}
            </div>
            <div>
                <h2>Most Booked Destination</h2>
                <p class="red">{mostBookedDestination}</p>
                <div class="trend">Most popular choice</div>
            </div>
        </div>

        <div class="card">
            <div class="icon-container blue-icon">
                {@html icons.kilometers}
            </div>
            <div>
                <h2>Total Kilometers Traveled</h2>
                <p class="blue">{formatNumber(totalKilometers)} Km</p>
                <div class="trend">Distance covered</div>
            </div>
        </div>
    </div>

    <!-- Comparison section -->
    <div class="comparison-section">
        <h2 class="section-title">Key Metrics Comparison</h2>
        <div class="simple-chart">
            <div class="bar" style="height: {Math.min(totalRevenue / 1000, 100)}%">
                <span class="bar-label">Revenue (€)</span>
            </div>
            <div class="bar" style="height: {Math.min(totalKilometers / 1000, 100)}%">
                <span class="bar-label">Distance (Km)</span>
            </div>
        </div>
    </div>

    <!-- Statistics summary -->
    <div class="stats-summary">
        <h3 class="summary-title">Performance Insights</h3>
        <p class="summary-text">Our company has generated a total revenue of <strong>{formatNumber(totalRevenue)}€</strong></p>
        <p class="summary-text"><strong>{mostBookedDestination}</strong> being the most popular destination among our customers.</p>
        <p class="summary-text"> The total distance traveled is <strong>{formatNumber(totalKilometers)} Km</strong></p>
    </div>
</div>