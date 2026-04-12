package com.quickbite.restaurantservice.controller;

import com.quickbite.restaurantservice.dto.*;
import com.quickbite.restaurantservice.entity.*;
import com.quickbite.restaurantservice.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @PostMapping
    public ResponseEntity<Restaurant> create(
            @RequestHeader("X-User-Id") Long ownerId,
            @Valid @RequestBody RestaurantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(restaurantService.createRestaurant(ownerId, request));
    }

    @GetMapping
    public ResponseEntity<List<Restaurant>> listOpen() {
        return ResponseEntity.ok(restaurantService.getOpenRestaurants());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Restaurant> getById(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getById(id));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Restaurant> toggleOpen(
            @PathVariable Long id, @RequestParam boolean open) {
        return ResponseEntity.ok(restaurantService.toggleOpen(id, open));
    }

    @PostMapping("/{id}/menu")
    public ResponseEntity<MenuItem> addMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(restaurantService.addMenuItem(id, request));
    }

    @GetMapping("/{id}/menu")
    public ResponseEntity<List<MenuItem>> getMenu(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getMenuItems(id));
    }

    @DeleteMapping("/{id}/menu/{itemId}")
    public ResponseEntity<Void> deleteMenuItem(
            @PathVariable Long id, @PathVariable Long itemId) {
        restaurantService.deleteMenuItem(id, itemId);
        return ResponseEntity.noContent().build();
    }
}