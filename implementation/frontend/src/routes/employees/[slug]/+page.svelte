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
  let showDeleteModal = $state(false); // Add state for delete modal

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
      const updatedEmployee = await api.update(
        "employees",
        slug,
        JSON.stringify(employeeToUpdate)
      );

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
    try {
      // Fix the delete API call - use just the employee ID without additional parameters
      await api.delete(`employees/${$page.params.slug}`, null);
      message = "Employee deleted successfully!";
      error = "";
      showDeleteModal = false; // Close the modal
      // Clear employee data immediately 
      employee = {};
      // Navigate back to employees list after deletion
      setTimeout(() => goto("/employees"), 1500);
    } catch (e) {
      error = "Failed to delete employee";
      message = "";
      console.error(e);
      showDeleteModal = false; // Close the modal on error
    }
  };

  const toggleEdit = () => {
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
    <div class="flex justify-between items-center mb-8">
      <h1 class="text-3xl font-bold text-gray-800">Employee Details</h1>
      <div class="flex space-x-3">
        {#if !isEditing && !isLoading}
          <button
            on:click={toggleEdit}
            class="bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-5 rounded-lg shadow-sm transition-all duration-200 transform hover:-translate-y-0.5"
          >
            Edit
          </button>
        {:else if isEditing}
          <button
            on:click={toggleEdit}
            class="bg-gray-500 hover:bg-gray-600 text-white font-medium py-2 px-5 rounded-lg shadow-sm transition-all duration-200 transform hover:-translate-y-0.5"
          >
            Cancel
          </button>
        {/if}
        {#if !isLoading}
          <button
            on:click={toggleDeleteModal}
            class="bg-red-500 hover:bg-red-600 text-white font-medium py-2 px-5 rounded-lg shadow-sm transition-all duration-200 transform hover:-translate-y-0.5"
          >
            Delete
          </button>
        {/if}
      </div>
    </div>

    {#if message}
      <div
        class="bg-green-50 border-l-4 border-green-500 text-green-700 p-4 rounded-md mb-6 shadow-sm"
        transition:slide
      >
        {message}
      </div>
    {/if}

    {#if error}
      <div
        class="bg-red-50 border-l-4 border-red-500 text-red-700 p-4 rounded-md mb-6 shadow-sm"
        transition:slide
      >
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
          <!-- Edit form -->
          <div class="p-8">
            <h2 class="text-2xl font-semibold text-gray-800 mb-6">
              Edit Employee
            </h2>
            <form on:submit|preventDefault={updateEmployee} class="space-y-6">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label
                    class="block text-sm font-medium text-gray-700 mb-2"
                    for="firstname"
                  >
                    First Name
                  </label>
                  <input
                    class="w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 outline-none"
                    id="firstname"
                    type="text"
                    bind:value={employee.Firstname}
                  />
                </div>
                <div>
                  <label
                    class="block text-sm font-medium text-gray-700 mb-2"
                    for="lastname"
                  >
                    Last Name
                  </label>
                  <input
                    class="w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 outline-none"
                    id="lastname"
                    type="text"
                    bind:value={employee.Lastname}
                  />
                </div>
              </div>

              <div>
                <label
                  class="block text-sm font-medium text-gray-700 mb-2"
                  for="email"
                >
                  Email
                </label>
                <input
                  class="w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 outline-none"
                  id="email"
                  type="email"
                  bind:value={employee.Email}
                />
              </div>

              <div>
                <label
                  class="block text-sm font-medium text-gray-700 mb-2"
                  for="password"
                >
                  Password
                </label>
                <input
                  class="w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 outline-none"
                  id="password"
                  type="password"
                  placeholder="••••••••"
                  bind:value={employee.Password}
                />
                <p class="text-xs text-gray-500 mt-2 italic">
                  Leave blank to keep current password
                </p>
              </div>

              <div>
                <label
                  class="block text-sm font-medium text-gray-700 mb-2"
                  for="type"
                >
                  Employee Type
                </label>
                <select
                  id="type"
                  class="w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 outline-none bg-white"
                  bind:value={employee.Type}
                >
                  {#each employeeTypes as type}
                    <option value={type}
                      >{type.replace(/([A-Z])/g, " $1").trim()}</option
                    >
                  {/each}
                </select>
              </div>

              <div class="pt-4">
                <button
                  class="w-full md:w-auto bg-blue-600 hover:bg-blue-700 text-white font-medium py-3 px-6 rounded-lg shadow-md transition-all duration-200 transform hover:-translate-y-0.5 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-opacity-50"
                  type="submit"
                >
                  Save Changes
                </button>
              </div>
            </form>
          </div>
        {:else}
          <!-- Display employee details -->
          <div class="p-8">
            <h2 class="text-2xl font-semibold text-gray-800 mb-6">
              Employee Information
            </h2>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-y-6 gap-x-12">
              <div>
                <p class="text-sm font-medium text-gray-500 mb-1">ID</p>
                <p class="text-gray-800 text-lg">{employee.id || "N/A"}</p>
              </div>
              <div>
                <p class="text-sm font-medium text-gray-500 mb-1">Full Name</p>
                <p class="text-gray-800 text-lg">
                  {employee.Firstname || "N/A"}
                  {employee.Lastname || ""}
                </p>
              </div>
              <div>
                <p class="text-sm font-medium text-gray-500 mb-1">Email</p>
                <p class="text-gray-800 text-lg">{employee.Email || "N/A"}</p>
              </div>
              <div>
                <p class="text-sm font-medium text-gray-500 mb-1">
                  Employee Type
                </p>
                <p class="text-gray-800 text-lg">
                  {employee.Type
                    ? employee.Type.replace(/([A-Z])/g, " $1").trim()
                    : "N/A"}
                </p>
              </div>
            </div>
          </div>
        {/if}
      </div>
    {:else}
      <div
        class="bg-yellow-50 border-l-4 border-yellow-400 text-yellow-800 p-6 rounded-md mb-6 shadow-sm"
      >
        <p class="font-bold text-lg mb-2">No employee found</p>
        <p>This employee may have been deleted or doesn't exist.</p>
      </div>
      <div class="mt-6">
        <a
          href="/employees"
          class="inline-flex items-center bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-5 rounded-lg shadow-sm transition-all duration-200 transform hover:-translate-y-0.5"
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
    {/if}
  </div>

  <!-- Delete Confirmation Modal -->
  {#if showDeleteModal}
    <div
      class="fixed inset-0 z-50 flex items-center justify-center backdrop-blur-sm"
    >
      <div class="bg-white p-6 rounded-lg shadow-lg max-w-md w-full">
        <h3 class="text-xl font-semibold text-gray-900 mb-4">
          Confirm Deletion
        </h3>
        <p class="text-gray-700 mb-6">
          Are you sure you want to delete {employee.Firstname}
          {employee.Lastname}? This action cannot be undone.
        </p>
        <div class="flex justify-end space-x-3">
            <button
            on:click={toggleDeleteModal}
            class="bg-gray-500 hover:bg-gray-600 text-white font-medium py-2 px-5 rounded-lg shadow-sm transition-all duration-200 transform hover:-translate-y-0.5"
          >
            Cancel
          </button>
          <button
            on:click={deleteEmployee}
            class="bg-red-500 hover:bg-red-600 text-white font-medium py-2 px-5 rounded-lg shadow-sm transition-all duration-200 transform hover:-translate-y-0.5"
          >
            Delete
          </button>
        </div>
      </div>
    </div>
  {/if}
</div>
