package com.ifconnected.service;

import com.ifconnected.model.NOSQL.Notification;
import com.ifconnected.repository.mongo.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // --- GATILHO PRINCIPAL ---
    public void createNotification(Long recipientId, Long senderId, String senderUsername, String type, String relatedPostId) {
        // Regra 1: Não criar notificação se o usuário curtir o próprio post
        if (recipientId.equals(senderId)) {
            return;
        }

        String message = "";
        switch (type) {
            case "LIKE":
                message = senderUsername + " curtiu sua publicação.";
                break;
            case "COMMENT":
                message = senderUsername + " comentou no seu post.";
                break;
            case "FOLLOW":
                message = senderUsername + " começou a seguir você.";
                break;
            default:
                message = senderUsername + " interagiu com você.";
        }

        Notification notification = new Notification(
                recipientId,
                senderId,
                senderUsername, // Salvamos o nome aqui para facilitar o Front
                type,
                message,
                relatedPostId
        );

        notificationRepository.save(notification);
    }

    // Listar notificações do usuário (mais recentes primeiro)
    public List<Notification> getNotifications(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    // Contar quantas não lidas (para o badge 🔴)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    // Marcar todas como lidas (quando abre a aba)
    public void markAllAsRead(Long userId) {
        List<Notification> unreadList = notificationRepository.findByRecipientIdAndIsReadFalse(userId);
        if (!unreadList.isEmpty()) {
            unreadList.forEach(n -> n.setRead(true));
            notificationRepository.saveAll(unreadList);
        }
    }
}