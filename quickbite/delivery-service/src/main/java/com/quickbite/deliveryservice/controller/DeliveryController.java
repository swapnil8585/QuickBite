package com.quickbite.deliveryservice.controller;

import com.quickbite.deliveryservice.entity.Delivery;
import com.quickbite.deliveryservice.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Delivery> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(deliveryService.findByOrderId(orderId));
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<Delivery> assignDriver(
            @PathVariable Long id, @RequestParam Long driverId) {
        return ResponseEntity.ok(deliveryService.assignDriver(id, driverId));
    }

    @PutMapping("/driver/{driverId}/location")
    public ResponseEntity<Void> updateLocation(
            @PathVariable Long driverId,
            @RequestBody Map<String, String> body) {
        deliveryService.updateDriverLocation(driverId, body.get("latLng"));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/driver/{driverId}/location")
    public ResponseEntity<Map<String, String>> getLocation(@PathVariable Long driverId) {
        return ResponseEntity.ok(Map.of("location", deliveryService.getDriverLocation(driverId)));
    }

    @PatchMapping("/{id}/delivered")
    public ResponseEntity<Delivery> markDelivered(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.markDelivered(id));
    }

    @GetMapping("/driver/{driverId}/history")
    public ResponseEntity<List<Delivery>> driverHistory(@PathVariable Long driverId) {
        return ResponseEntity.ok(deliveryService.getDriverHistory(driverId));
    }
}