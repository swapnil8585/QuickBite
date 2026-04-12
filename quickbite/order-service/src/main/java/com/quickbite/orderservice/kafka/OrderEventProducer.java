package com.quickbite.orderservice.kafka;

import com.quickbite.orderservice.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public static final String TOPIC_ORDER_PLACED   = "order-placed";
    public static final String TOPIC_ORDER_UPDATED  = "order-updated";
    public static final String TOPIC_ORDER_CANCELLED = "order-cancelled";

    public void publishOrderPlaced(OrderEvent event) {
        log.info("Publishing order-placed event for orderId={}", event.getOrderId());
        kafkaTemplate.send(TOPIC_ORDER_PLACED, String.valueOf(event.getOrderId()), event);
    }

    public void publishOrderUpdated(OrderEvent event) {
        log.info("Publishing order-updated event for orderId={} status={}", event.getOrderId(), event.getStatus());
        kafkaTemplate.send(TOPIC_ORDER_UPDATED, String.valueOf(event.getOrderId()), event);
    }

    public void publishOrderCancelled(OrderEvent event) {
        log.info("Publishing order-cancelled event for orderId={}", event.getOrderId());
        kafkaTemplate.send(TOPIC_ORDER_CANCELLED, String.valueOf(event.getOrderId()), event);
    }
}