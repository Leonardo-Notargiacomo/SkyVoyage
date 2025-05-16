<script>
  import { onMount, onDestroy } from 'svelte';
  import { api } from "$lib/api";
  import { goto } from "$app/navigation";

  // Remove $state usage, use normal Svelte reactivity
  let tickets = [];

  onMount(() => {
    if (typeof window !== "undefined") {
      window.addEventListener("keydown", handleKeyEvent);
    }
    load('1'); // Pass the ID here
  });

  onDestroy(() => {
    if (typeof window !== "undefined") {
      window.removeEventListener("keydown", handleKeyEvent);
    }
  });

  // Accept an id parameter and use it in the API call
  const load = async (id) => {
    const fetchedTickets = await api.list(`/tickets/`);
    tickets = fetchedTickets ? (Array.isArray(fetchedTickets) ? fetchedTickets : [fetchedTickets]) : [];
  };

  const handleKeyEvent = (e) => {
    if (e.key === "Escape" && isOpen) {
      isOpen = false;
    }
  };
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

{#if tickets.length === 0}
  <div>Loading tickets...</div>
{:else}
  {#each tickets as ticket, containerIndex}
    <div class="container-wrapper">
      <h2 class="container-title">Ticket {containerIndex + 1}</h2>
      <div class="grid-container">
        <div class="grid-item" style="grid-column: span 4; text-align: center; font-weight: bold;">
          {ticket.type1Name}
        </div>
        {#each ticket.boxNames.slice(0, ticket.type1Count) as boxName, index}
          <div class="grid-item">
            <div class="name-label">{boxName}</div>
            <div class="variable-display">
              {ticket.variables?.[index] || "Undisclosed"}
            </div>
          </div>
        {/each}
        <div class="grid-item" style="grid-column: span 4; text-align: center; font-weight: bold;">
          {ticket.type2Name}
        </div>
        {#each ticket.boxNames.slice(ticket.type1Count) as boxName, index}
          <div class="grid-item">
            <div class="name-label">{boxName}</div>
            <div class="variable-display">
              {ticket.variables?.[index + ticket.type1Count] || "Undisclosed"}
            </div>
          </div>
        {/each}
      </div>
    </div>
  {/each}
{/if}