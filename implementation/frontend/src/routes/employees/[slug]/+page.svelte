<script>
  import { onMount } from "svelte";
  import { api } from "$lib/api.js";
  import { page } from "$app/stores";
  import { goto } from "$app/navigation";

  let employee = $state({});
  let editForm = $state({});
  let message = $state("");
  let error = $state("");
  let isEditing = $state(false);
  let employeeTypes = ["SalesEmployee", "SalesManager", "AccountManager"];
  let isLoading = $state(true);
  let showDeleteModal = $state(false);

  onMount(() => {
    load();
  });

  const load = async () => {
    try {
      isLoading = true;
      employee = await api.getOne("employees", $page.params.slug);
      isLoading = false;
    } catch (e) {
      isLoading = false;
      console.log(e);
    }
  };

  const updateEmployee = async () => {
    try {
      // Create a new object with only the necessary fields
      const employeeToUpdate = {
        id: editForm.id, // Ensure ID is included
        Firstname: editForm.Firstname,
        Lastname: editForm.Lastname,
        Email: editForm.Email,
        Type: editForm.Type
      };

      // Only add password if it's not empty
      if (editForm.Password && editForm.Password.trim() !== "") {
        employeeToUpdate.Password = editForm.Password;
      }
      
      // Log the data we're sending (for debugging)
      console.log("Updating employee data:", JSON.stringify(employeeToUpdate));

      const slug = $page.params.slug;

      // Make the API call with error handling
      try {
        const updatedEmployee = await api.update(
          "employees",
          slug,
          JSON.stringify(employeeToUpdate)
        );

        // Update the local employee data and show success message
        employee = updatedEmployee;
        message = "Employee updated successfully!";
        error = "";
        isEditing = false;
      } catch (apiError) {
        // Detailed API error logging
        console.error("API Error:", apiError);
        
        if (apiError.status) {
          console.error(`Status Code: ${apiError.status}`);
        }
        
        if (apiError.errorData) {
          console.error("Error Data:", apiError.errorData);
        }
        
        // Create a more helpful error message
        error = `Failed to update employee: ${
          apiError.errorData?.error || 
          apiError.message || 
          "Unexpected error occurred. Please try again."
        }`;
        
        message = "";
      }
    } catch (e) {
      // Catch any other exceptions
      console.error("Unexpected error during update:", e);
      error = "An unexpected error occurred while preparing data for update";
      message = "";
    }
  };

  const deleteEmployee = async () => {
    try {
      await api.delete(`employees/${$page.params.slug}`, null);
      message = "Employee deleted successfully!";
      error = "";
      showDeleteModal = false;
      employee = {};
      setTimeout(() => goto("/employees"), 1500);
    } catch (e) {
      error = "Failed to delete employee";
      message = "";
      console.error(e);
      showDeleteModal = false;
    }
  };

  const toggleEdit = () => {
    if (!isEditing) {
      editForm = { ...employee };
      editForm.Password = "";
    }
    isEditing = !isEditing;
  };

  const toggleDeleteModal = () => {
    showDeleteModal = !showDeleteModal;
  };
</script>

<div class="">
  <nav class="flex my-4" aria-label="Breadcrumb">
    <ol class="inline-flex items-center space-x-2">
      <li class="inline-flex items-center">
        <a
          href="/"
          class="inline-flex items-center text-sm font-medium text-gray-600 hover:text-blue-500 transition-colors"
        >
          <svg
            class="w-4 h-4 me-2"
            aria-hidden="true"
            xmlns="http://www.w3.org/2000/svg"
            fill="currentColor"
            viewBox="0 0 20 20"
          >
            <path
              d="m19.707 9.293-2-2-7-7a1 1 0 0 0-1.414 0l-7 7-2 2a1 1 0 0 0 1.414 1.414L2 10.414V18a2 2 0 0 0 2 2h3a1 1 0 0 0 1-1v-4a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1v4a1 1 0 0 0 1 1h3a2 2 0 0 0 2-2v-7.586l.293.293a1 1 0 0 0 1.414-1.414Z"
            />
          </svg>
          Home
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
          <a
            href="/employees"
            class="ms-1 text-sm font-medium text-gray-600 hover:text-blue-500 transition-colors"
            >Employees</a
          >
        </div>
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
          <span class="ms-1 text-sm font-medium text-gray-400"
            >Employee Details</span
          >
        </div>
      </li>
    </ol>
  </nav>

  <div class="max-w-6xl mx-auto p-6">
    <div class="mb-8 bg-gradient-to-r from-blue-50 to-indigo-100 rounded-lg shadow-sm border border-blue-200 overflow-hidden">
      <div class="p-6">
        <div class="flex flex-col md:flex-row justify-between md:items-center gap-4">
          <div>
            <h1 class="text-3xl font-bold text-blue-900 mb-2">Employee Details</h1>
            <p class="text-blue-700">View and manage employee information</p>
          </div>
          <div class="flex space-x-3">
            {#if !isEditing && !isLoading}
              <button
                onclick={toggleEdit}
                class="bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-5 rounded-lg shadow-sm transition-all duration-200 transform hover:-translate-y-0.5 flex items-center"
              >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                </svg>
                Edit
              </button>
            {:else if isEditing}
              <button
                onclick={toggleEdit}
                class="bg-gray-500 hover:bg-gray-600 text-white font-medium py-2 px-5 rounded-lg shadow-sm transition-all duration-200 transform hover:-translate-y-0.5 flex items-center"
              >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
                Cancel
              </button>
            {/if}
            {#if !isLoading}
              <button
                onclick={toggleDeleteModal}
                class="bg-red-500 hover:bg-red-600 text-white font-medium py-2 px-5 rounded-lg shadow-sm transition-all duration-200 transform hover:-translate-y-0.5 flex items-center"
              >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                </svg>
                Delete
              </button>
            {/if}
          </div>
        </div>
      </div>
    </div>

    {#if message}
      <div
        class="bg-green-50 border-l-4 border-green-500 text-green-700 p-4 rounded-md mb-6 shadow-sm flex items-center"
        transition:slide
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-3 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
        </svg>
        {message}
      </div>
    {/if}

    {#if error}
      <div
        class="bg-red-50 border-l-4 border-red-500 text-red-700 p-4 rounded-md mb-6 shadow-sm flex items-center"
        transition:slide
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-3 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
        </svg>
        {error}
      </div>
    {/if}

    {#if isLoading}
      <div
        class="flex justify-center items-center h-64 bg-white rounded-lg shadow-sm"
      >
        <svg
          class="animate-spin h-10 w-10 text-blue-500"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
        >
          <circle
            class="opacity-25"
            cx="12"
            cy="12"
            r="10"
            stroke="currentColor"
            stroke-width="4"
          ></circle>
          <path
            class="opacity-75"
            fill="currentColor"
            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
          ></path>
        </svg>
        <p class="text-lg ml-3 text-gray-600">Loading employee data...</p>
      </div>
    {:else if employee && Object.keys(employee).length > 0}
      <div
        class="bg-white rounded-xl shadow-lg border border-gray-200 overflow-hidden transition-all duration-300"
      >
        {#if isEditing}
          <div class="p-8">
            <h2 class="text-2xl font-semibold text-gray-800 mb-6 flex items-center">
              <div class="bg-blue-100 p-2 rounded-full mr-3">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                </svg>
              </div>
              Edit Employee
            </h2>
            <form onsubmit={(e) => { e.preventDefault(); updateEmployee(); }} class="space-y-6">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label
                    class="block text-sm font-medium text-gray-700 mb-2"
                    for="firstname"
                  >
                    First Name
                  </label>
                  <div class="relative">
                    <div class="absolute inset-y-0 start-0 flex items-center ps-3.5 pointer-events-none">
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                      </svg>
                    </div>
                    <input
                      class="w-full ps-10 px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 outline-none"
                      id="firstname"
                      type="text"
                      bind:value={editForm.Firstname}
                    />
                  </div>
                </div>
                <div>
                  <label
                    class="block text-sm font-medium text-gray-700 mb-2"
                    for="lastname"
                  >
                    Last Name
                  </label>
                  <div class="relative">
                    <div class="absolute inset-y-0 start-0 flex items-center ps-3.5 pointer-events-none">
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                      </svg>
                    </div>
                    <input
                      class="w-full ps-10 px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 outline-none"
                      id="lastname"
                      type="text"
                      bind:value={editForm.Lastname}
                    />
                  </div>
                </div>
              </div>

              <div>
                <label
                  class="block text-sm font-medium text-gray-700 mb-2"
                  for="email"
                >
                  Email
                </label>
                <div class="relative">
                  <div class="absolute inset-y-0 start-0 flex items-center ps-3.5 pointer-events-none">
                    <svg class="w-4 h-4 text-gray-500" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="currentColor" viewBox="0 0 20 16">
                      <path d="m10.036 8.278 9.258-7.79A1.979 1.979 0 0 0 18 0H2A1.987 1.987 0 0 0 .641.541l9.395 7.737Z"/>
                      <path d="M11.241 9.817c-.36.275-.801.425-1.255.427-.428 0-.845-.138-1.187-.395L0 2.6V14a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V2.5l-8.759 7.317Z"/>
                    </svg>
                  </div>
                  <input
                    class="w-full ps-10 px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 outline-none"
                    id="email"
                    type="email"
                    bind:value={editForm.Email}
                  />
                </div>
              </div>

              <div>
                <label
                  class="block text-sm font-medium text-gray-700 mb-2"
                  for="password"
                >
                  Password <span class="text-xs text-gray-500 font-normal">*Required</span>
                </label>
                <div class="relative">
                  <div class="absolute inset-y-0 start-0 flex items-center ps-3.5 pointer-events-none">
                    <svg class="w-4 h-4 text-gray-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                    </svg>
                  </div>
                  <input
                    class="w-full ps-10 px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 outline-none"
                    id="password"
                    type="password"
                    placeholder="Enter new password"
                    bind:value={editForm.Password}
                  />
                </div>
              </div>

              <div>
                <label
                  class="block text-sm font-medium text-gray-700 mb-2"
                  for="type"
                >
                  Employee Type
                </label>
                <div class="relative">
                  <div class="absolute inset-y-0 start-0 flex items-center ps-3.5 pointer-events-none">
                    <svg class="w-4 h-4 text-gray-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                    </svg>
                  </div>
                  <select
                    id="type"
                    class="w-full ps-10 px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 outline-none bg-white"
                    bind:value={editForm.Type}
                  >
                    {#each employeeTypes as type}
                      <option value={type}
                        >{type.replace(/([A-Z])/g, " $1").trim()}</option
                      >
                    {/each}
                  </select>
                </div>
              </div>

              <div class="pt-6 border-t border-gray-200">
                <button
                  class="bg-blue-600 hover:bg-blue-700 text-white font-medium py-3 px-6 rounded-lg shadow-md transition-all duration-200 transform hover:-translate-y-0.5 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-opacity-50 flex items-center"
                  type="submit"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                  </svg>
                  Save Changes
                </button>
              </div>
            </form>
          </div>
        {:else}
          <div class="p-8">
            <h2 class="text-2xl font-semibold text-gray-800 mb-6 flex items-center">
              <div class="bg-blue-100 p-2 rounded-full mr-3">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
              </div>
              Employee Information
            </h2>
            
            <div class="bg-blue-50 p-6 rounded-lg mb-6 border border-blue-100">
              <div class="flex items-center">
                <div class="relative flex-shrink-0 inline-flex items-center justify-center w-20 h-20 overflow-hidden bg-gradient-to-br from-blue-700 to-blue-900 rounded-full text-white shadow-md mr-6">
                  <span class="font-bold text-2xl">
                    {(employee.Firstname.charAt(0) || '').toUpperCase() + (employee.Lastname.charAt(0) || '').toUpperCase()}
                  </span>
                </div>
                
                <div>
                  <h3 class="text-2xl font-bold text-gray-800 mb-2">{employee.Firstname} {employee.Lastname}</h3>
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
              </div>
            </div>
            
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div class="bg-gray-50 p-4 rounded-lg border border-gray-100">
                <div class="flex items-start">
                  <div class="bg-blue-100 p-2 rounded-full mr-3">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V8a2 2 0 00-2-2h-5m-4 0V5a2 2 0 114 0v1m-4 0a2 2 0 104 0m-5 8a2 2 0 100-4 2 2 0 000 4zm0 0c1.306 0 2.417.835 2.83 2M9 14a3.001 3.001 0 00-2.83 2M15 11h3m-3 4h2" />
                    </svg>
                  </div>
                  <div>
                    <p class="text-sm font-medium text-gray-500 mb-1">ID</p>
                    <p class="text-gray-800 text-lg font-medium">{employee.id || "N/A"}</p>
                  </div>
                </div>
              </div>
              
              <div class="bg-gray-50 p-4 rounded-lg border border-gray-100">
                <div class="flex items-start">
                  <div class="bg-green-100 p-2 rounded-full mr-3">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                    </svg>
                  </div>
                  <div>
                    <p class="text-sm font-medium text-gray-500 mb-1">Full Name</p>
                    <p class="text-gray-800 text-lg font-medium">
                      {employee.Firstname || "N/A"}
                      {employee.Lastname || ""}
                    </p>
                  </div>
                </div>
              </div>
              
              <div class="bg-gray-50 p-4 rounded-lg border border-gray-100">
                <div class="flex items-start">
                  <div class="bg-purple-100 p-2 rounded-full mr-3">
                    <svg class="w-5 h-5 text-purple-600" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                    </svg>
                  </div>
                  <div>
                    <p class="text-sm font-medium text-gray-500 mb-1">Email</p>
                    <p class="text-gray-800 text-lg font-medium">{employee.Email || "N/A"}</p>
                  </div>
                </div>
              </div>
              
              <div class="bg-gray-50 p-4 rounded-lg border border-gray-100">
                <div class="flex items-start">
                  <div class="bg-yellow-100 p-2 rounded-full mr-3">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-yellow-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                    </svg>
                  </div>
                  <div>
                    <p class="text-sm font-medium text-gray-500 mb-1">
                      Employee Type
                    </p>
                    <p class="text-gray-800 text-lg font-medium">
                      {employee.Type
                        ? employee.Type.replace(/([A-Z])/g, " $1").trim()
                        : "N/A"}
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        {/if}
      </div>
    {:else}
      <div
        class="bg-yellow-50 border-l-4 border-yellow-400 text-yellow-800 p-6 rounded-md mb-6 shadow-sm"
      >
        <div class="flex items-center">
          <svg
            class="h-6 w-6 text-yellow-500 mr-3"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
            />
          </svg>
          <div>
            <p class="font-bold text-lg mb-2">No employee found</p>
            <p>This employee may have been deleted or doesn't exist.</p>
          </div>
        </div>
      </div>
    {/if}
    <div class="mt-6">
      <a
              href="/employees"
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
        Back to Employees List
      </a>
    </div>
  </div>

  {#if showDeleteModal}
    <div
      class="fixed inset-0 z-50 flex items-center justify-center backdrop-blur-sm bg-gray-900/50"
    >
      <div class="bg-white p-6 rounded-lg shadow-xl max-w-md w-full animate-fadeIn">
        <div class="flex items-center mb-4">
          <div class="bg-red-100 p-2 rounded-full mr-3">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
            </svg>
          </div>
          <h3 class="text-xl font-semibold text-gray-900">
            Confirm Deletion
          </h3>
        </div>
        
        <p class="text-gray-700 mb-6">
          Are you sure you want to delete {employee.Firstname}
          {employee.Lastname}? This action cannot be undone.
        </p>
        <div class="flex justify-end space-x-3">
          <button
            onclick={toggleDeleteModal}
            class="bg-gray-500 hover:bg-gray-600 text-white font-medium py-2 px-5 rounded-lg shadow-sm transition-all duration-200 transform hover:-translate-y-0.5 flex items-center"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
            Cancel
          </button>
          <button
            onclick={deleteEmployee}
            class="bg-red-500 hover:bg-red-600 text-white font-medium py-2 px-5 rounded-lg shadow-sm transition-all duration-200 transform hover:-translate-y-0.5 flex items-center"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
            </svg>
            Delete
          </button>
        </div>
      </div>
    </div>
  {/if}
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
