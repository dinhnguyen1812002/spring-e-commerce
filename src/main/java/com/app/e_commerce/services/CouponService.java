package com.app.e_commerce.services;

import com.app.e_commerce.entity.Cart;
import com.app.e_commerce.entity.Coupon;
import com.app.e_commerce.exception.ResourceNotFoundException;
import com.app.e_commerce.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;

    /**
     * Find a coupon by its code
     * @param code The coupon code
     * @return Optional containing the coupon if found
     */
    public Optional<Coupon> findByCode(String code) {
        return couponRepository.findByCode(code);
    }

    /**
     * Validate a coupon for a given purchase amount
     * @param code The coupon code
     * @param purchaseAmount The total purchase amount
     * @return The coupon if valid, otherwise empty
     */
    public Optional<Coupon> validateCoupon(String code, BigDecimal purchaseAmount) {
        Optional<Coupon> couponOpt = findByCode(code);
        
        if (couponOpt.isPresent()) {
            Coupon coupon = couponOpt.get();
            if (coupon.isValid(purchaseAmount)) {
                return couponOpt;
            }
        }
        
        return Optional.empty();
    }

    /**
     * Apply a coupon (increment usage count)
     * @param code The coupon code
     * @return true if the coupon was applied, false otherwise
     */
    @Transactional
    public boolean applyCoupon(String code) {
        Optional<Coupon> couponOpt = findByCode(code);
        
        if (couponOpt.isPresent()) {
            Coupon coupon = couponOpt.get();
            coupon.incrementUsageCount();
            couponRepository.save(coupon);
            return true;
        }
        
        return false;
    }

    /**
     * Create a new coupon
     * @param coupon The coupon to create
     * @return The created coupon
     */
    @Transactional
    public Coupon createCoupon(Coupon coupon) {
        return couponRepository.save(coupon);
    }

    /**
     * Update an existing coupon
     * @param coupon The coupon to update
     * @return The updated coupon
     */
    @Transactional
    public Coupon updateCoupon(Coupon coupon) {
        return couponRepository.save(coupon);
    }

    /**
     * Delete a coupon
     * @param id The coupon ID
     */
    @Transactional
    public void deleteCoupon(Long id) {
        couponRepository.deleteById(id);
    }

    /**
     * Get all active coupons
     * @return List of active coupons
     */
    public List<Coupon> getAllActiveCoupons() {
        return couponRepository.findByIsActiveTrue();
    }

    /**
     * Get all valid coupons at the current time
     * @return List of valid coupons
     */
    public List<Coupon> getAllValidCoupons() {
        LocalDateTime now = LocalDateTime.now();
        return couponRepository.findByIsActiveTrueAndValidFromBeforeAndValidToAfter(now, now);
    }

    /**
     * Deactivate expired coupons
     * @return Number of coupons deactivated
     */
    @Transactional
    public int deactivateExpiredCoupons() {
        LocalDateTime now = LocalDateTime.now();
        List<Coupon> expiredCoupons = couponRepository.findByValidToBefore(now);
        
        for (Coupon coupon : expiredCoupons) {
            if (coupon.isActive()) {
                coupon.setActive(false);
                couponRepository.save(coupon);
            }
        }
        
        return expiredCoupons.size();
    }
//    public boolean isValidCoupon(String couponCode, Cart cart) {
//        Coupon coupon = couponRepository.findByCode(couponCode)
//                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
//        return coupon.getExpiryDate().isAfter(LocalDateTime.now()) &&
//                cart.getSubtotal().compareTo(coupon.getMinimumAmount()) >= 0;
//    }
}