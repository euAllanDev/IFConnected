package com.ifconnected.repository.mongo;

import com.ifconnected.model.NOSQL.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {

    // Busca todas ordenadas por data
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    // Conta as não lidas
    long countByRecipientIdAndIsReadFalse(Long recipientId);

    // Busca as não lidas (para marcar como lidas em lote)
    List<Notification> findByRecipientIdAndIsReadFalse(Long recipientId);
}