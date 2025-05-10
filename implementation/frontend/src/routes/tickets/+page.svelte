<script>
    let totalContainers = 3; // Hardcoded number of containers
    let variables = [
        ["Value 1.1", "", "Value 1.3", "Value 1.4", "Value 1.5", "", "Value 1.7", "Value 1.8", "Value 1.9", "Value 1.10"],
        ["Value 2.1", "Value 2.2", "Value 2.3", "Value 2.4", "Value 2.5", "Value 2.6", "Value 2.7", "Value 2.8", "Value 2.9", "Value 2.10"],
        ["Value 3.1", "Value 3.2", "Value 3.3", "Value 3.4", "Value 3.5", "Value 3.6", "Value 3.7", "Value 3.8", "Value 3.9", "Value 3.10"]
    ]; // Placeholder for external data
    let boxNames = ["Flight Number", "Departure airport", "Departure terminal", "Departure gate", "Departure time", "Departure delay", "Arrival airport", "Arrival terminal", "Arrival gate", "Arrival time", "Arrival delay", "Has a Seat", "First name", "Last name", "e-mail", "Phone number", "Country", "City", "Streetname", "House number"]; 
    let type1Name = "Flight information";
    let type2Name = "Personal information";
    let type1Count = 12; // Number of variables in Type 1
</script>

<style>
    .container-wrapper {
        margin-bottom: 20px; /* Add gap between containers */
        background-color: #85ebf8; /* Light blue for containers */
        border-radius: 8px;
        padding: 10px;
    }

    .grid-container {
        display: grid;
        grid-template-columns: repeat(4, 1fr); /* Change to 4 columns */
        grid-gap: 10px;
        width: 95%;
        margin: auto;
        border: 2px solid #000;
        padding: 10px;
        background-color: #ffffff; /* White background for grid */
        height: auto; /* Allow container to grow dynamically */
    }

    .grid-item {
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        border: 1px solid #ccc;
        background-color: #91ff82; /* Distinct yellow for variable fields */
        font-size: 1.2rem;
        height: 100%;
        text-align: center;
        border-radius: 4px;
        padding-bottom: 10px; /* Add padding to prevent touching the bottom */
    }

    .variable-display {
        width: 90%;
        height: auto; /* Adjust height to fit content */
        text-align: center;
        font-size: 1rem;
        background-color: #c94256; /* Slightly darker red for variable content */
        border: 1px solid #ccc;
        padding: 5px;
        box-sizing: border-box;
        border-radius: 4px;
    }

    .name-label {
        font-size: 1rem;
        margin-bottom: 5px;
        font-weight: bold;
        color: #000000; /* Black for text */
    }

    .container-title {
        font-size: 1.5rem;
        text-align: center;
        margin-bottom: 10px;
        font-weight: bold;
        color: #000000; /* Black for container titles */
    }
</style>

{#each Array(totalContainers).fill(0).map((_, i) => i) as containerIndex}
    <div class="container-wrapper">
        <h2 class="container-title">Ticket {containerIndex + 1}</h2>
        <div class="grid-container">
            <div class="grid-item" style="grid-column: span 4; text-align: center; font-weight: bold;">
                {type1Name}
            </div>
            {#each boxNames.slice(0, type1Count) as boxName, index}
                <div class="grid-item">
                    <div class="name-label">{boxName}</div>
                    <div class="variable-display">
                        {variables[containerIndex]?.[index] || "Undisclosed"}
                    </div>
                </div>
            {/each}
            <div class="grid-item" style="grid-column: span 4; text-align: center; font-weight: bold;">
                {type2Name}
            </div>
            {#each boxNames.slice(type1Count) as boxName, index}
                <div class="grid-item">
                    <div class="name-label">{boxName}</div>
                    <div class="variable-display">
                        {variables[containerIndex]?.[index + type1Count] || "Undisclosed"}
                    </div>
                </div>
            {/each}
        </div>
    </div>
{/each}