package io.github.fontysvenlo.ais.assembler;

import java.util.List;
import java.util.Properties;

import io.github.fontysvenlo.ais.businesslogic.BusinessLogicFactory;
import io.github.fontysvenlo.ais.businesslogic.api.BusinessLogic;
import io.github.fontysvenlo.ais.persistence.DBConfig;
import io.github.fontysvenlo.ais.persistence.PersistenceFactory;
import io.github.fontysvenlo.ais.persistence.api.Persistence;
import io.github.fontysvenlo.ais.restapi.APIServer;
import io.github.fontysvenlo.ais.restapi.ServerConfig;

/**
 * The Assembler class starts different layers of the application and connects
 * them.
 */
public class Assembler {

    /**
     * Private constructor to prevent instantiation.
     */
    private Assembler() {
    }

    /**
     * Starts the application with the given configurations.
     *
     * @param dbConfig the database configuration
     * @param serverConfig the server configuration
     */
    public static void start(DBConfig dbConfig, ServerConfig serverConfig) {
        // Get API key from environment variable
        String apiKey = System.getenv("AVIATIONSTACK_API_KEY");

        // If API key is not set in environment, try to read from .env file
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = readApiKeyFromEnvFile();
        }

        System.out.println("API Key: " + (apiKey != null ? "[FOUND]" : "null"));

        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("WARNING: AVIATIONSTACK_API_KEY environment variable is not set and not found in .env file. Flight functionality will be limited.");
        }

        Persistence persistence = PersistenceFactory.getInstance(dbConfig);
        BusinessLogic businessLogic = BusinessLogicFactory.getInstance(persistence);

        APIServer restServer = new APIServer(businessLogic, apiKey);
        restServer.start(serverConfig);
    }

    /**
     * Reads the API key from the .env file in the project root.
     * 
     * @return the API key, or null if not found
     */
    private static String readApiKeyFromEnvFile() {
        // List of possible locations for the .env file
        String[] possiblePaths = {
            ".env",
            "../.env",
            "../backend/.env",
            "/app/.env",
            "/app/backend/.env",
            "../../.env",
            System.getProperty("user.dir") + "/.env"
        };
        
        System.out.println("Current working directory: " + System.getProperty("user.dir"));
        System.out.println("Searching for .env file in the following locations:");
        
        for (String pathStr : possiblePaths) {
            try {
                java.nio.file.Path envPath = java.nio.file.Paths.get(pathStr);
                System.out.println("Checking path: " + envPath.toAbsolutePath());
                
                if (java.nio.file.Files.exists(envPath)) {
                    System.out.println("✓ Found .env file at: " + envPath.toAbsolutePath());
                    
                    List<String> lines = java.nio.file.Files.readAllLines(envPath);
                    for (String line : lines) {
                        // Skip comment lines and empty lines
                        if (line.trim().isEmpty() || line.trim().startsWith("//")) {
                            continue;
                        }
                        
                        if (line.contains("AVIATIONSTACK_API_KEY=")) {
                            String apiKey = line.substring(line.indexOf("AVIATIONSTACK_API_KEY=") + "AVIATIONSTACK_API_KEY=".length()).trim();
                            System.out.println("Found API key in .env file");
                            return apiKey;
                        }
                    }
                    
                    System.out.println("AVIATIONSTACK_API_KEY entry not found in .env file");
                } else {
                    System.out.println("✗ Not found at: " + envPath.toAbsolutePath());
                }
            } catch (Exception e) {
                System.out.println("Error checking path " + pathStr + ": " + e.getMessage());
            }
        }
        
        System.out.println("Could not find .env file in any of the checked locations");
        System.out.println("For Docker deployment, consider adding the environment variable directly in docker-compose.yml:");
        System.out.println("  environment:");
        System.out.println("    - AVIATIONSTACK_API_KEY=your_api_key_here");
        
        return null;
    }

    /**
     * The main method starts the application from the properties files.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Properties dbProperties = PropertiesLoader.loadProperties("db.properties");
        DBConfig databaseConfig = DBConfig.fromProperties(dbProperties, "aisdb");

        Properties serverProperties = PropertiesLoader.loadProperties("application.properties");
        ServerConfig serverConfig = ServerConfig.fromProperties(serverProperties);

        start(databaseConfig, serverConfig);
    }
}
