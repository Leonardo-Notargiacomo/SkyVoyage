<script>
    let totalContainers = 3; // Hardcoded number of containers
    let variables = [
        ["Value 1.1", "", "Value 1.3", "Value 1.4", "Value 1.5", "", "Value 1.7", "Value 1.8", "Value 1.9"],
        ["Value 2.1", "Value 2.2", "Value 2.3", "Value 2.4", "Value 2.5", "Value 2.6", "Value 2.7", "Value 2.8", "Value 2.9"],
        ["Value 3.1", "Value 3.2", "Value 3.3", "Value 3.4", "Value 3.5", "Value 3.6", "Value 3.7", "Value 3.8", "Value 3.9"]
    ]; // Placeholder for external data
    let boxNames = ["Name 1", "Name 2", "Name 3", "Name 4", "Name 5", "Name 6", "Name 7", "Name 8", "Name 9"];
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
        grid-template-columns: repeat(3, 1fr);
        grid-gap: 10px;
        width: 95%;
        height: 300px;
        margin: auto;
        border: 2px solid #000;
        padding: 10px;
        background-color: #ffffff; /* White background for grid */
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

{#each variables as containerVariables, containerIndex}
    <div class="container-wrapper">
        <h2 class="container-title">Ticket {containerIndex + 1}</h2>
        <div class="grid-container">
            {#each containerVariables as variable, index}
                <div class="grid-item">
                    <div class="name-label">{boxNames[index]}</div>
                    <div class="variable-display">
                        {variable || "Undisclosed"}
                    </div>
                </div>
            {/each}
        </div>
    </div>
{/each}