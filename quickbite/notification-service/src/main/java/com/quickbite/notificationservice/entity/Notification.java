package com.quickbite.notificationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String type;       // ORDER_PLACED, ORDER_UPDATED, PAYMENT_SUCCESS, etc.

    @Column(columnDefinition = "TEXT")
    private String message;

    private String channel;    // EMAIL, PUSH, SMS

    private boolean sent = false;

    @CreationTimestamp
    private LocalDateTime createdAt;
}