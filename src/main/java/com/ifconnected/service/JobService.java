package com.ifconnected.service;

import com.ifconnected.exception.BusinessRuleException;
import com.ifconnected.exception.ResourceNotFoundException;
import com.ifconnected.model.DTO.CandidateResponseDTO;
import com.ifconnected.model.DTO.MyApplicationDTO;
import com.ifconnected.model.JDBC.User;
import com.ifconnected.model.JPA.Job;
import com.ifconnected.model.JPA.JobApplication;
import com.ifconnected.model.enums.ApplicationStatus;
import com.ifconnected.model.enums.Role;
import com.ifconnected.repository.jpa.JobApplicationRepository;
import com.ifconnected.repository.jpa.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final MinioService minioService;

    public JobService(JobRepository jobRepository, JobApplicationRepository applicationRepository,
                      UserService userService, NotificationService notificationService, MinioService minioService) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.userService = userService;
        this.notificationService = notificationService;
        this.minioService = minioService;
    }

    //EMPRESA: Criar Vaga
    @Transactional
    public Job createJob(Job job, Long userId) {
        User user = userService.getUserEntityById(userId);

        if (user.getRole() != Role.COMPANY && user.getRole() != Role.ADMIN) {
            throw new BusinessRuleException("Apenas perfis de EMPRESA podem publicar vagas.");
        }

        job.setCompanyId(userId);
        job.setActive(true);
        job.setCreatedAt(LocalDateTime.now());

        // Se o front mandar uma URL, ele salva. Se não, salva nulo.
        return jobRepository.save(job);
    }

    //ALUNO: Aplicar para Vaga
    @Transactional
    public void applyToJob(Long jobId, Long candidateId, String coverLetter) {

        User candidate = userService.getUserEntityById(candidateId);

        // Apenas estudantes podem se candidatar
        if (candidate.getRole() != Role.STUDENT) {
            throw new BusinessRuleException("Apenas perfis de ESTUDANTE podem se candidatar a vagas.");
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Vaga não encontrada."));

        validateApplicationRules(job, candidateId);

        JobApplication app = new JobApplication(jobId, candidateId, coverLetter);
        applicationRepository.save(app);

        notifyCompanyAboutApplication(job, candidateId);
    }

    //ALUNO: Cancelar Candidatura
    @Transactional
    public void withdrawApplication(Long jobId, Long candidateId) {
        JobApplication app = applicationRepository.findByJobIdAndCandidateId(jobId, candidateId)
                .orElseThrow(() -> new RuntimeException("Candidatura não encontrada."));

        app.setStatus(ApplicationStatus.WITHDRAWN);
        applicationRepository.save(app);
    }

    //EMPRESA: Mudar Status
    @Transactional
    public void updateApplicationStatus(Long applicationId, ApplicationStatus newStatus, Long companyId) {
        JobApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Candidatura não encontrada."));

        Job job = jobRepository.findById(app.getJobId()).orElseThrow();

        if (!job.getCompanyId().equals(companyId)) {
            throw new BusinessRuleException("Você não tem permissão para gerenciar esta vaga.");
        }

        app.setStatus(newStatus);
        applicationRepository.save(app);

        notifyCandidateAboutStatusChange(app, job, companyId, newStatus);
    }

    public List<Job> getFeed() {
        return jobRepository.findByActiveTrueOrderByCreatedAtDesc();
    }

    public long countTotalJobs() {
        return jobRepository.count();
    }

    public List<CandidateResponseDTO> getJobCandidates(Long jobId, Long companyId) {
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Vaga não encontrada."));

        if (!job.getCompanyId().equals(companyId)) {
            throw new BusinessRuleException("Acesso negado.");
        }
        List<JobApplication> applications = applicationRepository.findByJobIdOrderByAppliedAtDesc(jobId);

        return applications.stream().map(app -> {
            User candidate = userService.getUserEntityById(app.getCandidateId());
            return new com.ifconnected.model.DTO.CandidateResponseDTO(
                    app.getId(),
                    candidate.getId(),
                    candidate.getUsername(),
                    candidate.getEmail(),
                    candidate.getProfileImageUrl(),
                    app.getCoverLetter(),
                    app.getStatus().name(),
                    app.getAppliedAt()
            );
        }).toList();
    }

    public List<MyApplicationDTO> getMyApplications(Long studentId) {
        List<JobApplication> applications = applicationRepository.findByCandidateIdOrderByAppliedAtDesc(studentId);

        return applications.stream().map(app -> {
            Job job = jobRepository.findById(app.getJobId()).orElseThrow();
            User company = userService.getUserEntityById(job.getCompanyId());

            return new MyApplicationDTO(
                    app.getId(),
                    job.getId(),
                    job.getTitle(),
                    company.getUsername(), // Nome da empresa
                    app.getStatus().name(),
                    app.getAppliedAt()
            );
        }).toList();
    }

    private void validateApplicationRules(Job job, Long candidateId) {
        if (!job.isActive()) {
            throw new BusinessRuleException("Esta vaga não está mais aceitando candidaturas.");
        }
        if (applicationRepository.existsByJobIdAndCandidateId(job.getId(), candidateId)) {
            throw new BusinessRuleException("Você já se candidatou para esta vaga.");
        }
    }

    private void notifyCompanyAboutApplication(Job job, Long candidateId) {
        User candidate = userService.getUserEntityById(candidateId);
        notificationService.createNotification(
                job.getCompanyId(),
                candidateId,
                candidate.getUsername(),
                "JOB_APPLY",
                job.getId().toString()
        );
    }

    private void notifyCandidateAboutStatusChange(JobApplication app, Job job, Long companyId, ApplicationStatus newStatus) {
        User company = userService.getUserEntityById(companyId);
        notificationService.createNotification(
                app.getCandidateId(),
                companyId,
                company.getUsername(),
                "JOB_STATUS_" + newStatus.name(),
                job.getId().toString()
        );
    }

    public List<Job> getJobsByCompany(Long companyId) {
        return jobRepository.findByCompanyIdOrderByCreatedAtDesc(companyId);
    }
}