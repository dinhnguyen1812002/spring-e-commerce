package com.app.e_commerce.exception.invoice;

/**
 * Exception thrown when a user lacks permission to edit a specific invoice field
 */
public class InsufficientPermissionException extends RuntimeException {
    
    public InsufficientPermissionException(String message) {
        super(message);
    }
    
    public InsufficientPermissionException(String fieldName, String userId) {
        super(String.format("User %s does not have permission to edit field: %s", userId, fieldName));
    }
    
    public static InsufficientPermissionException forField(String fieldName) {
        return new InsufficientPermissionException(
            String.format("Insufficient permission to edit field: %s", fieldName)
        );
    }
}
