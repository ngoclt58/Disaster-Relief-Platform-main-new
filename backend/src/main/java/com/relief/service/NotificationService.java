package com.relief.service;

import com.relief.entity.InAppNotification;
import com.relief.entity.NeedsRequest;
import com.relief.entity.User;
import com.relief.repository.InAppNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Basic notification service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final InAppNotificationRepository notificationRepository;
    private final EmailProvider emailProvider;
    private final SmsProvider smsProvider;
    private final PushProvider pushProvider;
    private final MeterRegistry meterRegistry;

    /**
     * Send notification to user
     */
    public void sendNotification(User user, String message, NeedsRequest request) {
        sendNotification(user, message, "EMAIL", request);
    }

    /**
     * Send notification to user with specific channel
     */
    public void sendNotification(User user, String message, String channel, NeedsRequest request) {
        log.info("Sending {} notification to user {}: {}", channel, user.getEmail(), message);
        switch (channel.toUpperCase()) {
            case "EMAIL":
                sendEmailNotification(user, message, request);
                meterRegistry.counter("notifications.sent", "channel", "EMAIL").increment();
                break;
            case "PUSH":
                sendPushNotification(user, message, request);
                meterRegistry.counter("notifications.sent", "channel", "PUSH").increment();
                break;
            case "SMS":
                sendSmsNotification(user, message, request);
                meterRegistry.counter("notifications.sent", "channel", "SMS").increment();
                break;
            case "IN_APP":
                sendInAppNotification(user, message, request);
                meterRegistry.counter("notifications.sent", "channel", "IN_APP").increment();
                break;
            default:
                log.warn("Unknown notification channel: {}", channel);
        }
    }

    private void sendEmailNotification(User user, String message, NeedsRequest request) {
        emailProvider.sendEmail(user, message, request);
        persistInApp(user, message, request, "EMAIL");
    }

    private void sendPushNotification(User user, String message, NeedsRequest request) {
        pushProvider.sendPush(user, message, request);
        persistInApp(user, message, request, "PUSH");
    }

    private void sendSmsNotification(User user, String message, NeedsRequest request) {
        smsProvider.sendSms(user, message, request);
        persistInApp(user, message, request, "SMS");
    }

    private void sendInAppNotification(User user, String message, NeedsRequest request) {
        persistInApp(user, message, request, "IN_APP");
    }

    private void persistInApp(User user, String message, NeedsRequest request, String channel) {
        InAppNotification notification = InAppNotification.builder()
                .user(user)
                .needsRequest(request)
                .channel(channel)
                .message(message)
                .build();
        notificationRepository.save(notification);
    }

    public interface EmailProvider {
        void sendEmail(User user, String message, NeedsRequest request);
    }

    public interface SmsProvider {
        void sendSms(User user, String message, NeedsRequest request);
    }

    public interface PushProvider {
        void sendPush(User user, String message, NeedsRequest request);
    }
}

@Component
@RequiredArgsConstructor
@Slf4j
class LoggingEmailProvider implements NotificationService.EmailProvider {
    @Override
    public void sendEmail(User user, String message, NeedsRequest request) {
        log.info("EMAIL to {}: {}", user.getEmail(), message);
    }
}

@Component
@RequiredArgsConstructor
@Slf4j
class LoggingSmsProvider implements NotificationService.SmsProvider {
    @Override
    public void sendSms(User user, String message, NeedsRequest request) {
        log.info("SMS to {}: {}", user.getPhone(), message);
    }
}

@Component
@RequiredArgsConstructor
@Slf4j
class LoggingPushProvider implements NotificationService.PushProvider {
    @Override
    public void sendPush(User user, String message, NeedsRequest request) {
        log.info("PUSH to {}: {}", user.getEmail(), message);
    }
}

