package com.app.e_commerce.repository;

import com.app.e_commerce.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    
    /**
     * Find a coupon by its code
     * @param code The coupon code
     * @return Optional containing the coupon if found
     */
    Optional<Coupon> findByCode(String code);
    
    /**
     * Find all active coupons
     * @return List of active coupons
     */
    List<Coupon> findByIsActiveTrue();
    
    /**
     * Find all active coupons valid at the current time
     * @param now The current time
     * @return List of valid coupons
     */
    List<Coupon> findByIsActiveTrueAndValidFromBeforeAndValidToAfter(
            LocalDateTime now, LocalDateTime now2);
    
    /**
     * Find all coupons that have expired
     * @param now The current time
     * @return List of expired coupons
     */
    List<Coupon> findByValidToBefore(LocalDateTime now);
}