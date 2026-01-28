package com.ifconnected.service;

import com.ifconnected.model.JPA.Opportunity;
import com.ifconnected.model.JDBC.User;
import com.ifconnected.repository.dao.OpportunityDAO;
import org.springframework.stereotype.Service;
import com.ifconnected.model.JDBC.User;

import java.util.List;

@Service
public class OpportunityService {

    private final OpportunityDAO opportunityDAO;
    private final UserService userService;

    public OpportunityService(OpportunityDAO opportunityDAO, UserService userService) {
        this.opportunityDAO = opportunityDAO;
        this.userService = userService;
    }

    public void createOpportunity(Opportunity opportunity, Long userId) {
        // Validação de Segurança Simples
        User user = userService.getUserEntityById(userId);
        if (user == null || !user.isAdmin()) {
            throw new RuntimeException("Acesso Negado: Apenas administradores podem criar oportunidades.");
        }

        opportunity.setCreatorId(userId);
        opportunityDAO.save(opportunity);
    }

    public List<Opportunity> getAll() {
        return opportunityDAO.findAll();
    }

    public void deleteOpportunity(Long id, Long userId) {
        User user = userService.getUserEntityById(userId);

        if (user == null || !user.isAdmin()) {
            throw new RuntimeException("Acesso Negado.");
        }
        opportunityDAO.delete(id);
    }
}