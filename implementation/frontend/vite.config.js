import tailwindcss from "@tailwindcss/vite";
import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vite';

export default defineConfig({
	plugins: [sveltekit(), tailwindcss()],
	server: {
		host: '0.0.0.0', // Allow access from outside the container
		watch: {
			usePolling: true, // Enable file watching in Docker
		},
	},
});
