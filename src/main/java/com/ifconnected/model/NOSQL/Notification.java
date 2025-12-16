package com.ifconnected.model.NOSQL;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;
    private Long recipientId; // Quem recebe a notificação (ID do Postgres)
    private Long senderId;    // Quem realizou a ação (ID do Postgres)
    private String senderUsername; // Nome de quem enviou (para exibir sem buscar no banco)
    private String type;      // LIKE, FOLLOW, COMMENT
    private String message;   // Texto pronto (ex: "Allan curtiu sua foto")
    private String relatedPostId; // ID do post (se houver)
    private boolean isRead = false; // Se foi lida
    private LocalDateTime createdAt = LocalDateTime.now();

    // --- Construtores ---
    public Notification() {}

    public Notification(Long recipientId, Long senderId, String senderUsername, String type, String message, String relatedPostId) {
        this.recipientId = recipientId;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.type = type;
        this.message = message;
        this.relatedPostId = relatedPostId;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters e Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getRelatedPostId() { return relatedPostId; }
    public void setRelatedPostId(String relatedPostId) { this.relatedPostId = relatedPostId; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}