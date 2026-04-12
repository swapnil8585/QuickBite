package com.quickbite.orderservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequest {

    @NotNull
    private Long restaurantId;

    @NotEmpty
    private List<OrderItemRequest> items;

    @NotBlank
    private String deliveryAddress;

    private String specialInstructions;

    @Data
    public static class OrderItemRequest {
        @NotNull private Long menuItemId;
        @NotBlank private String itemName;
        @NotNull @Min(1) private Integer quantity;
        @NotNull @DecimalMin("0.0") private java.math.BigDecimal unitPrice;
    }
}