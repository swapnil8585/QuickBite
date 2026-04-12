package com.quickbite.deliveryservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Delivery {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long orderId;

    private Long driverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    private String deliveryAddress;
    private String currentLocation;   // lat,lng string — updated frequently via Redis, persisted on completion

    @CreationTimestamp
    private LocalDateTime assignedAt;

    private LocalDateTime deliveredAt;

    public enum DeliveryStatus { PENDING, DRIVER_ASSIGNED, PICKED_UP, DELIVERED, FAILED }
}