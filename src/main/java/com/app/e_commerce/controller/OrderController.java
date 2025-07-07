package com.app.e_commerce.controller;

import com.app.e_commerce.Enum.OrderStatus;
import com.app.e_commerce.entity.Cart;
import com.app.e_commerce.entity.Order;
import com.app.e_commerce.entity.User;
import com.app.e_commerce.exception.ResourceNotFoundException;
import com.app.e_commerce.services.CartService;
import com.app.e_commerce.services.OrderService;
import com.app.e_commerce.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userService;
    @Autowired
    private CartService cartService;

    @GetMapping("/checkout")
    public String checkout(Model model, HttpSession session, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
//        if (result.hasErrors()) {
//            return "cart/checkout";
//        }

        User user = userService.findByUsername(principal.getName());
        Cart cart = cartService.getOrCreateCart(user, session);

        if (cart.getCartItems().isEmpty()) {
            return "redirect:/cart?error=empty";
        }

        model.addAttribute("cart", cart);
        return "cart/checkout";
    }

    @GetMapping("/confirm/{orderId}")
    public String confirmOrder(@PathVariable("orderId") String orderId, Model model) {
        Order order = orderService.findOrderById(orderId);
        if (order != null && order.getOrderStatus() == OrderStatus.PENDING) {
            order.setOrderStatus(OrderStatus.SUCCESS); // Update order status to SUCCESS
            orderService.saveOrder(order);  // Save the updated order
            return "redirect:/orders/success";
        }
        return "redirect:/orders/invalid";  // Handle invalid or already confirmed orders
    }

    @GetMapping("/success")
    public String orderSuccess() {
        return "orders/success";  // Return a Thymeleaf page showing payment success
    }

    @GetMapping
    public String getOrderHistory(
            Model model,
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        User user = userService.findByUsername(principal.getName());
        Page<Order> orderPage;

        if (status != null) {
            // Filter by status
            orderPage = orderService.getOrdersByUserAndStatus(user, status, PageRequest.of(page, size));
        } else if (startDate != null && endDate != null) {
            // Filter by date range
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.atTime(LocalTime.MAX);
            orderPage = orderService.getOrdersByUserAndDateRange(user, start, end, PageRequest.of(page, size));
        } else {
            // Get all orders
            orderPage = orderService.getOrdersByUserPaginated(user, PageRequest.of(page, size));
        }

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalItems", orderPage.getTotalElements());

        // Add additional attributes for filtering
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "users/order-history";
    }

    @GetMapping("/{orderId}")
    public String getOrderDetails(@PathVariable String orderId, Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        Order order = orderService.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        model.addAttribute("order", order);
        return "users/order-details";
    }
}



