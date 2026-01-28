package com.ifconnected.service;

import com.ifconnected.dao.LostFoundItemDAO;
import com.ifconnected.model.DTO.CreateLostFoundItemDTO;
import com.ifconnected.model.JPA.LostFoundItem;
import com.ifconnected.model.JPA.LostFoundStatus;
import com.ifconnected.model.JPA.UserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LostFoundService {

    private final LostFoundItemDAO dao;
    private final EntityManager em;

    public LostFoundService(LostFoundItemDAO dao, EntityManager em) {
        this.dao = dao;
        this.em = em;
    }

    public LostFoundItem create(Long userId, CreateLostFoundItemDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Body é obrigatório");
        if (dto.title() == null || dto.title().isBlank()) throw new IllegalArgumentException("title é obrigatório");
        if (dto.description() == null || dto.description().isBlank()) throw new IllegalArgumentException("description é obrigatório");

        // 1) busca owner como entidade JPA
        UserEntity owner = em.find(UserEntity.class, userId);
        if (owner == null) {
            throw new EntityNotFoundException("Usuário (UserEntity) não encontrado. id=" + userId);
        }

        // 2) cria item
        LostFoundItem item = new LostFoundItem();
        item.setTitle(dto.title().trim());
        item.setDescription(dto.description().trim());
        item.setStatus(parseStatus(dto.status()));

        // 3) relacionamento (ManyToOne)
        item.setOwner(owner);

        return dao.save(item);
    }

    public List<LostFoundItem> listAll() {
        return dao.findAll();
    }

    public List<LostFoundItem> search(String q) {
        if (q == null || q.isBlank()) return listAll();
        return dao.search(q.trim());
    }

    private LostFoundStatus parseStatus(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("status é obrigatório");
        }
        try {
            return LostFoundStatus.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Status inválido: '" + raw + "'. Use: PERDIDO | ACHADO | DEVOLVIDO"
            );
        }
    }
}
