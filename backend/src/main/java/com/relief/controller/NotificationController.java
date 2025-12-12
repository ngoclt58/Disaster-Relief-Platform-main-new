package com.relief.controller;

import com.relief.entity.InAppNotification;
import com.relief.entity.User;
import com.relief.repository.InAppNotificationRepository;
import com.relief.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "In-app notifications")
public class NotificationController {

    private final InAppNotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "List my notifications")
    public ResponseEntity<Page<InAppNotification>> list(
            @AuthenticationPrincipal UserDetails principal,
            Integer page,
            Integer size
    ) {
        int p = page != null && page >= 0 ? page : 0;
        int s = size != null && size > 0 && size <= 100 ? size : 20;
        User user = userRepository.findByEmail(principal.getUsername()).orElseGet(() ->
                userRepository.findByPhone(principal.getUsername()).orElseThrow());
        Page<InAppNotification> result = notificationRepository.findByUserId(user.getId(), PageRequest.of(p, s));
        return ResponseEntity.ok(result);
    }
}



