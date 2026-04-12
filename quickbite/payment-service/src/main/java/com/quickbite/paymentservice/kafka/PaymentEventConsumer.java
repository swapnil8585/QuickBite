package com.quickbite.paymentservice.kafka;

import com.quickbite.paymentservice.entity.Payment;
import com.quickbite.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "order-placed", groupId = "payment-service-group")
    public void onOrderPlaced(ConsumerRecord<String, Map<String, Object>> record) {
        log.info("Payment service: received order-placed event key={}", record.key());

        Map<String, Object> payload = record.value();
        Long orderId    = Long.valueOf(payload.get("orderId").toString());
        Long customerId = Long.valueOf(payload.get("customerId").toString());
        BigDecimal total = new BigDecimal(payload.get("totalAmount").toString());

        // Idempotency — skip if payment already exists for this order
        if (paymentRepository.findByOrderId(orderId).isPresent()) {
            log.warn("Payment already exists for orderId={}. Skipping.", orderId);
            return;
        }

        // Simulate payment processing (integrate Razorpay / Stripe here)
        Payment payment = Payment.builder()
                .orderId(orderId)
                .customerId(customerId)
                .amount(total)
                .status(Payment.PaymentStatus.SUCCESS)   // mock success
                .paymentMethod("UPI")
                .transactionId(UUID.randomUUID().toString())
                .processedAt(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);

        // Publish payment-success event so Order Service can confirm the order
        kafkaTemplate.send("payment-success", String.valueOf(orderId),
                Map.of("orderId", orderId, "customerId", customerId,
                        "transactionId", payment.getTransactionId()));

        log.info("Payment SUCCESS for orderId={}", orderId);
    }
}