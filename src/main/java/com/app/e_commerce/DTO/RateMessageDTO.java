package com.app.e_commerce.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RateMessageDTO {
    private Long id;
    private Long productId;
    private String username;
    private String comment;
    private Integer star;
    private String createdAt;
}