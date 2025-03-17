<script>
  import { onMount } from "svelte";

  let email = "";
  let password = "";
  let rememberMe = false;

  const handleLogin = async (event) => {
    event.preventDefault(); // Prevent form submission reload

    console.log("Logging in with:", { email, password, rememberMe });

    // Simulate API call
    try {
      const response = await fetch("http://localhost:8080/api/v1/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password, rememberMe }),
      });

      if (!response.ok) throw new Error("Login failed");
      console.log("Login successful!");
    } catch (error) {
      console.error(error);
    }
  };
</script>

<div class="flex items-center justify-center min-h-screen bg-gray-100">
  <div class="w-full max-w-sm p-6 bg-white rounded-lg shadow-lg">
    <!-- Logo -->
    <div class="flex justify-center mb-4">
      <img src="/SkyVoyage Logo.png" alt="SkyVoyage Logo" class="h-10" />
    </div>

    <!-- Heading -->
    <h2 class="text-2xl font-semibold text-gray-800">Login</h2>
    <p class="text-sm text-gray-600">
      Welcome back. Enter your credentials to access your account
    </p>

    <!-- Login Form -->
    <form on:submit={handleLogin} class="mt-4">
      <!-- Email Input -->
      <div>
        <label class="block text-sm font-medium text-gray-700" for="email">
          Enter your email
        </label>
        <input
          id="email"
          type="email"
          bind:value={email}
          required
          class="w-full mt-1 p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          placeholder="Email"
          aria-label="Email address"
        />
      </div>

      <!-- Password Input -->
      <div class="mt-4">
        <label class="block text-sm font-medium text-gray-700" for="password">
          Enter your password
        </label>
        <input
          id="password"
          type="password"
          bind:value={password}
          required
          class="w-full mt-1 p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          placeholder="Password"
          aria-label="Password"
        />
      </div>

      <!-- Forgot Password Link -->
      <div class="mt-2 text-right">
        <a href="/forgot-password" class="text-sm text-blue-600 hover:underline"
          >Forgot password?</a
        >
      </div>

      <!-- Remember Me Checkbox -->
      <div class="mt-4 flex items-center">
        <input
          id="remember"
          type="checkbox"
          bind:checked={rememberMe}
          class="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
        />
        <label for="remember" class="ml-2 text-sm text-gray-700">
          Keep me signed in
        </label>
      </div>

      <!-- Submit Button -->
      <button
        type="submit"
        class="w-full mt-4 p-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition"
        disabled={!(email && password)}
      >
        Continue
      </button>
    </form>
  </div>
</div>
