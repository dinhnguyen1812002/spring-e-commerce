package com.app.e_commerce.services;

import com.app.e_commerce.DTO.OrderNotificationDTO;
import com.app.e_commerce.Enum.OrderStatus;
import com.app.e_commerce.entity.Order;
import com.app.e_commerce.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for sending real-time notifications via WebSocket
 */
@Service
public class NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Send notification about new order to admin
     */
    public void notifyAdminAboutNewOrder(Order order) {
        if (order == null || order.getUser() == null) {
            return;
        }

        User user = order.getUser();
        OrderNotificationDTO notification = new OrderNotificationDTO(
                order.getId(),
                user.getId().toString(),
                user.getUsername(),
                user.getAvatar(),
                order.getTotalAmount(),
                order.getOrderStatus(),
                order.getOrderDate(),
                "New order #" + order.getId() + " has been placed",
                OrderNotificationDTO.NotificationType.NEW_ORDER
        );

        // Send to admin topic
        messagingTemplate.convertAndSend("/topic/admin/orders", notification);
    }

    /**
     * Send notification about order status change to user
     */
    public void notifyUserAboutOrderStatusChange(Order order, OrderStatus previousStatus) {
        if (order == null || order.getUser() == null) {
            return;
        }

        User user = order.getUser();
        String message = generateStatusChangeMessage(order.getOrderStatus(), previousStatus, order.getId());

        OrderNotificationDTO notification = new OrderNotificationDTO(
                order.getId(),
                user.getId().toString(),
                user.getUsername(),
                user.getAvatar(),
                order.getTotalAmount(),
                order.getOrderStatus(),
                order.getOrderDate(),
                message,
                OrderNotificationDTO.NotificationType.STATUS_CHANGE
        );

        // Send to user-specific topic
        messagingTemplate.convertAndSend("/topic/user/" + user.getId() + "/orders", notification);
    }

    /**
     * Generate appropriate message based on status change
     */
    private String generateStatusChangeMessage(OrderStatus newStatus, OrderStatus previousStatus, String orderId) {
        String baseMessage = "Order #" + orderId + " ";

        switch (newStatus) {
            case PROCESSING:
                return baseMessage + "is now being processed";
            case SHIPPED:
                return baseMessage + "has been shipped";
            case DELIVERED:
                return baseMessage + "has been delivered";
            case CANCELLED:
                return baseMessage + "has been cancelled";
            case SUCCESS:
                return baseMessage + "has been completed";
            default:
                return baseMessage + "status has changed from " + previousStatus + " to " + newStatus;
        }
    }
}
