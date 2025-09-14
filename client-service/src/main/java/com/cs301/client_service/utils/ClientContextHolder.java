package com.cs301.client_service.utils;

/**
 * Utility class to store and retrieve client information in thread-local variables.
 * This allows us to pass information from the service methods to the aspects.
 */
public class ClientContextHolder {
    private static final ThreadLocal<String> clientId = new ThreadLocal<>();
    private static final ThreadLocal<String> clientEmail = new ThreadLocal<>();
    private static final ThreadLocal<String> accountType = new ThreadLocal<>();
    
    /**
     * Set the client ID for the current thread.
     * @param id The client ID
     */
    public static void setClientId(String id) {
        clientId.set(id);
    }
    
    /**
     * Get the client ID for the current thread.
     * @return The client ID
     */
    public static String getClientId() {
        return clientId.get();
    }
    
    /**
     * Set the client email for the current thread.
     * @param email The client email
     */
    public static void setClientEmail(String email) {
        clientEmail.set(email);
    }
    
    /**
     * Get the client email for the current thread.
     * @return The client email
     */
    public static String getClientEmail() {
        return clientEmail.get();
    }
    
    /**
     * Set the account type for the current thread.
     * @param type The account type
     */
    public static void setAccountType(String type) {
        accountType.set(type);
    }
    
    /**
     * Get the account type for the current thread.
     * @return The account type
     */
    public static String getAccountType() {
        return accountType.get();
    }
    
    /**
     * Clear all thread-local variables.
     * This should be called after the request is processed to prevent memory leaks.
     */
    public static void clear() {
        clientId.remove();
        clientEmail.remove();
        accountType.remove();
    }
}
