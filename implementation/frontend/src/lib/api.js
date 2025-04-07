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
        // Fix: Ensure there's a leading slash on the endpoint if it doesn't start with one
        const formattedEndpoint = endpoint.startsWith('/') ? endpoint : `/${endpoint}`;
        const url = `${baseUrl}${formattedEndpoint}`;

        options.headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };

        try {
            // Clear debug message before each request
            console.clear();
            console.log(`API REQUEST: ${options.method || 'GET'} to ${url}`);
            if (options.body) {
                console.log('REQUEST BODY:', options.body);
                // Log as parsed JSON for better debugging
                try {
                    const parsedBody = JSON.parse(options.body);
                    console.log('PARSED BODY:', parsedBody);
                } catch (e) {
                    console.log('BODY IS NOT VALID JSON');
                }
            }

            // Try using a different approach for PUT requests as a workaround
            const response = await fetch(url, {
                ...options,
                // Ensure method is uppercase
                method: options.method ? options.method.toUpperCase() : 'GET'
            });

            console.log(`API RESPONSE STATUS: ${response.status} ${response.statusText}`);

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
            body: JSON.stringify(data)
        });
    },

    async getOne(endpoint, id) {
        return this.fetchAPI(`${endpoint}/${id}`);
    },

    async update(endpoint, id, data) {
        console.log(`Sending PATCH request to ${endpoint}/${id}`);
        return this.fetchAPI(`${endpoint}/${id}`, {
            method: 'PATCH',  // Changed back to PATCH as the server expects this
            body: data
        });
    },

    async delete(endpoint, id) {
        // Check if id is null/undefined or if the endpoint already includes the id
        const url = id ? `${endpoint}/${id}` : endpoint;

        return this.fetchAPI(url, {
            method: 'DELETE'
        });
    }
};