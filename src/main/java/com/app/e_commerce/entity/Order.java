package com.app.e_commerce.entity;

import com.app.e_commerce.Enum.OrderStatus;
import com.app.e_commerce.Enum.PaymentMethod;
import de.huxhorn.sulky.ulid.ULID;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;


@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private Set<OrderItem> orderItems = new HashSet<>();

    private String name;

    @Size(min = 10, max = 10)
    private String phoneNumber;

    private String note;

    private BigDecimal totalAmount;

    @Column(nullable = false)
    private String shippingAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus; // Add order status

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Column(nullable = true)
    private LocalDateTime shippedDate;

    @Column(nullable = true)
    private LocalDateTime deliveredDate;

    @Column(nullable = true)
    private String trackingNumber;

    @Column(nullable = true)
    private String paymentTransactionId;

    @Column(nullable = false)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal shippingCost = BigDecimal.ZERO;

    @Column(nullable = true)
    private String couponCode;

    @Column(nullable = false)
    private BigDecimal discountAmount = BigDecimal.ZERO;

//    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
//    private List<OrderDetail> orderDetails;
    // Constructors, getters, setters, etc.

    @PrePersist
    public void prePersist() {
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }
        if (id == null) {
            ULID ulid= new ULID();
            this.id= ulid.nextULID();
        }
    }
}
