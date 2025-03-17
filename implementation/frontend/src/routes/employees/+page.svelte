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
    console.log("Creating employee:", $state.snapshot(newEmployee));

    try {
      // Client-side validation
      if (newEmployee.Firstname.length < 2) {
        errorMessage = "First name must be at least 2 characters long";
        return;
      } else if (newEmployee.Lastname.length < 2) {
        errorMessage = "Last name must be at least 2 characters long";
        return;
      } else if (
        !newEmployee.Email.includes("@") ||
        !newEmployee.Email.includes(".")
      ) {
        errorMessage = "Please enter a valid email address";
        return;
      } else if (newEmployee.Password.length < 8) {
        errorMessage = "Password must be at least 8 characters long";
        return;
      }

      // Make the API call
      const response = await api.create(
        "/employees",
        JSON.stringify($state.snapshot(newEmployee))
      );

      console.log("API response:", response);

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
      console.error("Error creating employee:", error);

      // Try to extract error message from response
      try {
        const errorResponse = await error.response?.json();
        if (errorResponse && errorResponse.error) {
          errorMessage = `Error: ${errorResponse.error}`;
        } else {
          errorMessage =
            "Failed to create employee. Please check your inputs and try again.";
        }
      } catch (parseError) {
        errorMessage = `Failed to create employee: ${error.message || "Unknown error"}`;
      }
    }
  };
</script>

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
  </ol>
</nav>

<!-- Modal toggle -->
<div class="flex flex-row-reverse m-2">
  <button
    onclick={() => (isOpen = !isOpen)}
    class="block text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800"
    type="button"
  >
    Add employee
  </button>
</div>

<div class="relative overflow-x-auto shadow-md sm:rounded-lg">
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
      {#each employees as employee}
        <tr class="odd:bg-white even:bg-gray-50 border-gray-200 font-medium">
          <th class="px-6 py-4">{employee.Lastname}, {employee.Firstname}</th>
          <td class="px-6 py-4">{employee.Email}</td>
          <td class="px-6 py-4">{employee.Type}</td>
          <td class="px-6 py-4">
            <a
              href="/employees/{employee.id}"
              class="font-medium text-blue-600 dark:text-blue-500 hover:underline"
              >Edit</a
            >
          </td>
        </tr>
      {/each}
    </tbody>
  </table>
</div>

<div
  onclick={() => (isOpen = !isOpen)}
  id="crud-modal"
  tabindex="-1"
  aria-hidden="true"
  class="{isOpen
    ? ''
    : 'hidden'} backdrop-blur-xs flex overflow-y-auto overflow-x-hidden fixed top-0 right-0 left-0 z-50 justify-center items-center w-full md:inset-0 h-[calc(100%-1rem)] max-h-full"
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
          class="text-gray-400 bg-transparent hover:bg-gray-200 hover:text-gray-900 rounded-lg text-sm w-8 h-8 ms-auto inline-flex justify-center items-center dark:hover:bg-gray-600 dark:hover:text-white"
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
              class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-600 dark:border-gray-500 dark:placeholder-gray-400 dark:text-white dark:focus:ring-primary-500 dark:focus:border-primary-500"
            />
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
              class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-600 dark:border-gray-500 dark:placeholder-gray-400 dark:text-white dark:focus:ring-primary-500 dark:focus:border-primary-500"
            />
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
              class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-600 dark:border-gray-500 dark:placeholder-gray-400 dark:text-white dark:focus:ring-primary-500 dark:focus:border-primary-500"
            />
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
              class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-600 dark:border-gray-500 dark:placeholder-gray-400 dark:text-white dark:focus:ring-primary-500 dark:focus:border-primary-500"
            />
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
              class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-primary-500 focus:border-primary-500 block w-full p-2.5 dark:bg-gray-600 dark:border-gray-500 dark:placeholder-gray-400 dark:text-white dark:focus:ring-primary-500 dark:focus:border-primary-500"
            >
              {#each options as option}
                <option value={option}>{option}</option>
              {/each}
            </select>
          </div>
        </div>
        {#if errorMessage}
          <div
            class="col-span-2 p-4 mb-4 text-sm text-red-800 rounded-lg bg-red-50"
            role="alert"
          >
            {errorMessage}
          </div>
        {/if}
        <button
          type="submit"
          class="text-white inline-flex items-center bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800"
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
