package com.ifconnected.repository.jpa;

import com.ifconnected.model.JPA.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    // Para o Aluno: "Minhas Candidaturas"
    List<JobApplication> findByCandidateIdOrderByAppliedAtDesc(Long candidateId);

    // Para a Empresa: "Quem se candidatou nesta vaga?"
    List<JobApplication> findByJobIdOrderByAppliedAtDesc(Long jobId);

    // Para validar se já aplicou
    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);

    // Para buscar uma candidatura específica (ex: cancelar)
    Optional<JobApplication> findByJobIdAndCandidateId(Long jobId, Long candidateId);
}