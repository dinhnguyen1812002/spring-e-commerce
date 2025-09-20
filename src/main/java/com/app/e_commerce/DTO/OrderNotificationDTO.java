package com.app.e_commerce.DTO;

import com.app.e_commerce.Enum.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for sending order notifications via WebSocket
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderNotificationDTO {
    private String id;
    private String userId;
    private String username;
    private String userAvatar;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime orderDate;
    private String message;
    private NotificationType type;
    
    /**
     * Type of notification
     */
    public enum NotificationType {
        NEW_ORDER,       // New order notification for admin
        STATUS_CHANGE    // Order status change notification for user
    }
}