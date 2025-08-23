package com.app.e_commerce.services;

import com.app.e_commerce.Enum.OrderStatus;
import com.app.e_commerce.Enum.PaymentMethod;
import com.app.e_commerce.entity.*;
import com.app.e_commerce.exception.ProductNotFoundException;
import com.app.e_commerce.repository.CartRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductService productService;

//    @Autowired
//    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private CouponService couponService;

    // Nếu bạn cần gửi mail, bật lại MailService
    // @Autowired
    // private MailService mailService;

    private static final String GUEST_CART_ID = "guestCartId";

    /**
     * Lấy giỏ hàng cho user đăng nhập hoặc giỏ khách trong session.
     * Luôn trả về một Cart tồn tại trong DB.
     */
    @Transactional
    public Cart getOrCreateCart(User user, HttpSession session) {
        if (user != null) {
            // Authenticated user
            return cartRepository.findByUserAndIsGuestCartFalse(user)
                    .orElseGet(() -> {
                        Cart cart = new Cart();
                        cart.setUser(user);
                        cart.setGuestCart(false);
                        cart.setTotalPrice(java.math.BigDecimal.ZERO);
                        return cartRepository.save(cart);
                    });
        } else {
            // Guest user
            UUID guestCartId = (UUID) session.getAttribute(GUEST_CART_ID);
            if (guestCartId != null) {
                Optional<Cart> found = cartRepository.findByIdAndIsGuestCartTrue(guestCartId);
                if (found.isPresent()) {
                    return found.get();
                }
            }
            return createGuestCart(session);
        }
    }

    /**
     * Tạo giỏ hàng khách và lưu vào session.
     */
    @Transactional
    public Cart createGuestCart(HttpSession session) {
        Cart cart = new Cart();
        cart.setGuestCart(true);
        cart.setTotalPrice(java.math.BigDecimal.ZERO);
        Cart saved = cartRepository.save(cart);
        session.setAttribute(GUEST_CART_ID, saved.getId()); // id kiểu UUID theo method repo của bạn
        return saved;
    }

    /**
     * Merge guest cart vào user cart khi user đăng nhập.
     */
    @Transactional
    public void mergeGuestCartToUserCart(User user, HttpSession session) {
        UUID guestCartId = (UUID) session.getAttribute(GUEST_CART_ID);
        if (guestCartId != null) {
            cartRepository.findByIdAndIsGuestCartTrue(guestCartId).ifPresent(guestCart -> {
                Cart userCart = getOrCreateCart(user, session);
                // Giả định Cart đã có hàm mergeCart(guestCart)
                userCart.mergeCart(guestCart);
                userCart.updateTotalPrice();
                cartRepository.save(userCart);
                cartRepository.delete(guestCart);
                session.removeAttribute(GUEST_CART_ID);
            });
        }
    }

    /**
     * Thêm item vào cart. Kiểm tra số lượng hợp lệ (>0).
     */
    @Transactional
    public void addItemToCart(Product product, int quantity, User user, HttpSession session) {
        if (product == null || product.getId() == null) {
            throw new ProductNotFoundException("Sản phẩm không tồn tại");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải > 0");
        }

        Cart cart = getOrCreateCart(user, session);

        CartItem existingItem = cart.getCartItems().stream()
                .filter(item -> Objects.equals(item.getProduct().getId(), product.getId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            long newQty = (long) existingItem.getQuantity() + quantity;
            if (newQty > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Số lượng quá lớn");
            }
            existingItem.setQuantity((int) newQty);
        } else {
            CartItem cartItem = new CartItem(product, cart, quantity);
            cart.addCartItem(cartItem);
        }

        cart.updateTotalPrice();
        cartRepository.save(cart);
    }

    @Transactional
    public void removeItemFromCart(Long productId, User user, HttpSession session) {
        Cart cart = getOrCreateCart(user, session);
        cart.getCartItems().removeIf(item -> Objects.equals(item.getProduct().getId(), productId));
        cart.updateTotalPrice();
        cartRepository.save(cart);
    }

    @Transactional
    public void updateCartItem(Long productId, int quantity, User user, HttpSession session) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Số lượng không hợp lệ");
        }
        Cart cart = getOrCreateCart(user, session);
        cart.getCartItems().forEach(item -> {
            if (Objects.equals(item.getProduct().getId(), productId)) {
                if (quantity == 0) {
                    // Xoá nếu = 0
                    item.setQuantity(0);
                } else {
                    item.setQuantity(quantity);
                }
            }
        });
        // Dọn item quantity = 0
        cart.getCartItems().removeIf(ci -> ci.getQuantity() <= 0);
        cart.updateTotalPrice();
        cartRepository.save(cart);
    }

    /**
     * Tiến hành checkout: tạo Order từ Cart hiện tại.
     * - Validate người dùng
     * - Validate giỏ hàng
     * - Ủy quyền cho OrderService tạo order + trừ tồn
     */
//    @Transactional(rollbackFor = Exception.class)
//    public Order checkoutCart(String fullName,
//                              String phone,
//                              String address,
//                              String note,
//                              PaymentMethod paymentMethod,
//                              HttpSession session,
//                              Principal principal) {
//        if (principal == null || principal.getName() == null) {
//            throw new IllegalStateException("Bạn cần đăng nhập để thanh toán");
//        }
//        String username = principal.getName();
//        User user = userService.findByUsername(username);
//        if (user == null) {
//            throw new IllegalStateException("User không tồn tại");
//        }
//
//        Cart cart = getOrCreateCart(user, session);
//        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
//            throw new IllegalStateException("Giỏ hàng trống");
//        }
//
//        // Tạo Order skeleton
//        Order order = new Order();
//        order.setUser(user);
//        order.setName(fullName);
//        order.setPhoneNumber(phone);
//        order.setShippingAddress(address);
//        order.setNote(note);
//        order.setPaymentMethod(paymentMethod);
//        order.setTotalAmount(cart.getTotalPrice());
//        order.setOrderDate(LocalDateTime.now());
//        order.setOrderStatus(OrderStatus.PENDING);
//
//        // Ủy quyền tạo order (kèm trừ tồn + tạo order items)
//        Order created = orderService.createOrder(order, cart);
//
//        // Xoá sạch cart
//        clearCart(session, cart);
//
//        return created;
//    }

    /**
     * Xoá sạch cart hiện hành (vừa user vừa guest) và cập nhật session.
     */
    @Transactional
    public void clearCart(HttpSession session) {
        UUID guestCartId = (UUID) session.getAttribute(GUEST_CART_ID);
        if (guestCartId != null) {
            cartRepository.findByIdAndIsGuestCartTrue(guestCartId)
                    .ifPresent(cartRepository::delete);
            session.removeAttribute(GUEST_CART_ID);
        }
        // Nếu là user login thì chỉ cần xóa items trong giỏ
        // (user luôn giữ 1 cart riêng, không xóa cả cart)
    }


    @Transactional
    public void clearCart(HttpSession session, Cart cart) {
        if (cart == null) return;
        clearAndSave(cart);
        if (Boolean.TRUE.equals(cart.isGuestCart())) {
            session.removeAttribute(GUEST_CART_ID);
        }
    }

    private void clearAndSave(Cart cart) {
        cart.getCartItems().clear();
        cart.updateTotalPrice();
        cartRepository.save(cart);
    }

    /**
     * Helper thêm trực tiếp sản phẩm vào cart theo id (hay dùng trong controller).
     */
    @Transactional
    public void addItemToCartByProductId(Long productId, int quantity, User user, HttpSession session) {
        Product product = productService.findById(productId);
//                .o(() -> new ProductNotFoundException("Không tìm thấy sản phẩm: " + productId));
        if(product == null) {
            throw new ProductNotFoundException("Not found product with id: " +productId);
        }
        addItemToCart(product, quantity, user, session);
    }

    /**
     * Apply a coupon to a cart
     * @param couponCode The coupon code
     * @param user The user (can be null for guest)
     * @param session The HTTP session
     * @return true if coupon was applied, false otherwise
     */
    @Transactional
    public boolean applyCoupon(String couponCode, User user, HttpSession session) {
        if (couponCode == null || couponCode.trim().isEmpty()) {
            return false;
        }

        Cart cart = getOrCreateCart(user, session);

        // Validate the coupon
        Optional<Coupon> couponOpt = couponService.validateCoupon(couponCode, cart.getSubtotal());

        if (couponOpt.isPresent()) {
            Coupon coupon = couponOpt.get();

            // Apply the coupon to the cart
            cart.applyCoupon(coupon.getCode(), coupon.getDiscountAmount());
            cartRepository.save(cart);

            return true;
        }

        return false;
    }

    /**
     * Remove a coupon from a cart
     * @param user The user (can be null for guest)
     * @param session The HTTP session
     */
    @Transactional
    public void removeCoupon(User user, HttpSession session) {
        Cart cart = getOrCreateCart(user, session);

        if (cart.hasCoupon()) {
            cart.removeCoupon();
            cartRepository.save(cart);
        }
    }

    /**
     * Set custom shipping cost for a cart
     * @param shippingCost The shipping cost
     * @param user The user (can be null for guest)
     * @param session The HTTP session
     */
    @Transactional
    public void setShippingCost(BigDecimal shippingCost, User user, HttpSession session) {
        if (shippingCost == null || shippingCost.compareTo(BigDecimal.ZERO) < 0) {
            return;
        }

        Cart cart = getOrCreateCart(user, session);
        cart.setShippingCost(shippingCost);
        cartRepository.save(cart);
    }
}
