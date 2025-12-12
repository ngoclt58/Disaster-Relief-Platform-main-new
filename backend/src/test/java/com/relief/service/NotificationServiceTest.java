package com.relief.service;

import com.relief.entity.NeedsRequest;
import com.relief.entity.User;
import com.relief.repository.InAppNotificationRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import static org.mockito.Mockito.*;

public class NotificationServiceTest {

    @Test
    void sendsInAppAndPersists() {
        InAppNotificationRepository repo = mock(InAppNotificationRepository.class);
        NotificationService.EmailProvider email = (u, m, r) -> {};
        NotificationService.SmsProvider sms = (u, m, r) -> {};
        NotificationService.PushProvider push = (u, m, r) -> {};
        NotificationService service = new NotificationService(repo, email, sms, push, new SimpleMeterRegistry());

        User user = User.builder().email("a@example.com").phone("123").build();
        service.sendNotification(user, "hello", "IN_APP", (NeedsRequest) null);

        verify(repo, times(1)).save(ArgumentMatchers.any());
    }
}



