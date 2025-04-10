<script>
    import { goto } from '$app/navigation';
    let email = "";
    let password = "";
    let rememberMe = false;

    const handleLogin = async () => {
        try {
            const response = await fetch("http://localhost:8080/api/v1/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, password }),
            });

            if (response.ok) {
                const data = await response.json();
                console.log(data)
                if(data.firstname && data.lastname && data.type) {
                    // save user data to cookies
                    document.cookie = `firstname=${data.firstname};`;
                    document.cookie = `lastname=${data.lastname};`;
                    document.cookie = `type=${data.type};`;
                    // Notify layout to update user info
                    window.dispatchEvent(new Event('userInfoChanged'));

                    goto("/home");
                } else {
                    console.error("Invalid response format");
                }

            } else {
                const error = await response.text();
                console.error(error);
            }
        } catch (err) {
            console.error(error);
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

        <!-- Forgot Password Link -->
        <div class="mt-2 text-right">
            <a href="/forgot-password" class="text-sm text-blue-600 hover:underline"
            >Forgot password?</a
            >
        </div>

        <!-- Remember Me Checkbox -->
        <div class="mt-4 flex items-center">
            <input
                    type="checkbox"
                    bind:checked={rememberMe}
                    class="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
            />
            <label class="ml-2 text-sm text-gray-700">Keep me signed in</label>
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
