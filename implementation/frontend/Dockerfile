# Base image
FROM node:18

WORKDIR /app

# Copy package files
COPY package.json package-lock.json ./

# Install dependencies
RUN npm install

# Copy the rest of the frontend files
COPY . .

# Expose the port Vite runs on
EXPOSE 5173

# Run Vite's development server and bind to 0.0.0.0
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0"]