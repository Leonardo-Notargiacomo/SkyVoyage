import { PUBLIC_API_BASE } from '$env/static/public';

const fetchAPI = async (resource, options = {}) => {
    const sep = (resource.startsWith("/")) ? '' : '/';
    const response = await fetch(`${PUBLIC_API_BASE}${sep}${resource}`, options);
    if (!response.ok) {
        throw new Error(response.statusText);
    }
    return await response.json();
}

export const api = {
    async fetchAPI(endpoint, options = {}) {
        const baseUrl = 'http://localhost:8080/api/v1';
        const url = `${baseUrl}${endpoint}`;

        options.headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };

        try {
            const response = await fetch(url, options);

            // For debugging
            console.log(`API ${options.method || 'GET'} ${url} status: ${response.status}`);

            if (!response.ok) {
                // Try to parse error response
                const errorText = await response.text();
                let errorData;
                try {
                    errorData = JSON.parse(errorText);
                } catch (e) {
                    errorData = { error: errorText || 'Unknown error' };
                }

                // Create error with response attached
                const error = new Error(response.statusText || 'API request failed');
                error.status = response.status;
                error.response = response;
                error.errorData = errorData;
                throw error;
            }

            // Only try to parse JSON if there's content
            if (response.status !== 204) {
                return await response.json();
            }
            return null;
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    },

    async all(endpoint) {
        return this.fetchAPI(endpoint);
    },

    async create(endpoint, data) {
        return this.fetchAPI(endpoint, {
            method: 'POST',
            body: data
        });
    },

    // ... other methods
};