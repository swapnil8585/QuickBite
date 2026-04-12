package com.quickbite.orderservice.event;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderEvent {
    private Long orderId;
    private Long customerId;
    private Long restaurantId;
    private String status;
    private BigDecimal totalAmount;
    private String deliveryAddress;
    private String customerEmail;
    private LocalDateTime timestamp;
}