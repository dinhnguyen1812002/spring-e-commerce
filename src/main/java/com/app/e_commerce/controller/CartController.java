package com.app.e_commerce.controller;

import com.app.e_commerce.Enum.PaymentMethod;
import com.app.e_commerce.entity.Cart;
import com.app.e_commerce.entity.Product;
import com.app.e_commerce.entity.User;
import com.app.e_commerce.exception.ProductNotFoundException;
import com.app.e_commerce.services.CartService;
import com.app.e_commerce.services.ProductService;
import com.app.e_commerce.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public String addToCart(@RequestParam("productId") Long productId,
                            @RequestParam("quantity") int quantity,
                            HttpSession session,
                            Principal principal) {
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Not found product: " + productId));

        User user = principal != null ? userService.findByUsername(principal.getName()) : null;
        cartService.addItemToCart(product, quantity, user, session);

        return "redirect:/cart";
    }

    @GetMapping
    public String viewCart(Model model, HttpSession session, Principal principal) {
        User user = principal != null ? userService.findByUsername(principal.getName()) : null;
        Cart cart = cartService.getOrCreateCart(user, session);
        model.addAttribute("cart", cart);
        return "cart/view";
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam("productId") Long productId, HttpSession session, Principal principal) {
        User user = principal != null ? userService.findByUsername(principal.getName()) : null;
        cartService.removeItemFromCart(productId, user, session);
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateCartItem(@RequestParam("productId") Long productId,
                                 @RequestParam("quantity") int quantity,
                                 HttpSession session,
                                 Principal principal) {
        User user = principal != null ? userService.findByUsername(principal.getName()) : null;
        cartService.updateCartItem(productId, quantity, user, session);
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(@RequestParam("fullName") String fullName,
                           @RequestParam("phone") String phone,
                           @RequestParam("address") String address,
                           @RequestParam(value = "note", required = false) String note,
                           @RequestParam("paymentMethod") PaymentMethod paymentMethod,
                           HttpSession session,
                           Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        cartService.checkoutCart(fullName, phone, address, note, paymentMethod, session, principal);
        return "redirect:/orders/success";
    }

//    @PostMapping("/cart/checkout")
//    public String checkout(@ModelAttribute CheckoutRequestDTO checkoutRequest, Model model, Principal principal) {
//        // Lấy thông tin user hiện tại (nếu có)
//        User user = userService.findByUsername(principal.getName());
//
//        // Lưu thông tin đơn hàng và khách hàng
//        Order order = orderService.createOrderFromCheckout(checkoutRequest, user);
//
//        // Truyền thông tin đơn hàng ra view xác nhận
//        model.addAttribute("order", order);
//        return "cart/order-confirmation"; // Tạo trang xác nhận đơn hàng
//    }
}