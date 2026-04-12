package com.quickbite.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdatedEvent {
    private String eventId = UUID.randomUUID().toString();
    private LocalDateTime occurredAt = LocalDateTime.now();
    private String eventType = "ORDER_STATUS_UPDATED";
    private Long orderId;
    private Long customerId;
    private String previousStatus;
    private String newStatus;
    private String customerEmail;
}
