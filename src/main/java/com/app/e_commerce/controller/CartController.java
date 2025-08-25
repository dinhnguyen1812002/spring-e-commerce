package com.app.e_commerce.controller;

import com.app.e_commerce.entity.Cart;
import com.app.e_commerce.entity.Coupon;
import com.app.e_commerce.entity.Product;
import com.app.e_commerce.entity.User;
import com.app.e_commerce.exception.ProductNotFoundException;
import com.app.e_commerce.services.CartService;
import com.app.e_commerce.services.CouponService;
import com.app.e_commerce.services.ProductService;
import com.app.e_commerce.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;
    @Autowired
    private ProductService productService;
    @Autowired
    private UserService userService;
    @Autowired
    private CouponService couponService;

    /**
     * Hiển thị giỏ hàng
     */


    @GetMapping
    public String viewCart(@AuthenticationPrincipal UserDetails userDetails,
                           HttpSession session,
                           Model model) {
        User user = (userDetails != null)
                ? userService.findByUsername(userDetails.getUsername())
                : null;

        Cart cart = cartService.getOrCreateCart(user, session);

        // Tính tổng tiền từ cartItems
        BigDecimal total = cart.getCartItems()
                .stream()
                .map(item -> item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("cart", cart);
        model.addAttribute("total", total);

        return "cart/shopping-cart";
    }

    /**
     * Thêm sản phẩm vào giỏ
     */
    @PostMapping("/add")
    public String addToCart(@RequestParam("productId") Long productId,
                            @RequestParam("quantity") int quantity,
                            @AuthenticationPrincipal UserDetails userDetails,
                            HttpSession session) {
        User user = (userDetails != null)
                ? userService.findByUsername(userDetails.getUsername())
                : null;

        Product product = productService.findById(productId);
        if( product == null){
            throw new ProductNotFoundException("Đéo thấy product??");
        }
        cartService.addItemToCart(product, quantity, user, session);
        return "redirect:/cart";
    }

    /**
     * Cập nhật số lượng sản phẩm
     */
    @PostMapping("/update")
    public String updateCartItem(@RequestParam("productId") Long productId,
                                 @RequestParam("quantity") int quantity,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 HttpSession session) {
        User user = (userDetails != null)
                ? userService.findByUsername(userDetails.getUsername())
                : null;

        cartService.updateCartItem(productId, quantity, user, session);
        return "redirect:/cart";
    }

    /**
     * Xoá sản phẩm khỏi giỏ
     */
    @PostMapping("/remove")
    public String removeFromCart(@RequestParam("productId") Long productId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 HttpSession session) {
        User user = (userDetails != null)
                ? userService.findByUsername(userDetails.getUsername())
                : null;

        cartService.removeItemFromCart(productId, user, session);
        return "redirect:/cart";
    }

    /**
     * Xoá sạch giỏ hàng
     */
    @PostMapping("/clear")
    public String clearCart(@AuthenticationPrincipal UserDetails userDetails,
                            HttpSession session) {
        User user = (userDetails != null)
                ? userService.findByUsername(userDetails.getUsername())
                : null;

        Cart cart = cartService.getOrCreateCart(user, session);
        cartService.clearCart(session, cart);
        return "redirect:/cart";
    }

    /**
     * Apply a coupon to the cart
     */
    @PostMapping("/apply-coupon")
    public String applyCoupon(@RequestParam("couponCode") String couponCode,
                              @AuthenticationPrincipal UserDetails userDetails,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User user = (userDetails != null)
                ? userService.findByUsername(userDetails.getUsername())
                : null;

        boolean applied = cartService.applyCoupon(couponCode, user, session);

        if (applied) {
            redirectAttributes.addFlashAttribute("successMessage", "Mã giảm giá đã được áp dụng thành công!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Mã giảm giá không hợp lệ hoặc không áp dụng được cho đơn hàng này.");
        }

        return "redirect:/cart";
    }

    /**
     * Remove a coupon from the cart
     */
    @PostMapping("/remove-coupon")
    public String removeCoupon(@AuthenticationPrincipal UserDetails userDetails,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User user = (userDetails != null)
                ? userService.findByUsername(userDetails.getUsername())
                : null;

        cartService.removeCoupon(user, session);
        redirectAttributes.addFlashAttribute("successMessage", "Mã giảm giá đã được xóa khỏi giỏ hàng.");

        return "redirect:/cart";
    }

    /**
     * Get available coupons (AJAX)
     */
    @GetMapping("/available-coupons")
    @ResponseBody
    public ResponseEntity<?> getAvailableCoupons() {
        return ResponseEntity.ok(couponService.getAllValidCoupons());
    }
}
