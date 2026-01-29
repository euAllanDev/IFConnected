package com.ifconnected.dao.impl;

import com.ifconnected.dao.LostFoundItemDAO;
import com.ifconnected.model.JPA.LostFoundItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class LostFoundItemDAOImpl implements LostFoundItemDAO {

    @PersistenceContext
    private EntityManager em;

    @Override
    public LostFoundItem save(LostFoundItem item) {
        if (item.getId() == null) {
            em.persist(item);
            return item;
        }
        return em.merge(item);
    }

    @Override
    public LostFoundItem findById(Long id) {
        return em.find(LostFoundItem.class, id);
    }

    @Override
    public List<LostFoundItem> findAll() {
        return em.createQuery("SELECT i FROM LostFoundItem i ORDER BY i.id DESC", LostFoundItem.class)
                .getResultList();
    }

    @Override
    public List<LostFoundItem> findByStatus(String status) {
        return em.createQuery(
                        "SELECT i FROM LostFoundItem i WHERE i.status = :status ORDER BY i.id DESC",
                        LostFoundItem.class
                )
                .setParameter("status", status)
                .getResultList();
    }

    @Override
    public List<LostFoundItem> search(String q) {
        String like = "%" + q.toLowerCase() + "%";
        return em.createQuery(
                        "SELECT i FROM LostFoundItem i " +
                                "WHERE LOWER(i.title) LIKE :q OR LOWER(i.description) LIKE :q " +
                                "ORDER BY i.id DESC",
                        LostFoundItem.class
                )
                .setParameter("q", like)
                .getResultList();
    }

    @Override
    public void delete(Long id) {
        LostFoundItem item = em.find(LostFoundItem.class, id);
        if (item != null) em.remove(item);
    }


    @Override
    public List<LostFoundItem> findByOwnerId(Long ownerId) {
        return em.createQuery(
                        "SELECT i FROM LostFoundItem i WHERE i.owner.id = :ownerId ORDER BY i.id DESC",
                        LostFoundItem.class
                )
                .setParameter("ownerId", ownerId)
                .getResultList();
    }
}
