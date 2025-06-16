package io.github.fontysvenlo.ais.businesslogic.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class to retrieve error messages from a central JSON file.
 */
public class ErrorMessages {
    private static JsonNode messages;

    static {
        try (InputStream input = ErrorMessages.class.getClassLoader().getResourceAsStream("lang/ErrorMessages.json")) {
            if (input != null) {
                ObjectMapper mapper = new ObjectMapper();
                messages = mapper.readTree(input).get("messages");
            } else {
            }
        } catch (IOException ignored) {
        }
    }

    /**
     * Get an error message by key.
     * 
     * @param key The key of the error message
     * @return The error message, or "Unknown error" if not found
     */
    public static String getMessage(String key) {
        if (messages == null || !messages.has(key)) {
            return "Unknown error";
        }
        return messages.get(key).asText();
    }
    
    /**
     * Get an error message by key with placeholders.
     * 
     * @param key The key of the error message
     * @param params Array of parameters to replace {0}, {1}, etc. in the message
     * @return The formatted error message
     */
    public static String getMessage(String key, Object... params) {
        String message = getMessage(key);
        
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                message = message.replace("{" + i + "}", String.valueOf(params[i]));
            }
        }
        
        return message;
    }
}
