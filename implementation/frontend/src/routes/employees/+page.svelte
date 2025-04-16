<script>
  import { onMount, onDestroy } from "svelte";
  import { api } from "$lib/api";
  import { goto } from "$app/navigation";

  let isOpen = $state(false);
  let newEmployee = $state({
    Firstname: "",
    Lastname: "",
    Email: "",
    Password: "",
    Type: "SalesEmployee",
  });
  let employees = $state([]);
  let options = ["SalesEmployee", "SalesManager", "AccountManager"];
  let errorMessage = $state("");

  let validationErrors = $state({
    firstname: "",
    lastname: "",
    email: "",
    password: "",
    type: "",
  });

  onMount(() => {
    if (typeof window !== "undefined") {
      window.addEventListener("keydown", handleKeyEvent);
    }
    load();
  });

  onDestroy(() => {
    if (typeof window !== "undefined") {
      window.removeEventListener("keydown", handleKeyEvent);
    }
  });

  const load = async () => {
    const fetchedEmployees = await api.all("/employees");
    // Sort employees by ID (smallest first)
    employees = fetchedEmployees.sort((a, b) => {
      // Convert to number to ensure proper numerical sorting
      return parseInt(a.id) - parseInt(b.id);
    });
  };

  const handleKeyEvent = (e) => {
    if (e.key === "Escape" && isOpen) {
      isOpen = false;
    }
  };

  const createEmployee = async (e) => {
    e.preventDefault();
    errorMessage = "";

    Object.keys(validationErrors).forEach((key) => {
      validationErrors[key] = "";
    });

    try {
      const response = await api.create(
        "/employees",
        JSON.stringify($state.snapshot(newEmployee))
      );

      newEmployee.Firstname = "";
      newEmployee.Lastname = "";
      newEmployee.Email = "";
      newEmployee.Password = "";
      newEmployee.Type = "SalesEmployee";

      await load();
      isOpen = false;
    } catch (error) {
      if (error.errorData && error.errorData.validationErrors) {
        const errors = error.errorData.validationErrors;

        Object.keys(errors).forEach((key) => {
          const lowerKey = key.toLowerCase();
          if (lowerKey === "firstname" || lowerKey === "first_name")
            validationErrors.firstname = errors[key];
          if (lowerKey === "lastname" || lowerKey === "last_name")
            validationErrors.lastname = errors[key];
          if (lowerKey === "email") validationErrors.email = errors[key];
          if (lowerKey === "password") validationErrors.password = errors[key];
          if (lowerKey === "type") validationErrors.type = errors[key];
        });

        errorMessage = "Please correct the errors below.";
      } else if (error.errorData && error.errorData.error) {
        const errorMsg = error.errorData.error;

        if (errorMsg.includes("name")) {
          if (errorMsg.toLowerCase().includes("first")) {
            validationErrors.firstname = errorMsg;
          } else if (errorMsg.toLowerCase().includes("last")) {
            validationErrors.lastname = errorMsg;
          } else {
            validationErrors.firstname = errorMsg;
            validationErrors.lastname = errorMsg;
          }
        } else if (errorMsg.includes("email")) {
          validationErrors.email = errorMsg;
        } else if (errorMsg.includes("Password")) {
          validationErrors.password = errorMsg;
        } else if (
          errorMsg.includes("type") ||
          errorMsg.includes("employee type")
        ) {
          validationErrors.type = errorMsg;
        } else {
          errorMessage = `Error: ${errorMsg}`;
        }
      } else {
        errorMessage = "Failed to create employee. Please try again.";
      }
    }
  };
</script>

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
        <span class="ms-1 text-sm font-medium text-gray-400">Employees</span>
      </div>
    </li>
  </ol>
</nav>

<div class="max-w-6xl mx-auto">
  <div class="mb-8 bg-gradient-to-r from-indigo-50 to-indigo-100 rounded-lg shadow-sm border border-indigo-200 overflow-hidden">
    <div class="p-6">
      <div class="flex flex-col md:flex-row justify-between md:items-center gap-4">
        <div>
          <h2 class="text-2xl font-bold text-indigo-900 mb-2">Employee Management</h2>
          <p class="text-indigo-700">Manage your team members and their access roles</p>
        </div>
        <button
          onclick={() => (isOpen = !isOpen)}
          class="bg-indigo-600 hover:bg-indigo-700 text-white font-medium py-3 px-5 rounded-lg shadow-md transition-all duration-200 transform hover:-translate-y-0.5 flex items-center self-start"
          type="button"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
          </svg>
          Add New Employee
        </button>
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

  {#if employees.length > 0}
    <div class="grid gap-6 grid-cols-1">
      {#each employees as employee}
        <div class="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-shadow">
          <div class="flex flex-col md:flex-row md:items-center">
            <div class="p-5 flex-grow">
              <div class="flex items-center">
                <div class="relative flex-shrink-0 inline-flex items-center justify-center w-14 h-14 overflow-hidden bg-gradient-to-br from-blue-700 to-blue-900 rounded-full text-white shadow-md mr-4">
                  <span class="font-bold text-lg">
                  {(employee.Firstname.charAt(0) || '').toUpperCase() + (employee.Lastname.charAt(0) || '').toUpperCase()}
                  </span>
                </div>
                <div class="flex-grow">
                  <h3 class="text-xl font-bold text-gray-800">{employee.Firstname} {employee.Lastname}</h3>
                  
                  <div class="mt-1.5 flex items-center">
                    <span class={`inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold 
                      ${employee.Type === 'SalesManager' ? 'bg-purple-100 text-purple-800 ring-1 ring-purple-300' : 
                      employee.Type === 'AccountManager' ? 'bg-emerald-100 text-emerald-800 ring-1 ring-emerald-300' : 
                      'bg-indigo-100 text-indigo-800 ring-1 ring-indigo-300'}`}>
                      {#if employee.Type === 'SalesManager'}
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-3.5 w-3.5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                        </svg>
                      {:else if employee.Type === 'AccountManager'}
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-3.5 w-3.5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2z" />
                        </svg>
                      {:else}
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-3.5 w-3.5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                        </svg>
                      {/if}
                      {employee.Type.replace(/([A-Z])/g, " $1").trim()}
                    </span>
                  </div>
                  
                  <div class="mt-3 flex items-center">
                    <div class="inline-flex items-center px-2.5 py-1 bg-gray-100 rounded text-sm text-gray-600">
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1.5 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                      </svg>
                      <span class="truncate max-w-[220px]">{employee.Email}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="p-5 md:p-6 flex justify-center md:justify-end items-center">
              <a
              href="/employees/{employee.id}"
              class="w-full md:w-auto bg-white text-green-600 border-2 border-green-600 hover:bg-green-600 hover:text-white font-medium py-2.5 px-5 rounded-lg shadow-sm transition-all duration-200 text-center"
              >
              <span class="flex items-center justify-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
                Manage
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
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 7a4 4 0 1 0 0-8 4 4 0 0 0 0 8zM23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" />
      </svg>
      <p class="text-lg font-medium mb-2">No employees found</p>
      <p class="text-gray-500 text-center mb-4">Your employee list is currently empty. Add employees to get started.</p>
      <button
        onclick={() => (isOpen = !isOpen)}
        class="bg-indigo-600 hover:bg-indigo-700 text-white font-medium py-2 px-5 rounded-lg shadow-md flex items-center"
        type="button"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
        </svg>
        Add First Employee
      </button>
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

    {#if employees.length > 0}
      <div class="text-sm text-gray-500">
        Showing {employees.length} employee{employees.length !== 1 ? 's' : ''}
      </div>
    {/if}
  </div>
</div>

<div
  onclick={() => (isOpen = !isOpen)}
  id="crud-modal"
  tabindex="-1"
  aria-hidden="true"
  class="{isOpen
    ? ''
    : 'hidden'} backdrop-blur-sm bg-gray-900/50 flex overflow-y-auto overflow-x-hidden fixed top-0 right-0 left-0 z-50 justify-center items-center w-full md:inset-0 h-screen"
>
  <div
    role="none"
    class="relative p-4 w-full max-w-md max-h-full"
    onclick={(event) => event.stopPropagation()}
  >
    <div class="relative bg-white rounded-xl shadow-2xl animate-fadeIn">
      <div class="bg-gradient-to-r from-blue-600 to-blue-800 p-5 rounded-t-xl flex justify-between items-center">
        <h3 class="text-xl font-bold text-white flex items-center">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
          </svg>
          Create New Employee
        </h3>
        <button
          onclick={() => (isOpen = false)}
          type="button"
          class="text-white/70 hover:text-white bg-white/10 hover:bg-white/20 rounded-lg text-sm w-8 h-8 inline-flex justify-center items-center transition-all duration-200"
        >
          <svg
            class="w-3 h-3"
            aria-hidden="true"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 14 14"
          >
            <path
              stroke="currentColor"
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="m1 1 6 6m0 0 6 6M7 7l6-6M7 7l-6 6"
            />
          </svg>
          <span class="sr-only">Close modal</span>
        </button>
      </div>
      <form class="p-6" onsubmit={createEmployee}>
        <div class="space-y-5">
          <div>
            <label
              for="Firstname"
              class="block mb-2 text-sm font-medium text-gray-700"
              >First Name</label
            >
            <div class="relative">
              <div class="absolute inset-y-0 start-0 flex items-center ps-3.5 pointer-events-none">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
              </div>
              <input
                type="text"
                name="Firstname"
                id="Firstname"
                placeholder="Enter first name"
                required=""
                bind:value={newEmployee.Firstname}
                class="w-full ps-10 px-4 py-2.5 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 outline-none {validationErrors.firstname ? 'border-red-500 bg-red-50' : ''}"
              />
            </div>
            {#if validationErrors.firstname}
              <p class="mt-1 text-sm text-red-600 flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
                {validationErrors.firstname}
              </p>
            {/if}
          </div>
          
          <div>
            <label
              for="Lastname"
              class="block mb-2 text-sm font-medium text-gray-700"
              >Last Name</label
            >
            <div class="relative">
              <div class="absolute inset-y-0 start-0 flex items-center ps-3.5 pointer-events-none">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
              </div>
              <input
                type="text"
                name="Lastname"
                id="Lastname"
                placeholder="Enter last name"
                required=""
                bind:value={newEmployee.Lastname}
                class="w-full ps-10 px-4 py-2.5 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 outline-none {validationErrors.lastname ? 'border-red-500 bg-red-50' : ''}"
              />
            </div>
            {#if validationErrors.lastname}
              <p class="mt-1 text-sm text-red-600 flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
                {validationErrors.lastname}
              </p>
            {/if}
          </div>
          
          <div>
            <label
              for="Email"
              class="block mb-2 text-sm font-medium text-gray-700"
              >Email</label
            >
            <div class="relative">
              <div class="absolute inset-y-0 start-0 flex items-center ps-3.5 pointer-events-none">
                <svg class="w-4 h-4 text-gray-500" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="currentColor" viewBox="0 0 20 16">
                  <path d="m10.036 8.278 9.258-7.79A1.979 1.979 0 0 0 18 0H2A1.987 1.987 0 0 0 .641.541l9.395 7.737Z"/>
                  <path d="M11.241 9.817c-.36.275-.801.425-1.255.427-.428 0-.845-.138-1.187-.395L0 2.6V14a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V2.5l-8.759 7.317Z"/>
                </svg>
              </div>
              <input
                type="email"
                name="Email"
                id="Email"
                placeholder="name@company.com"
                required=""
                bind:value={newEmployee.Email}
                class="w-full ps-10 px-4 py-2.5 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 outline-none {validationErrors.email ? 'border-red-500 bg-red-50' : ''}"
              />
            </div>
            {#if validationErrors.email}
              <p class="mt-1 text-sm text-red-600 flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
                {validationErrors.email}
              </p>
            {/if}
          </div>
          
          <div>
            <label
              for="Password"
              class="block mb-2 text-sm font-medium text-gray-700"
              >Password</label
            >
            <div class="relative">
              <div class="absolute inset-y-0 start-0 flex items-center ps-3.5 pointer-events-none">
                <svg class="w-4 h-4 text-gray-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                </svg>
              </div>
              <input
                type="password"
                name="Password"
                id="Password"
                placeholder="••••••••"
                required=""
                bind:value={newEmployee.Password}
                class="w-full ps-10 px-4 py-2.5 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 outline-none {validationErrors.password ? 'border-red-500 bg-red-50' : ''}"
              />
            </div>
            {#if validationErrors.password}
              <p class="mt-1 text-sm text-red-600 flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
                {validationErrors.password}
              </p>
            {/if}
          </div>
          
          <div>
            <label
              for="Type"
              class="block mb-2 text-sm font-medium text-gray-700"
              >Employee Type</label
            >
            <div class="relative">
              <div class="absolute inset-y-0 start-0 flex items-center ps-3.5 pointer-events-none">
                <svg class="w-4 h-4 text-gray-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                </svg>
              </div>
              <select
                id="Type"
                name="Type"
                bind:value={newEmployee.Type}
                class="w-full ps-10 px-4 py-2.5 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 outline-none bg-white {validationErrors.type ? 'border-red-500 bg-red-50' : ''}"
              >
                {#each options as option}
                  <option value={option}
                    >{option.replace(/([A-Z])/g, " $1").trim()}</option
                  >
                {/each}
              </select>
            </div>
            {#if validationErrors.type}
              <p class="mt-1 text-sm text-red-600 flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
                {validationErrors.type}
              </p>
            {/if}
          </div>
        </div>

        <div class="flex items-center justify-end pt-6 mt-4 border-t border-gray-200 gap-3">
          <button
            type="button"
            onclick={() => (isOpen = false)}
            class="text-gray-700 bg-white hover:bg-gray-100 focus:ring-2 focus:outline-none focus:ring-gray-200 rounded-lg border border-gray-300 text-sm font-medium px-5 py-2.5 flex items-center transition-all duration-200 transform hover:-translate-y-0.5"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
            Cancel
          </button>
          <button
            type="submit"
            class="bg-blue-600 hover:bg-blue-700 text-white font-medium py-2.5 px-6 rounded-lg shadow-md transition-all duration-200 flex items-center transform hover:-translate-y-0.5"
          >
            <svg
              class="me-1 -ms-1 w-5 h-5"
              fill="currentColor"
              viewBox="0 0 20 20"
              xmlns="http://www.w3.org/2000/svg"
              ><path
                fill-rule="evenodd"
                d="M10 5a1 1 0 011 1v3h3a1 1 0 110 2h-3v3a1 1 0 11-2 0v-3H6a1 1 0 110-2h3V6a1 1 0 011-1z"
                clip-rule="evenodd"
              ></path></svg
            >
            Add Employee
          </button>
        </div>
      </form>
    </div>
  </div>
</div>

<style>
  .animate-fadeIn {
    animation: fadeIn 0.3s ease-out;
  }
  
  @keyframes fadeIn {
    from {
      opacity: 0;
      transform: translateY(-20px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
</style>
