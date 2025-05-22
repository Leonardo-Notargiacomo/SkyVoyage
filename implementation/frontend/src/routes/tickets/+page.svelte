<script>
  import { onMount } from "svelte";
  import { api } from "$lib/api";

  let tickets = $state([]);
  let errorMessage = $state("");

  // For now, bookingID is hardcoded
  let bookingID = 1;

  onMount(() => {
    load(bookingID);
  });

  const load = async (bookingID) => {
    try {
      const fetchedTickets = await api.getOne(`/tickets/${bookingID}`);
      // Sort tickets by ID (smallest first)
      tickets = fetchedTickets.sort((a, b) => parseInt(a.id) - parseInt(b.id));
    } catch (error) {
      console.error("API Error:", error); // Added this for debugging
      errorMessage = "Failed to load tickets. Please try again.";
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

<nav class="flex my-4" aria-label="Breadcrumb">
  <ol class="inline-flex items-center space-x-2">
    <li class="inline-flex items-center">
      <a
        href="/home"
        class="inline-flex items-center text-sm font-medium text-gray-600 hover:text-indigo-500 transition-colors"
      >
        <svg
          class="w-4 h-4 me-2"
          aria-hidden="true"
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 20 20"
          fill="currentColor"
        >
          <path
            d="m19.707 9.293-2-2-7-7a1 1 0 0 0-1.414 0l-7 7-2 2a1 1 0 0 0 1.414 1.414L2 10.414V18a2 2 0 0 0 2 2h3a1 1 0 0 0 1-1v-4a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1v4a1 1 0 0 0 1 1h3a2 2 0 0 0 2-2v-7.586l.293.293a1 1 0 0 0 1.414-1.414Z"
          />
        </svg>
        Dashboard
      </a>
    </li>
    <li>
      <div class="flex items-center">
        <svg
          class="w-4 h-4 text-gray-400 mx-1"
          aria-hidden="true"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 6 10"
        >
          <path
            stroke="currentColor"
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="m1 9 4-4-4-4"
          />
        </svg>
        <span class="ms-1 text-sm font-medium text-gray-400">Tickets</span>
      </div>
    </li>
  </ol>
</nav>

<div class="max-w-6xl mx-auto">
  <div class="mb-8 bg-gradient-to-r from-indigo-50 to-indigo-100 rounded-lg shadow-sm border border-indigo-200 overflow-hidden">
    <div class="p-6">
      <div class="flex flex-col md:flex-row justify-between md:items-center gap-4">
        <div>
          <h2 class="text-2xl font-bold text-indigo-900 mb-2">Ticket Management</h2>
          <p class="text-indigo-700">View and manage support tickets</p>
        </div>
      </div>
    </div>
  </div>

  {#if errorMessage}
    <div
      class="mb-6 p-4 bg-red-50 border-l-4 border-red-500 text-red-700 rounded-md flex items-center"
      role="alert"
    >
      <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-3 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
      </svg>
      {errorMessage}
    </div>
  {/if}

  {#if tickets.length > 0}
    <div class="grid gap-6 grid-cols-1">
      {#each tickets as ticket}
        <div class="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-shadow">
          <div class="flex flex-col md:flex-row md:items-center">
            <div class="p-5 flex-grow">
              <div class="flex items-center">
                <div class="flex-grow">
                  <h3 class="text-xl font-bold text-gray-800">Ticket #{ticket.id}</h3>
                  
                  <div class="mt-1.5 flex items-center">
                    <span class={`inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold 
                      ${ticket.Status === 'Open' ? 'bg-green-100 text-green-800 ring-1 ring-green-300' : 
                      ticket.Status === 'In Progress' ? 'bg-blue-100 text-blue-800 ring-1 ring-blue-300' : 
                      'bg-gray-100 text-gray-800 ring-1 ring-gray-300'}`}>
                      {ticket.Status}
                    </span>
                  </div>
                  
                  <div class="mt-3">
                    <p class="text-gray-600">{ticket.Description}</p>
                  </div>

                  <div class="mt-3 flex items-center gap-4">
                    <div class="inline-flex items-center text-sm text-gray-600">
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1.5 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                      </svg>
                      Created: {new Date(ticket.CreatedAt).toLocaleDateString()}
                    </div>
                    <div class="inline-flex items-center text-sm text-gray-600">
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1.5 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                      </svg>
                      Assigned to: {ticket.AssignedTo || 'Unassigned'}
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="p-5 md:p-6 flex justify-center md:justify-end items-center">
              <a
                href="/tickets/{ticket.id}"
                class="w-full md:w-auto bg-white text-blue-600 border-2 border-blue-600 hover:bg-blue-600 hover:text-white font-medium py-2.5 px-5 rounded-lg shadow-sm transition-all duration-200 text-center"
              >
                <span class="flex items-center justify-center">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                  </svg>
                  View Details
                </span>
              </a>
            </div>
          </div>
        </div>
      {/each}
    </div>
  {:else}
    <div class="mt-6 p-6 bg-gray-50 border border-gray-200 text-gray-600 rounded-lg flex flex-col items-center justify-center">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 text-gray-400 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
      </svg>
      <p class="text-lg font-medium mb-2">No tickets found</p>
      <p class="text-gray-500 text-center">There are currently no support tickets in the system.</p>
    </div>
  {/if}

  <div class="mt-8 flex justify-between items-center">
    <a
      href="/home"
      class="inline-flex items-center bg-white border border-gray-300 text-gray-700 hover:bg-gray-50 font-medium py-2.5 px-5 rounded-lg shadow-sm transition-colors"
    >
      <svg
        xmlns="http://www.w3.org/2000/svg"
        class="h-5 w-5 mr-2"
        viewBox="0 0 20 20"
        fill="currentColor"
      >
        <path
          fill-rule="evenodd"
          d="M9.707 16.707a1 1 0 01-1.414 0l-6-6a1 1 0 010-1.414l6-6a1 1 0 011.414 1.414L5.414 9H17a1 1 0 110 2H5.414l4.293 4.293a1 1 0 010 1.414z"
          clip-rule="evenodd"
        />
      </svg>
      Back to Dashboard
    </a>

    {#if tickets.length > 0}
      <div class="text-sm text-gray-500">
        Showing {tickets.length} ticket{tickets.length !== 1 ? 's' : ''}
      </div>
    {/if}
  </div>
</div>