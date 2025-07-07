package com.app.e_commerce.DTO;

import com.app.e_commerce.Enum.PaymentMethod;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CheckoutRequestDTO {
    // Getters and Setters
    private String fullName;
    private String phone;
    private String address;
    private String note;
    private PaymentMethod paymentMethod;

}
