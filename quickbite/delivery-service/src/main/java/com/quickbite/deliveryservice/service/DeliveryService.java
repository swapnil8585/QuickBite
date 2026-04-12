package com.quickbite.deliveryservice.service;

import com.quickbite.deliveryservice.entity.Delivery;
import com.quickbite.deliveryservice.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    // Redis key pattern: driver:{driverId}:location  ->  "lat,lng"
    private static final String DRIVER_LOCATION_KEY = "driver:%d:location";

    @Transactional
    public Delivery createDelivery(Long orderId, String deliveryAddress) {
        Delivery delivery = Delivery.builder()
                .orderId(orderId)
                .deliveryAddress(deliveryAddress)
                .status(Delivery.DeliveryStatus.PENDING)
                .build();
        return deliveryRepository.save(delivery);
    }

    @Transactional
    public Delivery assignDriver(Long deliveryId, Long driverId) {
        Delivery delivery = findById(deliveryId);
        delivery.setDriverId(driverId);
        delivery.setStatus(Delivery.DeliveryStatus.DRIVER_ASSIGNED);
        return deliveryRepository.save(delivery);
    }

    /**
     * Called by driver app every few seconds.
     * Stores the location in Redis with a 5-minute TTL (fast writes, low DB load).
     */
    public void updateDriverLocation(Long driverId, String latLng) {
        String key = String.format(DRIVER_LOCATION_KEY, driverId);
        redisTemplate.opsForValue().set(key, latLng, Duration.ofMinutes(5));
        log.debug("Driver {} location updated to {}", driverId, latLng);
    }

    /** Returns live driver location from Redis. */
    public String getDriverLocation(Long driverId) {
        String key = String.format(DRIVER_LOCATION_KEY, driverId);
        Object location = redisTemplate.opsForValue().get(key);
        return location != null ? location.toString() : "location unavailable";
    }

    @Transactional
    public Delivery markDelivered(Long deliveryId) {
        Delivery delivery = findById(deliveryId);
        delivery.setStatus(Delivery.DeliveryStatus.DELIVERED);
        delivery.setDeliveredAt(LocalDateTime.now());
        return deliveryRepository.save(delivery);
    }

    public Delivery findByOrderId(Long orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Delivery not found for order: " + orderId));
    }

    public List<Delivery> getDriverHistory(Long driverId) {
        return deliveryRepository.findByDriverIdOrderByAssignedAtDesc(driverId);
    }

    private Delivery findById(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + id));
    }
}