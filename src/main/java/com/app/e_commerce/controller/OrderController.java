package com.app.e_commerce.controller;

import com.app.e_commerce.Enum.OrderStatus;
import com.app.e_commerce.Enum.PaymentMethod;
import com.app.e_commerce.entity.Cart;
import com.app.e_commerce.entity.Order;
import com.app.e_commerce.entity.User;
import com.app.e_commerce.exception.ProductNotFoundException;
import com.app.e_commerce.services.CartService;
import com.app.e_commerce.services.OrderService;
import com.app.e_commerce.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private CartService cartService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userService;

    /**
     * Trang checkout (form nhập thông tin)
     */

    @GetMapping("/checkout")
    public String checkout(Model model, HttpSession session, @AuthenticationPrincipal UserDetails userDetails) {
        User user = (userDetails != null)
                ? userService.findByUsername(userDetails.getUsername())
                : null;

        // Lấy giỏ hàng từ CartService
        Cart cart = cartService.getOrCreateCart(user, session);

        // Nếu giỏ hàng rỗng thì redirect về trang giỏ hàng
        if (cart.getCartItems().isEmpty()) {
            return "redirect:/cart";
        }

        model.addAttribute("cart", cart);
        model.addAttribute("paymentMethods", PaymentMethod.values());

        return "cart/checkout";
    }



    /**
     * Submit đơn hàng
     */
//    @PostMapping("/checkout")
//    public String placeOrder(@RequestParam String fullName,
//                             @RequestParam String phone,
//                             @RequestParam String address,
//                             @RequestParam(required = false) String note,
//                             @RequestParam PaymentMethod paymentMethod,
//                             HttpSession session,
//                             Principal principal,
//                             Model model) {
//        try {
//            Order order = orderService.checkoutCart(fullName, phone, address, note, paymentMethod, session, principal);
//            return "redirect:/orders/" + order.getId();
//        } catch (Exception e) {
//            model.addAttribute("error", e.getMessage());
//            model.addAttribute("paymentMethods", PaymentMethod.values());
//            return "order/checkout";
//        }
//    }
    @PostMapping("/checkout")
    public String placeOrder(@RequestParam String fullName,
                             @RequestParam String phone,
                             @RequestParam String address,
                             @RequestParam(required = false) String note,
                             @RequestParam PaymentMethod paymentMethod,
                             HttpSession session,
                             Principal principal,
                             Model model) {
        try {
            // Lấy user từ principal (ví dụ bạn dùng Spring Security)
            User user = userService.findByUsername(principal.getName());
//                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            if( user == null){
                throw  new UsernameNotFoundException("User Not Found");
            }
            Order order = orderService.checkoutCart(user, fullName, phone, note, address, paymentMethod, session);

            return "orders/success";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("paymentMethods", PaymentMethod.values());
            return "cart/checkout";
        }
    }


    /**
     * Xem chi tiết đơn hàng
     */
    @GetMapping("/{orderId}")
    public String orderDetail(@PathVariable String orderId,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = userService.findByUsername(userDetails.getUsername());
        Order order = orderService.findByIdAndUserOrThrow(orderId, user);

        model.addAttribute("order", order);

        return "cart/view";
    }

    /**
     * Xem danh sách đơn của user
     */
    @GetMapping
    public String listOrders(@AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = userService.findByUsername(userDetails.getUsername());
        model.addAttribute("orders", orderService.getOrdersByUser(user));
        return "cart/orderHistory";
    }

    /**
     * Cancel an order
     */
    @PostMapping("/{orderId}/cancel")
    public String cancelOrder(@PathVariable String orderId,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            User user = userService.findByUsername(userDetails.getUsername());
            orderService.cancelOrder(orderId, user);
            redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã được hủy thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể hủy đơn hàng: " + e.getMessage());
        }

        return "redirect:/orders";
    }

    /**
     * Admin endpoint to update order status
     */
    @PostMapping("/{orderId}/status")
    public String updateOrderStatus(@PathVariable String orderId,
                                    @RequestParam OrderStatus status,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        // Check if user is admin (you should implement proper role checking)
        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            orderService.updateOrderStatus(orderId, status);
            redirectAttributes.addFlashAttribute("successMessage", "Trạng thái đơn hàng đã được cập nhật.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật trạng thái đơn hàng: " + e.getMessage());
        }

        return "redirect:/admin/orders";
    }

    /**
     * Admin endpoint to set tracking number
     */
    @PostMapping("/{orderId}/tracking")
    public String setTrackingNumber(@PathVariable String orderId,
                                    @RequestParam String trackingNumber,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        // Check if user is admin (you should implement proper role checking)
        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            orderService.setTrackingNumber(orderId, trackingNumber);
            redirectAttributes.addFlashAttribute("successMessage", "Mã vận đơn đã được cập nhật.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật mã vận đơn: " + e.getMessage());
        }

        return "redirect:/admin/orders";
    }

    /**
     * Admin endpoint to set payment transaction ID
     */
    @PostMapping("/{orderId}/payment")
    public String setPaymentTransactionId(@PathVariable String orderId,
                                          @RequestParam String transactionId,
                                          @AuthenticationPrincipal UserDetails userDetails,
                                          RedirectAttributes redirectAttributes) {
        // Check if user is admin (you should implement proper role checking)
        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            orderService.setPaymentTransactionId(orderId, transactionId);
            redirectAttributes.addFlashAttribute("successMessage", "Mã giao dịch thanh toán đã được cập nhật.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật mã giao dịch: " + e.getMessage());
        }

        return "redirect:/admin/orders";
    }
}
