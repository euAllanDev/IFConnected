package com.ifconnected.controller;

import com.ifconnected.model.NOSQL.Notification;
import com.ifconnected.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications/user/{userId}")
    public List<Notification> getUserNotifications(@PathVariable Long userId) {
        return notificationService.getNotifications(userId);
    }

    @PutMapping("/notifications/user/{userId}/read")
    public void markNotificationsAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
    }

    @GetMapping("/notifications/user/{userId}/count")
    public long getUnreadNotificationCount(@PathVariable Long userId) {
        return notificationService.getUnreadCount(userId);
    }
}