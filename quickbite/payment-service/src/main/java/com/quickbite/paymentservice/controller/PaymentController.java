package com.quickbite.paymentservice.controller;

import com.quickbite.paymentservice.entity.Payment;
import com.quickbite.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentRepository paymentRepository;

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Payment> getByOrder(@PathVariable Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/my")
    public ResponseEntity<List<Payment>> myPayments(@RequestHeader("X-User-Id") Long customerId) {
        return ResponseEntity.ok(paymentRepository.findByCustomerIdOrderByCreatedAtDesc(customerId));
    }
}