<script>
  import { onMount } from "svelte";
  import { api } from "$lib/api.js";
  import { page } from "$app/stores";
  import { goto } from "$app/navigation";

  let employee = $state({});
  let message = $state("");
  let error = $state("");
  let isEditing = $state(false);
  let employeeTypes = ["SalesEmployee", "SalesManager", "AccountManager"];
  let isLoading = $state(true);

  onMount(() => {
    load();
  });

  const load = async () => {
    try {
      isLoading = true;
      // Use consistent API method name - getOne instead of get
      employee = await api.getOne("employees", $page.params.slug);
      isLoading = false;
    } catch (e) {
      error = "Failed to load employee data";
      isLoading = false;
      console.log(e);
    }
  };

  const updateEmployee = async () => {
    try {
      // Create a copy of the employee object to clean up the data before sending
      const employeeToUpdate = { ...employee };

      // If password is empty, explicitly set to null or undefined to avoid sending empty string
      if (
        !employeeToUpdate.Password ||
        employeeToUpdate.Password.trim() === ""
      ) {
        delete employeeToUpdate.Password; // Remove empty password field completely
      }

      console.log("Sending update for employee:", employeeToUpdate);

      const slug = $page.params.slug;
      console.log(`Calling API update for employees/${slug}`);

      // Use update method with proper data formatting
      const updatedEmployee = await api.update("employees", slug, JSON.stringify(employeeToUpdate));
      
      // Refresh the employee data with the response
      employee = updatedEmployee;
      message = "Employee updated successfully!";
      error = "";
      isEditing = false;
    } catch (e) {
      error =
        "Failed to update employee: " +
        (e.errorData?.error || e.message || "Unknown error");
      message = "";
      console.error("Update failed:", e);
      // Log detailed error info
      console.error("Error details:", {
        status: e.status,
        response: e.response,
        errorData: e.errorData,
      });
    }
  };

  const deleteEmployee = async () => {
    if (confirm("Are you sure you want to delete this employee?")) {
      try {
        // Fix the delete API call - use just the employee ID without additional parameters
        await api.delete(`employees/${$page.params.slug}`, null);
        message = "Employee deleted successfully!";
        error = "";
        // Navigate back to employees list after deletion
        setTimeout(() => goto("/employees"), 1500);
      } catch (e) {
        error = "Failed to delete employee";
        message = "";
        console.error(e);
      }
    }
  };

  const toggleEdit = () => {
    isEditing = !isEditing;
  };
</script>

<div class="container mx-auto p-4">
  <nav class="flex mt-2 mb-2" aria-label="Breadcrumb">
    <ol
      class="inline-flex items-center space-x-1 md:space-x-2 rtl:space-x-reverse"
    >
      <li class="inline-flex items-center">
        <a
          href="/"
          class="inline-flex items-center text-sm font-medium text-gray-700 hover:text-blue-600"
        >
          <svg
            class="w-3 h-3 me-2.5"
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
            class="rtl:rotate-180 w-3 h-3 text-gray-400 mx-1"
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
            class="ms-1 text-sm font-medium text-gray-700 hover:text-blue-600 md:ms-2"
            >Employees</a
          >
        </div>
      </li>
      <li>
        <div class="flex items-center">
          <svg
            class="rtl:rotate-180 w-3 h-3 text-gray-400 mx-1"
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
          <span class="ms-1 text-sm font-medium text-gray-500 md:ms-2"
            >Employee Details</span
          >
        </div>
      </li>
    </ol>
  </nav>

  <div class="flex justify-between items-center mb-6">
    <h1 class="text-4xl font-bold">Employee Details</h1>
    <div>
      {#if !isEditing && !isLoading}
        <button
          on:click={toggleEdit}
          class="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded mr-2"
        >
          Edit
        </button>
      {:else if isEditing}
        <button
          on:click={toggleEdit}
          class="bg-gray-500 hover:bg-gray-700 text-white font-bold py-2 px-4 rounded mr-2"
        >
          Cancel
        </button>
      {/if}
      {#if !isLoading}
        <button
          on:click={deleteEmployee}
          class="bg-red-500 hover:bg-red-700 text-white font-bold py-2 px-4 rounded"
        >
          Delete
        </button>
      {/if}
    </div>
  </div>

  {#if message}
    <div
      class="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4"
    >
      {message}
    </div>
  {/if}

  {#if error}
    <div
      class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4"
    >
      {error}
    </div>
  {/if}

  {#if isLoading}
    <div class="flex justify-center items-center h-32">
      <svg
        class="animate-spin -ml-1 mr-3 h-8 w-8 text-blue-500"
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
      <p class="text-lg">Loading employee data...</p>
    </div>
  {:else if employee && Object.keys(employee).length > 0}
    <div class="bg-white shadow-md rounded px-8 pt-6 pb-8 mb-4">
      {#if isEditing}
        <!-- Edit form -->
        <form on:submit|preventDefault={updateEmployee}>
          <div class="mb-4">
            <label
              class="block text-gray-700 text-sm font-bold mb-2"
              for="firstname"
            >
              First Name
            </label>
            <input
              class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              id="firstname"
              type="text"
              bind:value={employee.Firstname}
            />
          </div>
          <div class="mb-4">
            <label
              class="block text-gray-700 text-sm font-bold mb-2"
              for="lastname"
            >
              Last Name
            </label>
            <input
              class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              id="lastname"
              type="text"
              bind:value={employee.Lastname}
            />
          </div>
          <div class="mb-4">
            <label
              class="block text-gray-700 text-sm font-bold mb-2"
              for="email"
            >
              Email
            </label>
            <input
              class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              id="email"
              type="email"
              bind:value={employee.Email}
            />
          </div>
          <div class="mb-4">
            <label
              class="block text-gray-700 text-sm font-bold mb-2"
              for="password"
            >
              Password
            </label>
            <input
              class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              id="password"
              type="password"
              placeholder="••••••••"
              bind:value={employee.Password}
            />
            <p class="text-xs text-gray-500 mt-1">
              Leave blank to keep current password
            </p>
          </div>
          <div class="mb-4">
            <label
              class="block text-gray-700 text-sm font-bold mb-2"
              for="type"
            >
              Employee Type
            </label>
            <select
              id="type"
              class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              bind:value={employee.Type}
            >
              {#each employeeTypes as type}
                <option value={type}
                  >{type.replace(/([A-Z])/g, " $1").trim()}</option
                >
              {/each}
            </select>
          </div>
          <div class="flex items-center justify-between">
            <button
              class="bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
              type="submit"
            >
              Save Changes
            </button>
          </div>
        </form>
      {:else}
        <!-- Display employee details -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <p class="text-gray-700 text-sm font-bold">ID:</p>
            <p class="text-gray-700">{employee.id || "N/A"}</p>
          </div>
          <div>
            <p class="text-gray-700 text-sm font-bold">Name:</p>
            <p class="text-gray-700">
              {employee.Firstname || "N/A"}
              {employee.Lastname || ""}
            </p>
          </div>
          <div>
            <p class="text-gray-700 text-sm font-bold">Email:</p>
            <p class="text-gray-700">{employee.Email || "N/A"}</p>
          </div>
          <div>
            <p class="text-gray-700 text-sm font-bold">Employee Type:</p>
            <p class="text-gray-700">
              {employee.Type
                ? employee.Type.replace(/([A-Z])/g, " $1").trim()
                : "N/A"}
            </p>
          </div>
        </div>
      {/if}
    </div>
  {:else}
    <div
      class="bg-yellow-100 border border-yellow-400 text-yellow-700 px-4 py-3 rounded mb-4"
    >
      <p class="font-bold">No employee found</p>
      <p>This employee may have been deleted or doesn't exist.</p>
    </div>
    <div class="mt-4">
      <a
        href="/employees"
        class="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
      >
        Back to Employees List
      </a>
    </div>
  {/if}
</div>
