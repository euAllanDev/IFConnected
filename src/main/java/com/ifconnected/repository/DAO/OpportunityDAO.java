package com.ifconnected.repository.dao;

import com.ifconnected.model.JPA.Opportunity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OpportunityDAO {

    @PersistenceContext
    private EntityManager entityManager;

    // Salvar (INSERT)
    @Transactional
    public void save(Opportunity opportunity) {
        entityManager.persist(opportunity);
    }

    // Atualizar (UPDATE)
    @Transactional
    public void update(Opportunity opportunity) {
        entityManager.merge(opportunity);
    }

    // Deletar (DELETE)
    @Transactional
    public void delete(Long id) {
        Opportunity op = entityManager.find(Opportunity.class, id);
        if (op != null) {
            entityManager.remove(op);
        }
    }

    // Buscar por ID
    public Opportunity findById(Long id) {
        return entityManager.find(Opportunity.class, id);
    }

    // Listar Todas (JPQL)
    public List<Opportunity> findAll() {
        return entityManager.createQuery("SELECT o FROM Opportunity o ORDER BY o.createdAt DESC", Opportunity.class)
                .getResultList();
    }

    // Buscar por Tipo (Exemplo de filtro manual com JPQL)
    public List<Opportunity> findByType(String type) {
        return entityManager.createQuery("SELECT o FROM Opportunity o WHERE o.type = :type", Opportunity.class)
                .setParameter("type", type)
                .getResultList();
    }
}