<script>
    import {goto} from '$app/navigation';

    let email = '';
    let password = '';

    async function handleLogin(event) {
        event.preventDefault();

        const response = await fetch('/api/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({email, password})
        });

        if (response.ok) {
            goto('/');
        } else {
            console.error('Login failed');
        }
    }
</script>

<style>

</style>

<div class="flex items-center justify-center min-h-screen">
    <div class="w-full max-w-md p-8 space-y-6 bg-white rounded-lg shadow-md">
        <h2 class="text-2xl font-bold text-center text-gray-900">Login</h2>
        <form class="space-y-4" on:submit={handleLogin}>
            <div>
                <input type="email" placeholder="Email" bind:value={email} required
                       class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring focus:ring-blue-200" />
            </div>
            <div>
                <input type="password" placeholder="Password" bind:value={password} required
                       class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring focus:ring-blue-200" />
            </div>
            <div>
                <button type="submit"
                        class="w-full px-4 py-2 font-bold text-white bg-blue-500 rounded-md hover:bg-blue-600 focus:outline-none focus:ring focus:ring-blue-200">
                    Login
                </button>
            </div>
        </form>
    </div>
</div>
