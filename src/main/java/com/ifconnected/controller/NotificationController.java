package com.ifconnected.controller;

import com.ifconnected.model.NOSQL.Notification;
import com.ifconnected.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // GET /api/notifications/user/{userId}
    @GetMapping("/user/{userId}")
    public List<Notification> getUserNotifications(@PathVariable Long userId) {
        return notificationService.getNotifications(userId);
    }

    // PUT /api/notifications/user/{userId}/read
    // Marca tudo como lido (para zerar o contador)
    @PutMapping("/user/{userId}/read")
    public void markAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
    }

    // GET /api/notifications/user/{userId}/count
    @GetMapping("/user/{userId}/count")
    public long getUnreadCount(@PathVariable Long userId) {
        return notificationService.getUnreadCount(userId);
    }
}