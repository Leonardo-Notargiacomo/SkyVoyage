<script>
    import {onMount} from "svelte";
    import {api} from "$lib/api.js";

    let totalRevenue;
    let mostBookedDestination;
    let totalKilometers;
    let errorMessage = "";

    // Format numbers with a dot as thousands separator
    function formatNumber(num) {
        return new Intl.NumberFormat('de-DE', {
            useGrouping: true,
            maximumFractionDigits: 0
        }).format(num);
    }

    onMount(async () => {
        try {
            const data = await api.fetchAPI("kpi");
            console.log("KPI data:", data);

            if (!data || data.totalRevenue == null || data.topDestination == null || data.totalKilometer == null) {
                errorMessage = "Failed to load KPI data. Please try again later.";
                return;
            }
            totalRevenue = data.totalRevenue;
            mostBookedDestination = data.topDestination;
            totalKilometers = data.totalKilometer;
        } catch (e) {
            errorMessage = "Failed to load KPI data. Please try again later.";
        }
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
        font-size: 2.2rem;
        font-weight: 700;
        text-align: center;
        color: #1f2937;
        margin-bottom: 2.5rem;
        background: linear-gradient(to right, #2563eb, #1d4ed8);
        -webkit-background-clip: text;
        background-clip: text;
        color: transparent;
        position: relative;
        display: inline-block;
        left: 50%;
        transform: translateX(-50%);
        padding-bottom: 0.5rem;
    }

    .title::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 60%;
        height: 3px;
        background: linear-gradient(to right, #2563eb, #1d4ed8);
        border-radius: 2px;
    }

    .cards {
        display: grid;
        gap: 1.5rem;
        grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    }

    .card {
        background: rgba(255, 255, 255, 0.95);
        backdrop-filter: blur(20px);
        border-radius: 16px;
        box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08);
        padding: 1.75rem;
        text-align: center;
        transition: all 0.3s ease;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: space-between;
        min-height: 200px;
        border: 1px solid rgba(255, 255, 255, 0.4);
        overflow: hidden;
        position: relative;
    }

    .card::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: linear-gradient(135deg, rgba(255,255,255,0.2) 0%, rgba(255,255,255,0) 100%);
        z-index: 0;
    }

    .card:hover {
        transform: translateY(-6px);
        box-shadow: 0 15px 35px rgba(0, 0, 0, 0.12);
    }

    .card h2 {
        font-size: 1.1rem;
        font-weight: 700;
        color: #374151;
        margin-bottom: 0.5rem;
        position: relative;
        z-index: 1;
    }

    .card p {
        font-size: 2rem;
        font-weight: 700;
        margin: 0.75rem 0 0;
        position: relative;
        z-index: 1;
    }

    .green { 
        color: #16a34a;
        text-shadow: 0 0 20px rgba(22, 163, 74, 0.1);
    }
    
    .red { 
        color: #dc2626;
        text-shadow: 0 0 20px rgba(220, 38, 38, 0.1);
    }
    
    .blue { 
        color: #2563eb; 
        text-shadow: 0 0 20px rgba(37, 99, 235, 0.1);
    }

    .icon-container {
        margin-bottom: 1rem;
        padding: 1rem;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        position: relative;
        z-index: 1;
    }

    .green-icon { 
        background: linear-gradient(135deg, #dcfce7, #bbf7d0);
        color: #16a34a;
    }
    
    .red-icon { 
        background: linear-gradient(135deg, #fee2e2, #fecaca);
        color: #dc2626; 
    }
    
    .blue-icon { 
        background: linear-gradient(135deg, #dbeafe, #bfdbfe);
        color: #2563eb; 
    }

    .trend {
        font-size: 0.9rem;
        margin-top: 0.75rem;
        color: #6b7280;
        font-weight: 500;
        position: relative;
        z-index: 1;
    }

    .comparison-section {
        margin-top: 3.5rem;
        padding: 2rem;
        background: rgba(255, 255, 255, 0.95);
        backdrop-filter: blur(20px);
        border-radius: 16px;
        box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08);
        border: 1px solid rgba(255, 255, 255, 0.4);
        position: relative;
        overflow: hidden;
    }

    .comparison-section::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        height: 6px;
    }

    .section-title {
        font-size: 1.5rem;
        font-weight: 700;
        margin-bottom: 2rem;
        text-align: center;
        color: #1f2937;
    }

    .simple-chart {
        display: flex;
        align-items: flex-end;
        justify-content: center;
        height: 250px;
        gap: 3rem;
        padding: 1rem;
        position: relative;
    }

    .simple-chart::before {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50px;
        right: 50px;
        height: 1px;
        background: #e5e7eb;
    }

    .bar {
        width: 100px;
        background: linear-gradient(to top, #10b981, #34d399);
        border-radius: 12px 12px 0 0;
        position: relative;
        min-height: 20%;
        transition: all 1s cubic-bezier(0.34, 1.56, 0.64, 1);
        box-shadow: 0 6px 16px rgba(16, 185, 129, 0.2);
    }

    .bar:nth-child(2) {
        background: linear-gradient(to top, #3b82f6, #60a5fa);
        box-shadow: 0 6px 16px rgba(59, 130, 246, 0.2);
    }

    .bar-label {
        position: absolute;
        bottom: -30px;
        left: 0;
        right: 0;
        text-align: center;
        font-weight: 600;
        color: #4b5563;
    }

    .stats-summary {
        margin-top: 3.5rem;
        padding: 2rem;
        background: rgba(255, 255, 255, 0.95);
        backdrop-filter: blur(20px);
        border-radius: 16px;
        box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08);
        border: 1px solid rgba(255, 255, 255, 0.4);
        position: relative;
        overflow: hidden;
    }

    .stats-summary::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        height: 6px;

    }

    .summary-title {
        font-size: 1.3rem;
        font-weight: 700;
        margin-bottom: 1.5rem;
        color: #1f2937;
    }

    .summary-text {
        line-height: 1.8;
        color: #4b5563;
        font-size: 1.05rem;
    }
</style>

{#if errorMessage}
    <div class="bg-red-50 border border-red-200 text-red-700 px-6 py-4 rounded-lg shadow-md mx-auto my-6 max-w-2xl text-center">
        <div class="flex items-center justify-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span class="font-semibold">{errorMessage}</span>
        </div>
    </div>
{/if}

<div class="dashboard">
    <h1 class="title">Key Performance Indicators</h1>

    <div class="cards">
        <div class="card">
            <div class="icon-container green-icon">
                {@html icons.revenue}
            </div>
            <div>
                <h2>Total Revenue</h2>
                <p class="green">{formatNumber(totalRevenue)}€</p>
                <div class="trend">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 inline-block mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
                    </svg>
                    Growing metric
                </div>
            </div>
        </div>

        <div class="card">
            <div class="icon-container red-icon">
                {@html icons.destination}
            </div>
            <div>
                <h2>Most Booked Destination</h2>
                <p class="red">{mostBookedDestination}</p>
                <div class="trend">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 inline-block mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z" />
                    </svg>
                    Most popular choice
                </div>
            </div>
        </div>

        <div class="card">
            <div class="icon-container blue-icon">
                {@html icons.kilometers}
            </div>
            <div>
                <h2>Total Kilometers Traveled</h2>
                <p class="blue">{formatNumber(totalKilometers)} Km</p>
                <div class="trend">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 inline-block mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7" />
                    </svg>
                    Distance covered
                </div>
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