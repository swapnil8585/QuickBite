package com.quickbite.orderservice.service;

import com.quickbite.orderservice.dto.*;
import com.quickbite.orderservice.entity.Order;
import com.quickbite.orderservice.entity.OrderItem;
import com.quickbite.orderservice.event.OrderEvent;
import com.quickbite.orderservice.kafka.OrderEventProducer;
import com.quickbite.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer eventProducer;

    @Transactional
    public OrderResponse placeOrder(Long customerId, CreateOrderRequest request) {
        List<OrderItem> items = request.getItems().stream()
                .map(i -> OrderItem.builder()
                        .menuItemId(i.getMenuItemId())
                        .itemName(i.getItemName())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .build())
                .collect(Collectors.toList());

        BigDecimal total = items.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .customerId(customerId)
                .restaurantId(request.getRestaurantId())
                .status(Order.OrderStatus.PLACED)
                .items(items)
                .totalAmount(total)
                .deliveryAddress(request.getDeliveryAddress())
                .specialInstructions(request.getSpecialInstructions())
                .build();

        items.forEach(item -> item.setOrder(order));
        Order saved = orderRepository.save(order);

        eventProducer.publishOrderPlaced(toEvent(saved));
        return toResponse(saved);
    }

    @Cacheable(value = "orders", key = "#orderId")
    public OrderResponse getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        return toResponse(order);
    }

    public List<OrderResponse> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersByRestaurant(Long restaurantId) {
        return orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "orders", key = "#orderId")
    public OrderResponse updateStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        order.setStatus(Order.OrderStatus.valueOf(newStatus));
        Order saved = orderRepository.save(order);
        eventProducer.publishOrderUpdated(toEvent(saved));
        return toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "orders", key = "#orderId")
    public OrderResponse cancelOrder(Long orderId, Long requestingUserId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        if (order.getStatus() != Order.OrderStatus.PLACED) {
            throw new IllegalStateException("Only PLACED orders can be cancelled");
        }
        order.setStatus(Order.OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        eventProducer.publishOrderCancelled(toEvent(saved));
        return toResponse(saved);
    }

    private OrderEvent toEvent(Order order) {
        return OrderEvent.builder()
                .orderId(order.getId())
                .customerId(order.getCustomerId())
                .restaurantId(order.getRestaurantId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .deliveryAddress(order.getDeliveryAddress())
                .timestamp(LocalDateTime.now())
                .build();
    }

    private OrderResponse toResponse(Order order) {
        OrderResponse r = new OrderResponse();
        r.setId(order.getId());
        r.setCustomerId(order.getCustomerId());
        r.setRestaurantId(order.getRestaurantId());
        r.setStatus(order.getStatus().name());
        r.setTotalAmount(order.getTotalAmount());
        r.setDeliveryAddress(order.getDeliveryAddress());
        r.setSpecialInstructions(order.getSpecialInstructions());
        r.setCreatedAt(order.getCreatedAt());
        r.setUpdatedAt(order.getUpdatedAt());
        if (order.getItems() != null) {
            r.setItems(order.getItems().stream().map(i -> {
                OrderResponse.OrderItemDto dto = new OrderResponse.OrderItemDto();
                dto.setId(i.getId()); dto.setMenuItemId(i.getMenuItemId());
                dto.setItemName(i.getItemName()); dto.setQuantity(i.getQuantity());
                dto.setUnitPrice(i.getUnitPrice());
                return dto;
            }).collect(Collectors.toList()));
        }
        return r;
    }
}