package com.forknight.server.controller;

import com.forknight.common.dto.notification.NotificationDTO;
import com.forknight.server.security.UserPrincipal;
import com.forknight.server.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        List<NotificationDTO> notifs = notificationService.getUserNotifications(userPrincipal.id());
        return ResponseEntity.ok(notifs);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        List<NotificationDTO> notifs = notificationService.getUnreadNotifications(userPrincipal.id());
        return ResponseEntity.ok(notifs);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        notificationService.markAllAsRead(userPrincipal.id());
        return ResponseEntity.ok().build();
    }
}
