package com.quickbite.restaurantservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RestaurantRequest {
    @NotBlank private String name;
    private String description;
    @NotBlank private String address;
    private String phone;
    private String cuisineType;
}