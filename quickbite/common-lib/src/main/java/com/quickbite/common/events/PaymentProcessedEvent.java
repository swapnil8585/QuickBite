package com.quickbite.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessedEvent {
    private String eventId = UUID.randomUUID().toString();
    private LocalDateTime occurredAt = LocalDateTime.now();
    private String eventType = "PAYMENT_PROCESSED";
    private Long orderId;
    private Long customerId;
    private BigDecimal amount;
    private String status; // SUCCESS | FAILED
    private String transactionId;
}
