package com.app.e_commerce.controller;

import com.app.e_commerce.Enum.PaymentMethod;
import com.app.e_commerce.entity.Cart;
import com.app.e_commerce.entity.Product;
import com.app.e_commerce.entity.User;
import com.app.e_commerce.exception.ProductNotFoundException;
import com.app.e_commerce.services.CartService;
import com.app.e_commerce.services.OrderService;
import com.app.e_commerce.services.ProductService;
import com.app.e_commerce.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private ProductService productService; // Service to fetch products
    @Autowired
    private CartService cartService;       // Cart service to manage cart logic
    @Autowired
    private OrderService orderService;     // Order service to manage orders
    @Autowired
    private UserService userService;
    // Display cart items
    @PostMapping("/add")
    public String addToCart(@RequestParam("productId") Long productId,
                            @RequestParam("quantity") int quantity,
                            HttpSession session) {
        // Retrieve product from the database
        Product product = productService.getProductById(productId)
                .orElseThrow(()->new ProductNotFoundException("Not found product: "+productId));

        // Add product to the cart (stored in session)
        cartService.addItemToCart(product, quantity, session);

        // Redirect to the cart page or back to the product list
        return "redirect:/cart";  // After adding the product, you can redirect to the cart
    }

    // Display the cart items
    @GetMapping
    public String viewCart(Model model, HttpSession session) {
        Cart cart = cartService.getCartFromSession(session);
        model.addAttribute("cart", cart);
        return "cart/view";  // Thymeleaf template to display cart
    }

    // Remove item from cart
    @PostMapping("/remove")
    public String removeFromCart(@RequestParam("productId") Long productId, HttpSession session) {
        cartService.removeItemFromCart(productId, session);
        return "redirect:/cart";
    }
    // Update quantity of a cart item
//    @PostMapping("/update/{productId}")
//    public String updateCartItem(@PathVariable("productId") Long productId, @RequestParam("quantity") int quantity, HttpSession session) {
//        cartService.updateCartItem(productId, quantity, session);
//        return "redirect:/cart";
//    }
    @PostMapping("/update")
    public String updateCartItem(@RequestParam("productId") Long productId,
                                 @RequestParam("quantity") int quantity,
                                 HttpSession session) {
        cartService.updateCartItem(productId, quantity, session);
        return "redirect:/cart";  // Redirect back to the cart page to show updated quantity and total price
    }

    // Checkout the cart
    @PostMapping("/checkout")
    public String checkout(@RequestParam("address") String address,
                           @RequestParam("paymentMethod") PaymentMethod paymentMethod,
                           HttpSession session, Principal principal) {

        // Get the logged-in user's username from the Principal
        String username = principal.getName();

        // Retrieve the User entity from the database using the username
        User user = userService.findByUsername(username);
        if (user == null ||username ==null) {
            return "redirect:/login";  // Redirect to login if user is not authenticated
        }

        // Delegate the checkout process to the service layer
        cartService.checkoutCart(address, paymentMethod, session, principal);

        // Clear the cart from the session after checkout
        session.removeAttribute("cart");

        // Redirect to the orders page (order history or confirmation page)
        return "redirect:/";
    }
}
