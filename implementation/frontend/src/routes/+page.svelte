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
            document.cookie = `employeeID=${data.id}; path=/;`;

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
    const handleKeyDown = (event) => {
        if (event.key === "Enter") {
            handleLogin();
        }
    };
</script>

<div class="login-page">
    <div class="login-background"></div>
    <div class="blur-overlay"></div>

    <div class="flex items-center justify-center min-h-screen fixed inset-0">
        <div class="w-full max-w-sm p-8 bg-white/90 backdrop-blur-md rounded-xl shadow-xl border border-white/20 transform transition-all duration-300 hover:shadow-2xl">
            <!-- Logo with enhanced styling -->
            <div class="flex justify-center mb-8">
                <div class="relative">
                    <div class="relative bg-white rounded-full p-2">
                        <img src="/SkyVoyage Logo.png" alt="SkyVoyage Logo" class="h-12" />
                    </div>
                </div>
            </div>

            <!-- Heading with improved styling -->
            <h1 class="text-center text-xl font-semibold text-gray-800 mb-2">Welcome Back</h1>
            <p class="text-center text-sm mb-8 text-gray-600">
                Enter your credentials to access your account
            </p>

            <!-- Error Message with improved styling -->
            {#if errorMessage}
                <div class="mt-4 p-3 text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg flex items-center mb-4">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                    </svg>
                    {errorMessage}
                </div>
            {/if}

            <!-- Email Input with enhanced styling -->
            <div class="mb-6">
                <label for="email" class="block text-sm font-medium text-gray-700 mb-1">Email Address</label>
                <div class="relative">
                    <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 12a4 4 0 10-8 0 4 4 0 008 0zm0 0v1.5a2.5 2.5 0 005 0V12a9 9 0 10-9 9m4.5-1.206a8.959 8.959 0 01-4.5 1.207" />
                        </svg>
                    </div>
                    <input
                        id="email"
                        type="email"
                        bind:value={email}
                        class="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
                        placeholder="Your email"
                    />
                </div>
            </div>

            <!-- Password Input with enhanced styling -->
            <div class="mb-8">
                <label for="password" class="block text-sm font-medium text-gray-700 mb-1">Password</label>
                <div class="relative">
                    <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                        </svg>
                    </div>
                    <input
                        id="password"
                        type="password"
                        bind:value={password}
                        class="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
                        placeholder="Your password"
                        on:keydown={handleKeyDown}
                    />
                </div>
            </div>

            <!-- Submit Button with modern styling -->
            <button
                on:click={handleLogin}
                class="w-full py-2.5 bg-gradient-to-r from-blue-600 to-blue-700 text-white rounded-lg font-medium transition-all duration-300 hover:from-blue-700 hover:to-blue-800 flex items-center justify-center shadow-lg hover:shadow-xl transform hover:-translate-y-0.5 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1" />
                </svg>
                Sign In
            </button>
        </div>
    </div>
</div>

<style>
    .login-page {
        position: relative;
        min-height: 100vh;
        width: 100%;
        overflow: hidden;
    }

    .login-background {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: 
            radial-gradient(circle at 10% 20%, rgba(91, 194, 255, 0.2) 0%, rgba(91, 194, 255, 0) 50%),
            radial-gradient(circle at 90% 80%, rgba(10, 71, 255, 0.2) 0%, rgba(10, 71, 255, 0) 50%),
            linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
        z-index: -2;
    }

    .blur-overlay {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%234299e1' fill-opacity='0.07'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E"),
            url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M11 18c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm48 25c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm-43-7c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm63 31c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM34 90c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm56-76c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM12 86c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm28-65c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm23-11c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-6 60c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm29 22c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zM32 63c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm57-13c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-9-21c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM60 91c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM35 41c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM12 60c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2z' fill='%234299e1' fill-opacity='0.05' fill-rule='evenodd'/%3E%3C/svg%3E");
        z-index: -1;
    }
</style>
