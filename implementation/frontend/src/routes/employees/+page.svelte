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

  // Add field-specific validation errors
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
    employees = await api.all("/employees");
  };

  const handleKeyEvent = (e) => {
    if (e.key === "Escape" && isOpen) {
      isOpen = false;
    }
  };

  const createEmployee = async (e) => {
    e.preventDefault();
    errorMessage = ""; // Clear previous errors

    // Clear all validation errors
    Object.keys(validationErrors).forEach((key) => {
      validationErrors[key] = "";
    });

    try {
      // Make the API call
      const response = await api.create(
        "/employees",
        JSON.stringify($state.snapshot(newEmployee))
      );

      // Reset the form
      newEmployee.Firstname = "";
      newEmployee.Lastname = "";
      newEmployee.Email = "";
      newEmployee.Password = "";
      newEmployee.Type = "SalesEmployee";

      // Reload the employee list
      await load();
      isOpen = false;
    } catch (error) {
      // Check if we have validation errors
      if (error.errorData && error.errorData.validationErrors) {
        const errors = error.errorData.validationErrors;

        // Map API errors to form fields, handling different case formats
        Object.keys(errors).forEach((key) => {
          // Convert API field names to lowercase for matching
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
        // Try to extract field-specific errors from the general error message
        const errorMsg = error.errorData.error;

        // Handle specific error messages based on content
        if (errorMsg.includes("name")) {
          if (errorMsg.toLowerCase().includes("first")) {
            validationErrors.firstname = errorMsg;
          } else if (errorMsg.toLowerCase().includes("last")) {
            validationErrors.lastname = errorMsg;
          } else {
            // General name error
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
          // Fallback to general error message
          errorMessage = `Error: ${errorMsg}`;
        }
      } else {
        // Fallback error message
        errorMessage = "Failed to create employee. Please try again.";
      }
    }
  };
</script>

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
        <span class="ms-1 text-sm font-medium text-gray-400">Employees</span>
      </div>
    </li>
  </ol>
</nav>

<div class="max-w-6xl mx-auto p-6">
  <div class="flex justify-between items-center mb-8">
    <h1 class="text-3xl font-bold text-gray-800">Employees</h1>
    <div>
      <button
        onclick={() => (isOpen = !isOpen)}
        class="bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-5 rounded-lg shadow-sm transition-all duration-200 transform hover:-translate-y-0.5"
        type="button"
      >
        Add employee
      </button>
    </div>
  </div>

  {#if errorMessage}
    <div
      class="bg-red-50 border-l-4 border-red-500 text-red-700 p-4 rounded-md mb-6 shadow-sm"
      role="alert"
    >
      {errorMessage}
    </div>
  {/if}

  <div
    class="bg-white rounded-xl shadow-lg border border-gray-200 overflow-hidden"
  >
    <div class="relative overflow-x-auto">
      <table class="w-full text-sm text-left rtl:text-right text-gray-900">
        <thead class="text-xs text-gray-700 uppercase bg-gray-50">
          <tr>
            <th scope="col" class="px-6 py-3">Name</th>
            <th scope="col" class="px-6 py-3">Email</th>
            <th scope="col" class="px-6 py-3">Type</th>
            <th scope="col" class="px-6 py-3">Action</th>
          </tr>
        </thead>
        <tbody>
        {#if employees.length > 0}
          {#each employees as employee}
            <tr
                    class="odd:bg-white even:bg-gray-50 border-b border-gray-200 hover:bg-gray-100 transition-colors"
            >
              <th class="px-6 py-4 font-medium"
              >{employee.Firstname} {employee.Lastname}</th
              >
              <td class="px-6 py-4">{employee.Email}</td>
              <td class="px-6 py-4"
              >{employee.Type.replace(/([A-Z])/g, " $1").trim()}</td
              >
              <td class="px-6 py-4">
                <a
                        href="/employees/{employee.id}"
                        class="bg-white text-green-600 border border-green-600 hover:bg-green-600 hover:text-white font-medium py-2 px-5 rounded-lg shadow-sm transition-all duration-200 transform hover:-translate-y-0.5 inline-block"
                >Manage</a
                >
              </td>
            </tr>
          {/each}
        {:else}
            <tr>
                <td colspan="4" class="p-4 text-center text-gray-500">
                No employees found.
                </td>
            </tr>
        {/if}
        </tbody>
      </table>
    </div>
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
    <!-- Modal content -->
    <div class="relative bg-white rounded-lg shadow-sm dark:bg-gray-700">
      <!-- Modal header -->
      <div
        class="flex items-center justify-between p-4 md:p-5 border-b rounded-t dark:border-gray-600 border-gray-200"
      >
        <h3 class="text-lg font-semibold text-gray-900 dark:text-white">
          Create New Employee
        </h3>
        <button
          onclick={() => (isOpen = false)}
          type="button"
          class="text-gray-400 bg-transparent hover:bg-gray-200 hover:text-gray-900 rounded-lg text-sm w-8 h-8 ms-auto inline-flex justify-center items-center transition-all duration-200"
          data-modal-toggle="crud-modal"
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
      <!-- Modal body -->
      <form class="p-4 md:p-5" onsubmit={createEmployee}>
        <div class="grid gap-4 mb-4 grid-cols-2">
          <div class="col-span-2">
            <label
              for="Firstname"
              class="block mb-2 text-sm font-medium text-gray-900 dark:text-white"
              >First name</label
            >
            <input
              type="text"
              name="Firstname"
              id="Firstname"
              placeholder="First name"
              required=""
              bind:value={newEmployee.Firstname}
              class="bg-gray-50 border {validationErrors.firstname
                ? 'border-red-500'
                : 'border-gray-300'} text-gray-900 text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-600 dark:border-gray-500 dark:placeholder-gray-400 dark:text-white dark:focus:ring-primary-500 dark:focus:border-primary-500"
            />
            {#if validationErrors.firstname}
              <p class="mt-1 text-sm text-red-600">
                {validationErrors.firstname}
              </p>
            {/if}
          </div>
          <div class="col-span-2">
            <label
              for="Lastname"
              class="block mb-2 text-sm font-medium text-gray-900 dark:text-white"
              >Last name</label
            >
            <input
              type="text"
              name="Lastname"
              id="Lastname"
              placeholder="Last name"
              required=""
              bind:value={newEmployee.Lastname}
              class="bg-gray-50 border {validationErrors.lastname
                ? 'border-red-500'
                : 'border-gray-300'} text-gray-900 text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-600 dark:border-gray-500 dark:placeholder-gray-400 dark:text-white dark:focus:ring-primary-500 dark:focus:border-primary-500"
            />
            {#if validationErrors.lastname}
              <p class="mt-1 text-sm text-red-600">
                {validationErrors.lastname}
              </p>
            {/if}
          </div>
          <div class="col-span-2">
            <label
              for="Email"
              class="block mb-2 text-sm font-medium text-gray-900 dark:text-white"
              >Email</label
            >
            <input
              type="email"
              name="Email"
              id="Email"
              placeholder="name@company.com"
              required=""
              bind:value={newEmployee.Email}
              class="bg-gray-50 border {validationErrors.email
                ? 'border-red-500'
                : 'border-gray-300'} text-gray-900 text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-600 dark:border-gray-500 dark:placeholder-gray-400 dark:text-white dark:focus:ring-primary-500 dark:focus:border-primary-500"
            />
            {#if validationErrors.email}
              <p class="mt-1 text-sm text-red-600">{validationErrors.email}</p>
            {/if}
          </div>
          <div class="col-span-2">
            <label
              for="Password"
              class="block mb-2 text-sm font-medium text-gray-900 dark:text-white"
              >Password</label
            >
            <input
              type="password"
              name="Password"
              id="Password"
              placeholder="••••••••"
              required=""
              bind:value={newEmployee.Password}
              class="bg-gray-50 border {validationErrors.password
                ? 'border-red-500'
                : 'border-gray-300'} text-gray-900 text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-600 dark:border-gray-500 dark:placeholder-gray-400 dark:text-white dark:focus:ring-primary-500 dark:focus:border-primary-500"
            />
            {#if validationErrors.password}
              <p class="mt-1 text-sm text-red-600">
                {validationErrors.password}
              </p>
            {/if}
          </div>
          <div class="col-span-2">
            <label
              for="Type"
              class="block mb-2 text-sm font-medium text-gray-900 dark:text-white"
              >Employee type</label
            >
            <select
              id="Type"
              name="Type"
              bind:value={newEmployee.Type}
              class="bg-gray-50 border {validationErrors.type
                ? 'border-red-500'
                : 'border-gray-300'} text-gray-900 text-sm rounded-lg focus:ring-primary-500 focus:border-primary-500 block w-full p-2.5 dark:bg-gray-600 dark:border-gray-500 dark:placeholder-gray-400 dark:text-white dark:focus:ring-primary-500 dark:focus:border-primary-500"
            >
              {#each options as option}
                <option value={option}
                  >{option.replace(/([A-Z])/g, " $1").trim()}</option
                >
              {/each}
            </select>
            {#if validationErrors.type}
              <p class="mt-1 text-sm text-red-600">{validationErrors.type}</p>
            {/if}
          </div>
        </div>
        {#if errorMessage}
          <div
            class="p-4 mb-4 text-sm text-red-800 rounded-lg bg-red-50"
            role="alert"
          >
            {errorMessage}
          </div>
        {/if}
        <button
          type="submit"
          class="bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-5 rounded-lg shadow-sm transition-all duration-200 transform hover:-translate-y-0.5 inline-flex items-center"
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
          Add new employee
        </button>
      </form>
    </div>
  </div>
</div>
