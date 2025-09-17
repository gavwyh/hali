package com.cs301.client_service.utils;

import com.cs301.client_service.exceptions.EntityComparisonException;
import com.cs301.client_service.exceptions.JsonConversionException;
import com.cs301.client_service.models.Account;
import com.cs301.client_service.models.Client;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

@Component
public class LoggingUtils {
    private static final String SYSTEM_USER = "system";
    private static final String CLIENT_ATTRIBUTE_NAMES = "clientId,firstName,lastName,dateOfBirth,gender,emailAddress,phoneNumber,address,city,state,country,postalCode,nric,agentId";
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingUtils.class);
    private static ObjectMapper objectMapper;

    @Autowired
    private LoggingUtils(ObjectMapper mapper) {
        objectMapper = mapper;
    }
    
    // Fallback if injection fails
    private static ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            // Configure the mapper for proper date handling
            objectMapper.findAndRegisterModules();
            // Configure to handle circular references
            objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        }
        return objectMapper;
    }

    /**
     * Compares two entities and returns a map of changed properties with their before and after values
     * @param oldEntity The entity before changes
     * @param newEntity The entity after changes
     * @return Map with property name as key and pair of before/after values
     */
    public static Map<String, Map.Entry<String, String>> compareEntities(Object oldEntity, Object newEntity) {
        if (oldEntity == null || newEntity == null || !oldEntity.getClass().equals(newEntity.getClass())) {
            throw new IllegalArgumentException("Entities must be non-null and of the same type");
        }

        Map<String, Map.Entry<String, String>> changes = new HashMap<>();
        
        // Special handling for Client objects with explicit field comparison
        if (oldEntity instanceof Client && newEntity instanceof Client) {
            Client oldClient = (Client) oldEntity;
            Client newClient = (Client) newEntity;
            
            // Explicitly check each field for Client objects
            if (!equals(oldClient.getFirstName(), newClient.getFirstName())) {
                changes.put("firstName", Map.entry(
                    toString(oldClient.getFirstName()), 
                    toString(newClient.getFirstName())));
            }
            
            if (!equals(oldClient.getLastName(), newClient.getLastName())) {
                changes.put("lastName", Map.entry(
                    toString(oldClient.getLastName()), 
                    toString(newClient.getLastName())));
            }
            
            if (!equals(oldClient.getEmailAddress(), newClient.getEmailAddress())) {
                changes.put("emailAddress", Map.entry(
                    toString(oldClient.getEmailAddress()), 
                    toString(newClient.getEmailAddress())));
            }
            
            if (!equals(oldClient.getPhoneNumber(), newClient.getPhoneNumber())) {
                changes.put("phoneNumber", Map.entry(
                    toString(oldClient.getPhoneNumber()), 
                    toString(newClient.getPhoneNumber())));
            }
            
            if (!equals(oldClient.getAddress(), newClient.getAddress())) {
                changes.put("address", Map.entry(
                    toString(oldClient.getAddress()), 
                    toString(newClient.getAddress())));
            }
            
            if (!equals(oldClient.getCity(), newClient.getCity())) {
                changes.put("city", Map.entry(
                    toString(oldClient.getCity()), 
                    toString(newClient.getCity())));
            }
            
            if (!equals(oldClient.getState(), newClient.getState())) {
                changes.put("state", Map.entry(
                    toString(oldClient.getState()), 
                    toString(newClient.getState())));
            }
            
            if (!equals(oldClient.getCountry(), newClient.getCountry())) {
                changes.put("country", Map.entry(
                    toString(oldClient.getCountry()), 
                    toString(newClient.getCountry())));
            }
            
            if (!equals(oldClient.getPostalCode(), newClient.getPostalCode())) {
                changes.put("postalCode", Map.entry(
                    toString(oldClient.getPostalCode()), 
                    toString(newClient.getPostalCode())));
            }
            
            if (!equals(oldClient.getNric(), newClient.getNric())) {
                changes.put("nric", Map.entry(
                    toString(oldClient.getNric()), 
                    toString(newClient.getNric())));
            }
            
            if (!equals(oldClient.getAgentId(), newClient.getAgentId())) {
                changes.put("agentId", Map.entry(
                    toString(oldClient.getAgentId()), 
                    toString(newClient.getAgentId())));
            }
            
            if (!equals(oldClient.getVerificationStatus(), newClient.getVerificationStatus())) {
                changes.put("verificationStatus", Map.entry(
                    toString(oldClient.getVerificationStatus()), 
                    toString(newClient.getVerificationStatus())));
            }
            
            if (!equals(oldClient.getDateOfBirth(), newClient.getDateOfBirth())) {
                changes.put("dateOfBirth", Map.entry(
                    toString(oldClient.getDateOfBirth()), 
                    toString(newClient.getDateOfBirth())));
            }
            
            if (!equals(oldClient.getGender(), newClient.getGender())) {
                changes.put("gender", Map.entry(
                    toString(oldClient.getGender()), 
                    toString(newClient.getGender())));
            }
            
            // clientId should never change, so skip it
            
            return changes;
        }
        
        // Generic handling for other entity types
        try {
            compareGenericEntities(oldEntity, newEntity, changes);
        } catch (Exception e) {
            throw new EntityComparisonException("Error comparing entities", e);
        }
        
        return changes;
    }
    
    // Helper method to safely compare possibly null values
    private static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }
    
    // Helper method to safely convert objects to string
    private static String toString(Object obj) {
        return obj != null ? obj.toString() : "";
    }
    
    /**
     * Helper method to compare generic entities using reflection
     * @param oldEntity The entity before changes
     * @param newEntity The entity after changes
     * @param changes Map to store the detected changes
     * @throws Exception If there's an error accessing properties
     */
    private static void compareGenericEntities(Object oldEntity, Object newEntity, 
            Map<String, Map.Entry<String, String>> changes) throws Exception {
        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(oldEntity.getClass());
        
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            String propertyName = propertyDescriptor.getName();
            
            // Skip class property and collections
            if ("class".equals(propertyName) || "accounts".equals(propertyName)) {
                continue;
            }
            
            Method readMethod = propertyDescriptor.getReadMethod();
            if (readMethod != null) {
                Object oldValue = readMethod.invoke(oldEntity);
                Object newValue = readMethod.invoke(newEntity);
                
                // Check if values are different
                if ((oldValue == null && newValue != null) || 
                    (oldValue != null && !oldValue.equals(newValue))) {
                    
                    String oldValueStr = oldValue != null ? oldValue.toString() : "";
                    String newValueStr = newValue != null ? newValue.toString() : "";
                    
                    changes.put(propertyName, Map.entry(oldValueStr, newValueStr));
                }
            }
        }
    }

    /**
     * Converts an entity to a JSON string
     * @param entity The entity to convert
     * @return JSON string representation
     */
    public static String convertToString(Object entity) {
        if (entity == null) {
            return "";
        }
        
        try {
            if (entity instanceof Account account) {
                // Create a simplified representation to avoid circular references
                Map<String, Object> accountMap = new HashMap<>();
                accountMap.put("accountId", account.getAccountId());
                accountMap.put("accountType", account.getAccountType());
                accountMap.put("accountStatus", account.getAccountStatus());
                accountMap.put("openingDate", account.getOpeningDate());
                accountMap.put("initialDeposit", account.getInitialDeposit());
                accountMap.put("currency", account.getCurrency());
                accountMap.put("branchId", account.getBranchId());
                
                // Include client ID but not the full client object
                if (account.getClient() != null) {
                    accountMap.put("clientId", account.getClient().getClientId());
                }
                
                return getObjectMapper().writeValueAsString(accountMap);
            } else if (entity instanceof Client client) {
                // Create a simplified representation to avoid circular references
                Map<String, Object> clientMap = new HashMap<>();
                clientMap.put("clientId", client.getClientId());
                clientMap.put("firstName", client.getFirstName());
                clientMap.put("lastName", client.getLastName());
                clientMap.put("dateOfBirth", client.getDateOfBirth());
                clientMap.put("gender", client.getGender());
                clientMap.put("emailAddress", client.getEmailAddress());
                clientMap.put("phoneNumber", client.getPhoneNumber());
                clientMap.put("address", client.getAddress());
                clientMap.put("city", client.getCity());
                clientMap.put("state", client.getState());
                clientMap.put("country", client.getCountry());
                clientMap.put("postalCode", client.getPostalCode());
                clientMap.put("nric", client.getNric());
                clientMap.put("agentId", client.getAgentId());
                clientMap.put("verificationStatus", client.getVerificationStatus());
                
                // Don't include accounts to avoid circular references
                
                return getObjectMapper().writeValueAsString(clientMap);
            }
            
            // For other types, use the default serialization
            return getObjectMapper().writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            throw new JsonConversionException("Error converting entity to string", e);
        }
    }
    
    /**
     * Converts a JSON string back to an object
     * @param json The JSON string to convert
     * @param valueType The class of the object to convert to
     * @return The converted object
     */
    public static <T> T convertFromString(String json, Class<T> valueType) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        
        try {
            return getObjectMapper().readValue(json, valueType);
        } catch (JsonProcessingException e) {
            throw new JsonConversionException("Error converting string to object", e);
        }
    }

    /**
     * Extracts client ID from an entity
     * @param entity The entity to extract from
     * @return The client ID
     */
    public static String extractClientId(Object entity) {
        if (entity == null) {
            return null;
        }
        
        if (entity instanceof Client client) {
            return client.getClientId();
        } else if (entity instanceof Account account) {
            return account.getClient().getClientId();
        }
        
        return null;
    }

    /**
     * Gets the current agent ID from security context
     * @return The agent ID or "Admin" if the user is an admin
     */
    public static String getCurrentAgentId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null) {
                // If it's an agent, use their ID from JWT
                if (JwtAuthorizationUtil.isAgent(authentication)) {
                    String agentId = JwtAuthorizationUtil.getAgentId(authentication);
                    return agentId != null ? agentId : "system";
                }
                // If it's an admin, use "Admin"
                else if (JwtAuthorizationUtil.isAdmin(authentication)) {
                    return "Admin";
                }
            }
            
            // Default fallback
            return "system";
        } catch (Exception e) {
            // Log the error but don't let it break the app
            LoggerFactory.getLogger(LoggingUtils.class).error("Error getting agent ID from security context", e);
            return "system";
        }
    }
    
    /**
     * Gets the full name of a client (firstName + lastName)
     * @param clientId The client ID to get the name for
     * @return The client's full name or empty string if not found
     */
    public static String getClientFullName(String clientId) {
        if (clientId == null || clientId.isEmpty()) {
            return "";
        }
        
        try {
            // This is a placeholder until ClientService is properly injected
            return "";
        } catch (Exception e) {
            logger.error("Error getting client name for ID: {}", clientId, e);
            return "";
        }
    }
    
    /**
     * Converts a Client object to a comma-delimited string of values
     * @param client The client to convert
     * @return Comma-delimited string of values
     */
    public static String convertClientToCommaSeparatedValues(Client client) {
        if (client == null) {
            return "";
        }
        
        StringBuilder values = new StringBuilder();
        values.append(client.getClientId()).append(",")
              .append(client.getFirstName()).append(",")
              .append(client.getLastName()).append(",")
              .append(client.getDateOfBirth()).append(",")
              .append(client.getGender()).append(",")
              .append(client.getEmailAddress()).append(",")
              .append(client.getPhoneNumber()).append(",")
              .append(client.getAddress()).append(",")
              .append(client.getCity()).append(",")
              .append(client.getState()).append(",")
              .append(client.getCountry()).append(",")
              .append(client.getPostalCode()).append(",")
              .append(client.getNric()).append(",")
              .append(client.getAgentId());
        
        return values.toString();
    }
    
    /**
     * Gets a comma-delimited string of client attribute names
     * @return Comma-delimited string of attribute names
     */
    public static String getClientAttributeNames() {
        return CLIENT_ATTRIBUTE_NAMES;
    }
}
