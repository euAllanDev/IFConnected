package com.ifconnected.repository.jpa;

import com.ifconnected.model.JPA.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    // Busca vagas ativas, ordenadas da mais recente para a mais antiga (Feed)
    List<Job> findByActiveTrueOrderByCreatedAtDesc();

    // Busca todas as vagas de uma empresa específica (Painel da Empresa)
    List<Job> findByCompanyIdOrderByCreatedAtDesc(Long companyId);
}