<script>
  import { onMount } from "svelte";
  import { api } from "$lib/api";
  import { page } from "$app/stores";

  let tickets = $state([]);
  let errorMessage = $state("");

  onMount(() => {
    load();
  });

  const load = async () => {
    try {
      const fetchedTickets = await api.getOne("tickets", $page.params.slug);
      tickets = fetchedTickets.sort((a, b) => parseInt(a.id) - parseInt(b.id));
    } catch (error) {
      console.error("API Error:", error); 
      errorMessage = "Failed to load tickets. Please try again.";
    }
  };
</script>

<style>
  .ticket-card {
    transition: all 0.3s ease;
  }
  
  .ticket-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  }
  
  .badge {
    transition: all 0.2s ease;
  }
  
  .badge:hover {
    transform: scale(1.05);
  }
  
  .back-button {
    transition: all 0.2s ease;
  }
  
  .back-button:hover {
    transform: translateX(-2px);
  }
  
  .header-gradient {
    background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
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
  <div class="mb-8 bg-gradient-to-r from-indigo-600 to-purple-600 rounded-lg shadow-lg border border-indigo-200 overflow-hidden text-white">
    <div class="p-6">
      <div class="flex flex-col md:flex-row justify-between md:items-center gap-4">
        <div>
          <h2 class="text-2xl font-bold text-white mb-2 flex items-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-7 w-7 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 110 4v3a2 2 0 002 2h14a2 2 0 002-2v-3a2 2 0 110-4V7a2 2 0 00-2-2H5z" />
            </svg>
            Ticket Viewer
          </h2>
          <p class="text-indigo-100">View your flight tickets for booking #{$page.params.slug}</p>
        </div>
      </div>
    </div>
  </div>

  {#if errorMessage}
    <div
      class="mb-6 p-4 bg-red-50 border-l-4 border-red-500 text-red-700 rounded-md flex items-center shadow-md"
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
        <div class="ticket-card bg-white border border-gray-200 rounded-xl shadow-md overflow-hidden hover:shadow-lg transition-shadow">
          <div class="flex flex-col md:flex-row md:items-center">
            <div class="p-5 flex-grow">
              <div class="flex items-center">
                <div class="flex-grow">
                  <div class="flex justify-between items-center">
                    <h3 class="text-xl font-bold text-gray-800">Flight #{ticket.flightNumber}</h3>
                    <span class={`inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold badge
                      ${ticket.hasSeat ? 'bg-green-100 text-green-800 ring-1 ring-green-300' : 
                      'bg-red-100 text-red-800 ring-1 ring-red-300'}`}>
                      {ticket.hasSeat ? 'Seat Assigned' : 'No Seat'}
                    </span>
                  </div>
                  
                  <div class="mt-4 grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div class="space-y-2">
                      <div class="text-gray-600">
                        <div class="flex items-center mb-1">
                          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 text-indigo-600" viewBox="0 0 20 20" fill="currentColor">
                            <path fill-rule="evenodd" d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z" clip-rule="evenodd" />
                          </svg>
                          <span class="font-semibold">From:</span> {ticket.departureAirport}
                        </div>
                        <div class="ml-7 text-sm text-gray-500">
                          Terminal {ticket.departureTerminal} • Gate {ticket.departureGate}
                        </div>
                        <div class="ml-7 mt-1 text-sm font-medium bg-indigo-50 px-3 py-1.5 rounded-lg inline-block">
                          Departure: {new Date(ticket.departureTime).toLocaleString()}
                        </div>
                      </div>
                    </div>
                    
                    <div class="space-y-2">
                      <div class="text-gray-600">
                        <div class="flex items-center mb-1">
                          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 text-indigo-600" viewBox="0 0 20 20" fill="currentColor">
                            <path fill-rule="evenodd" d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z" clip-rule="evenodd" />
                          </svg>
                          <span class="font-semibold">To:</span> {ticket.arrivalAirport}
                        </div>
                        <div class="ml-7 mt-1 text-sm font-medium bg-indigo-50 px-3 py-1.5 rounded-lg inline-block">
                          Arrival: {new Date(ticket.arrivalTime).toLocaleString()}
                        </div>
                      </div>
                    </div>
                  </div>

                  <div class="mt-5 pt-3 border-t border-gray-200">
                    <div class="text-gray-600 flex items-center">
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 text-indigo-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                      </svg>
                      <span class="font-semibold">Passenger:</span> {ticket.firstName} {ticket.lastName}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      {/each}
    </div>
  {:else}
    <div class="mt-6 p-8 bg-white border border-gray-200 text-gray-600 rounded-lg flex flex-col items-center justify-center shadow-md">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 text-indigo-300 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
      </svg>
      <p class="text-xl font-medium mb-2 text-gray-700">No tickets found</p>
      <p class="text-gray-500 text-center max-w-md">It looks like the tickets are unavailable, or simply just not there.</p>
    </div>
  {/if}

  <div class="mt-8 flex justify-between items-center">
    <a
      href="/home"
      class="back-button inline-flex items-center bg-white border border-gray-300 text-gray-700 hover:bg-gray-50 font-medium py-2.5 px-5 rounded-lg shadow-sm transition-colors"
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
      <div class="bg-indigo-50 px-3 py-2 rounded-lg text-indigo-700 text-sm">
        Showing {tickets.length} ticket{tickets.length !== 1 ? 's' : ''}
      </div>
    {/if}
  </div>
</div>