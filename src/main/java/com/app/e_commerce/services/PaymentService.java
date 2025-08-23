package com.app.e_commerce.services;

import com.app.e_commerce.Enum.OrderStatus;
import com.app.e_commerce.Enum.PaymentMethod;
import com.app.e_commerce.entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private OrderService orderService;

    /**
     * Process payment for an order
     * @param order The order to process payment for
     * @param paymentMethod The payment method
     * @param paymentDetails Additional payment details
     * @return Map containing payment result and transaction ID
     */
    @Transactional
    public Map<String, Object> processPayment(Order order, PaymentMethod paymentMethod, Map<String, String> paymentDetails) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Generate a transaction ID
            String transactionId = generateTransactionId();
            
            // Process payment based on payment method
            boolean paymentSuccess = false;
            
            switch (paymentMethod) {
                case CREDIT_CARD:
                    paymentSuccess = processCreditCardPayment(order, paymentDetails, transactionId);
                    break;
                case PAYPAL:
                    paymentSuccess = processPayPalPayment(order, paymentDetails, transactionId);
                    break;
                case CASH_ON_DELIVERY:
                    // COD doesn't require immediate payment processing
                    paymentSuccess = true;
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
            }
            
            // Update order status based on payment result
            if (paymentSuccess) {
                if (paymentMethod == PaymentMethod.CASH_ON_DELIVERY) {
                    // For COD, payment is not completed yet
                    orderService.updateOrderStatus(order.getId(), OrderStatus.PROCESSING);
                } else {
                    // For other methods, payment is completed
                    orderService.updateOrderStatus(order.getId(), OrderStatus.PROCESSING);
                    orderService.setPaymentTransactionId(order.getId(), transactionId);
                }
                
                result.put("success", true);
                result.put("transactionId", transactionId);
                result.put("message", "Payment processed successfully");
            } else {
                orderService.updateOrderStatus(order.getId(), OrderStatus.FAILED);
                result.put("success", false);
                result.put("message", "Payment processing failed");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Payment error: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Process credit card payment
     * @param order The order
     * @param paymentDetails Payment details
     * @param transactionId Transaction ID
     * @return true if payment was successful, false otherwise
     */
    private boolean processCreditCardPayment(Order order, Map<String, String> paymentDetails, String transactionId) {
        // In a real application, this would integrate with a payment gateway
        // For this example, we'll simulate a successful payment
        
        // Validate required fields
        if (!paymentDetails.containsKey("cardNumber") || 
            !paymentDetails.containsKey("expiryDate") || 
            !paymentDetails.containsKey("cvv")) {
            return false;
        }
        
        // Simple validation (in real app, would be more robust)
        String cardNumber = paymentDetails.get("cardNumber").replaceAll("\\s", "");
        if (cardNumber.length() < 13 || cardNumber.length() > 19) {
            return false;
        }
        
        // Simulate payment processing
        return simulatePaymentProcessing(order.getTotalAmount());
    }
    
    /**
     * Process PayPal payment
     * @param order The order
     * @param paymentDetails Payment details
     * @param transactionId Transaction ID
     * @return true if payment was successful, false otherwise
     */
    private boolean processPayPalPayment(Order order, Map<String, String> paymentDetails, String transactionId) {
        // In a real application, this would integrate with PayPal API
        // For this example, we'll simulate a successful payment
        
        // Validate required fields
        if (!paymentDetails.containsKey("paypalEmail")) {
            return false;
        }
        
        // Simulate payment processing
        return simulatePaymentProcessing(order.getTotalAmount());
    }
    
    /**
     * Simulate payment processing
     * @param amount The payment amount
     * @return true if payment was successful, false otherwise
     */
    private boolean simulatePaymentProcessing(BigDecimal amount) {
        // In a real application, this would call a payment gateway
        // For this example, we'll simulate a 95% success rate
        return Math.random() < 0.95;
    }
    
    /**
     * Generate a unique transaction ID
     * @return Transaction ID
     */
    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Validate payment details
     * @param paymentMethod The payment method
     * @param paymentDetails Payment details
     * @return Map containing validation result and error message if any
     */
    public Map<String, Object> validatePaymentDetails(PaymentMethod paymentMethod, Map<String, String> paymentDetails) {
        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);
        
        switch (paymentMethod) {
            case CREDIT_CARD:
                if (!paymentDetails.containsKey("cardNumber") || 
                    !paymentDetails.containsKey("expiryDate") || 
                    !paymentDetails.containsKey("cvv")) {
                    result.put("valid", false);
                    result.put("message", "Missing required credit card details");
                }
                break;
            case PAYPAL:
                if (!paymentDetails.containsKey("paypalEmail")) {
                    result.put("valid", false);
                    result.put("message", "Missing PayPal email");
                }
                break;
            case CASH_ON_DELIVERY:
                // No validation needed for COD
                break;
            default:
                result.put("valid", false);
                result.put("message", "Unsupported payment method");
        }
        
        return result;
    }
}