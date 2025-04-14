<script>
    import { goto } from '$app/navigation';
    import { api } from "$lib/api";

    let email = "";
    let password = "";
    let activeUser = false;
    let errorMessage = "";

    const handleLogin = async () => {
        try {
            const response = await api.fetchAPI("login", {
                method: "POST",
                body: JSON.stringify({ email, password })
            });
            
            const data = await api.fetchAPI(`getLoginUser?email=${encodeURIComponent(email)}`);
        
            // Save user data to cookies
            document.cookie = `firstname=${data.Firstname}; path=/;`;
            document.cookie = `lastname=${data.Lastname}; path=/;`;
            document.cookie = `type=${data.Type}; path=/;`;

            // Notify layout to update user info
            window.dispatchEvent(new Event('userInfoChanged'));
            activeUser = true;

            // Redirect to home page
            goto("/home");
            
        } catch (err) {
            errorMessage = "Invalid email or password. Please try again.";
            console.error(err);
        }
    };
</script>

<div class="flex items-center justify-center min-h-screen fixed inset-0">
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

        <!-- Error Message -->
        {#if errorMessage}
            <div class="mt-4 p-2 text-sm text-red-600 bg-red-100 rounded">
                {errorMessage}
            </div>
        {/if}

        <!-- Email Input -->
        <div class="mt-4">
            <label class="block text-sm font-medium text-gray-700"
            >Enter your email</label
            >
            <input
                    type="email"
                    bind:value={email}
                    class="w-full mt-1 p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="Email"
            />
        </div>

        <!-- Password Input -->
        <div class="mt-4">
            <label class="block text-sm font-medium text-gray-700"
            >Enter your password</label
            >
            <input
                    type="password"
                    bind:value={password}
                    class="w-full mt-1 p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="Password"
            />
        </div>


        <!-- Submit Button -->
        <button
                on:click={handleLogin}
                class="w-full mt-4 p-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition"
        >
            Continue
        </button>
    </div>
</div>
