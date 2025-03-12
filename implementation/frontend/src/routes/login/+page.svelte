<script>
  import { goto } from '$app/navigation';

  let email = '';
  let username = '';
  let password = '';
  let isRegister = false;

  function toggleForm() {
    isRegister = !isRegister;
  }

</script>

<main class="flex items-center justify-center min-h-screen bg-gray-100">
  <div class="w-full max-w-md p-8 space-y-6 bg-white rounded-lg shadow-md">
    <h2 class="text-2xl font-bold text-center text-gray-900">{isRegister ? 'Register' : 'Login'}</h2>
    <form class="space-y-4" on:submit>
      {#if isRegister}
        <div>
          <input type="text" placeholder="Username" bind:value={username} required class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring focus:ring-blue-200" />
        </div>
      {/if}
      <div>
        <input type="email" placeholder="Email" bind:value={email} required class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring focus:ring-blue-200" />
      </div>
      <div>
        <input type="password" placeholder="Password" bind:value={password} required class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring focus:ring-blue-200" />
      </div>
      <div>
        <button type="submit" class="w-full px-4 py-2 font-bold text-white bg-blue-500 rounded-md hover:bg-blue-600 focus:outline-none focus:ring focus:ring-blue-200">{isRegister ? 'Register' : 'Login'}</button>
      </div>
    </form>
    <button on:click={toggleForm} class="w-full px-4 py-2 mt-4 font-bold text-blue-500 bg-transparent border border-blue-500 rounded-md hover:bg-blue-500 hover:text-white focus:outline-none focus:ring focus:ring-blue-200">
      {isRegister ? 'Already have an account? Login' : 'Don\'t have an account? Register'}
    </button>
  </div>
</main>