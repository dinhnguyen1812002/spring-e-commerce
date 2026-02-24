package com.app.e_commerce.controller;

import com.app.e_commerce.Enum.OrderStatus;
import com.app.e_commerce.entity.Order;
import com.app.e_commerce.entity.User;

import com.app.e_commerce.repository.UserRepo;
import com.app.e_commerce.services.OrderService;
import com.app.e_commerce.services.UserServiceOptimize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/users")
public class UserController {

    // @Autowired
    // private UserService userService;
    @Autowired
    private UserServiceOptimize userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepo userRepo;

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/auth/login";
        }
        User user = userService.findByUsername(principal.getName());
        model.addAttribute("user", user);

        // Fetch some stats for the dashboard
        List<Order> orders = orderService.getOrdersByUser(user);
        model.addAttribute("orders", orders);
        model.addAttribute("totalOrders", orders.size());

        // Total spent
        BigDecimal totalSpent = orders.stream()
                .filter(o -> o.getOrderStatus() == OrderStatus.SUCCESS || o.getOrderStatus() == OrderStatus.DELIVERED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("totalSpent", totalSpent);

        // Pending orders
        long pendingOrders = orders.stream()
                .filter(o -> o.getOrderStatus() == OrderStatus.PENDING || o.getOrderStatus() == OrderStatus.PROCESSING)
                .count();
        model.addAttribute("pendingOrders", pendingOrders);

        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("pageDescription", "Tổng quan hoạt động của bạn");

        return "users/dashboard";
    }

    @GetMapping("/edit/{userId}")
    public String showEditUserForm(@PathVariable UUID userId, Model model, Principal pro) {
        if (pro == null) {
            return "redirect:/auth/login";
        }
        User user = userService.getUserById(userId);
        model.addAttribute("user", user);
        return "users/user-profile"; // Points to user-edit.html Thymeleaf template
    }

    @PostMapping("/edit/{userId}")
    public String updateUser(@PathVariable UUID userId,
            @ModelAttribute User user,
            @RequestParam("avatarFile") MultipartFile avatarFile,
            RedirectAttributes redirectAttributes,
            Principal principal) {

        if (principal == null)
            return "redirect:/auth/login";
        // Update the user's information and avatar
        userService.updateUser(userId, user, avatarFile);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");

        // Reload the same profile page with updated information
        return "users/user-profile";
    }

    @GetMapping("/order-history/{orderId}")
    public String orderDetail(@PathVariable String orderId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = userService.findByUsername(userDetails.getUsername());
        Order order = orderService.findByIdAndUserOrThrow(orderId, user);

        model.addAttribute("order", order);

        return "users/order-details";
    }

    /**
     * Serves the user's avatar as a binary image response.
     * Avoids embedding huge base64 data URIs directly in HTML.
     */
    @GetMapping("/{userId}/avatar")
    @ResponseBody
    public ResponseEntity<byte[]> getAvatar(@PathVariable UUID userId) {
        User user = userService.getUserById(userId);
        String avatar = user.getAvatar();

        if (avatar == null || avatar.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Strip the data URI prefix if present, e.g. "data:image/jpeg;base64,..."
        String base64Data = avatar.contains(",") ? avatar.split(",", 2)[1] : avatar;
        byte[] imageBytes = Base64.getDecoder().decode(base64Data);

        // Detect content type from data URI prefix; default to JPEG
        MediaType mediaType = MediaType.IMAGE_JPEG;
        if (avatar.startsWith("data:image/png")) {
            mediaType = MediaType.IMAGE_PNG;
        } else if (avatar.startsWith("data:image/gif")) {
            mediaType = MediaType.IMAGE_GIF;
        } else if (avatar.startsWith("data:image/webp")) {
            mediaType = MediaType.parseMediaType("image/webp");
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CACHE_CONTROL, "max-age=86400, public")
                .body(imageBytes);
    }

}
