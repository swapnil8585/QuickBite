package com.quickbite.deliveryservice.kafka;

import com.quickbite.deliveryservice.entity.Delivery;
import com.quickbite.deliveryservice.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryEventConsumer {

    private final DeliveryRepository deliveryRepository;

    // Listens to order-confirmed events published by Order Service
    @KafkaListener(topics = "order-updated", groupId = "delivery-service-group")
    public void onOrderUpdated(ConsumerRecord<String, Object> record) {
        log.info("Delivery service received event: {}", record.value());
        // In a real app, parse the payload and auto-create a Delivery record
        // when order status is CONFIRMED or READY_FOR_PICKUP
    }
}