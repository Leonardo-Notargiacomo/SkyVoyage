<script>
  import { goto } from '$app/navigation';

  let firstName = '';
  let lastName = '';
  let email = '';
  let password = '';
  let type = "Sales employee";
  let options = [
    "Sales employee",
    "Sales manager",
    "Account manager"
  ]
</script>

<main class="flex items-center justify-center min-h-screen bg-gray-100">
  <div class="w-full max-w-md p-8 space-y-6 bg-white rounded-lg shadow-md">
    <h2 class="text-2xl font-bold text-center text-gray-900">Create an employee account</h2>
    <form class="space-y-4">
      <div>
        <input type="firstName" placeholder="FirstName" bind:value={firstName} required class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring focus:ring-blue-200" />
      </div>
      
      <div>
        <input type="lastName" placeholder="LastName" bind:value={lastName} required class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring focus:ring-blue-200" />
      </div>

      <div>
        <input type="email" placeholder="Email" bind:value={email} required class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring focus:ring-blue-200" />
      </div>

      <div>
        <input type="password" placeholder="Password" bind:value={password} required class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring focus:ring-blue-200" />
      </div>

      <select bind:value ={type} placeholder="Type" required class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring focus:ring-blue-200">
        {#each options as value}
          <option {value}>
            {value}
          </option>
        {/each}
      </select>

      <div>
        <button type="confirm" class="w-full px-4 py-2 font-bold text-white bg-blue-500 rounded-md hover:bg-blue-600 focus:outline-none focus:ring focus:ring-blue-200">Confirm</button>
      </div>
    </form>
  </div>
</main>