package com.quickbite.notificationservice.kafka;

import com.quickbite.notificationservice.entity.Notification;
import com.quickbite.notificationservice.repository.NotificationRepository;
import com.quickbite.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @KafkaListener(topics = "order-placed", groupId = "notification-service-group")
    public void onOrderPlaced(ConsumerRecord<String, Map<String, Object>> record) {
        Map<String, Object> payload = record.value();
        Long customerId = Long.valueOf(payload.get("customerId").toString());
        Long orderId    = Long.valueOf(payload.get("orderId").toString());

        String msg = "Your order #" + orderId + " has been placed successfully!";
        saveAndSend(customerId, "ORDER_PLACED", msg);
    }

    @KafkaListener(topics = "order-updated", groupId = "notification-service-group")
    public void onOrderUpdated(ConsumerRecord<String, Map<String, Object>> record) {
        Map<String, Object> payload = record.value();
        Long customerId = Long.valueOf(payload.get("customerId").toString());
        String status   = payload.get("status").toString();
        Long orderId    = Long.valueOf(payload.get("orderId").toString());

        String msg = "Order #" + orderId + " status updated to: " + status;
        saveAndSend(customerId, "ORDER_UPDATED", msg);
    }

    @KafkaListener(topics = "payment-success", groupId = "notification-service-group")
    public void onPaymentSuccess(ConsumerRecord<String, Map<String, Object>> record) {
        Map<String, Object> payload = record.value();
        Long customerId = Long.valueOf(payload.get("customerId").toString());
        Long orderId    = Long.valueOf(payload.get("orderId").toString());

        String msg = "Payment successful for order #" + orderId;
        saveAndSend(customerId, "PAYMENT_SUCCESS", msg);
    }

    private void saveAndSend(Long userId, String type, String message) {
        Notification n = Notification.builder()
                .userId(userId).type(type).message(message)
                .channel("EMAIL").sent(false)
                .build();
        notificationRepository.save(n);

        emailService.sendSimpleNotification(userId, type, message);
        log.info("Notification saved and sent: userId={} type={}", userId, type);
    }
}