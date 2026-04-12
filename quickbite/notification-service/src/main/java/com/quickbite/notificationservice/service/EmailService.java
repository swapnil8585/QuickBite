package com.quickbite.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendSimpleNotification(Long userId, String subject, String message) {
        try {
            // In a real app, look up the user's email from User Service or a cache
            // For demo, log the notification
            log.info("EMAIL to user={} | subject={} | body={}", userId, subject, message);

            // Uncomment to send real email:
            // SimpleMailMessage mail = new SimpleMailMessage();
            // mail.setTo(userEmail);
            // mail.setSubject("[QuickBite] " + subject);
            // mail.setText(message);
            // mailSender.send(mail);
        } catch (Exception e) {
            log.error("Failed to send email to userId={}: {}", userId, e.getMessage());
        }
    }
}