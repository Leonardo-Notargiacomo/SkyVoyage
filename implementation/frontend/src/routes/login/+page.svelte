<script>
    import {goto} from '$app/navigation';

    let email = '';
    let password = '';
    let message = '';

    async function handleLogin(event) {
        event.preventDefault();

        try {
            const response = await fetch('http://localhost:8080/api/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({email, password})
            });

            const text = await response.text(); // Read the response as text
            console.log('Response:', text); // Log the response

            if (text) {
                try {
                    const data = JSON.parse(text); // Attempt to parse the response as JSON
                    message = data.message || data.error;
                } catch (error) {
                    console.error('Error parsing JSON:', error);
                    message = 'Error parsing server response';
                }
            } else {
                message = 'Empty response from server';
            }

            if (response.ok) {
                goto('/dashboard');
            } else {
                console.error('Login failed');
            }
        } catch (error) {
            console.error('Fetch error:', error);
            message = 'Network error';
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
