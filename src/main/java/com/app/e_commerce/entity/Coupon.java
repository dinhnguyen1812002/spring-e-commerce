package com.app.e_commerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal discountAmount;

    @Column(nullable = false)
    private BigDecimal minimumPurchaseAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDateTime validFrom;

    @Column(nullable = false)
    private LocalDateTime validTo;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false)
    private int usageLimit = 0; // 0 means unlimited

    @Column(nullable = false)
    private int usageCount = 0;

    /**
     * Check if the coupon is valid for use
     * @param purchaseAmount The total purchase amount
     * @return true if the coupon is valid, false otherwise
     */
    public boolean isValid(BigDecimal purchaseAmount) {
        LocalDateTime now = LocalDateTime.now();
        
        return isActive &&
               now.isAfter(validFrom) &&
               now.isBefore(validTo) &&
               (usageLimit == 0 || usageCount < usageLimit) &&
               purchaseAmount.compareTo(minimumPurchaseAmount) >= 0;
    }

    /**
     * Increment the usage count of this coupon
     */
    public void incrementUsageCount() {
        this.usageCount++;
        if (usageLimit > 0 && usageCount >= usageLimit) {
            this.isActive = false;
        }
    }
}