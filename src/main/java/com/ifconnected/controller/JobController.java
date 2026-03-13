package com.ifconnected.controller;

import com.ifconnected.model.DTO.CandidateResponseDTO;
import com.ifconnected.model.DTO.MyApplicationDTO;
import com.ifconnected.model.JPA.Job;
import com.ifconnected.model.JPA.JobApplication;
import com.ifconnected.model.enums.ApplicationStatus;
import com.ifconnected.service.JobService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    // 1. Feed de Vagas (Público/Autenticado)
    @GetMapping
    public List<Job> getJobFeed() {
        return jobService.getFeed();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Job createJob(@RequestBody Job job, @RequestParam Long userId) {
        return jobService.createJob(job, userId);
    }
    // 3. Aplicar para Vaga (Alunos)
    @PostMapping("/{jobId}/apply")
    public ResponseEntity<?> apply(@PathVariable Long jobId,
                                   @RequestBody Map<String, Object> payload) {
        Long candidateId = Long.valueOf(payload.get("userId").toString());
        String coverLetter = (String) payload.get("coverLetter");

        jobService.applyToJob(jobId, candidateId, coverLetter);
        return ResponseEntity.ok().build();
    }

    // 4. Ver Candidatos da Vaga (Apenas dono da vaga)
    @GetMapping("/{jobId}/candidates")
    public List<CandidateResponseDTO> getCandidates(@PathVariable Long jobId, @RequestParam Long companyId) {
        return jobService.getJobCandidates(jobId, companyId);
    }

    // 5. Mudar Status do Candidato (Empresa)
    @PatchMapping("/applications/{applicationId}/status")
    public void updateStatus(@PathVariable Long applicationId,
                             @RequestBody Map<String, Object> payload) {
        String statusStr = (String) payload.get("status");
        Long companyId = Long.valueOf(payload.get("companyId").toString());

        jobService.updateApplicationStatus(applicationId, ApplicationStatus.valueOf(statusStr), companyId);
    }

    // 6. Minhas Candidaturas (Aluno)
    @GetMapping("/my-applications")
    public List<MyApplicationDTO> getMyApplications(@RequestParam Long userId) {
        return jobService.getMyApplications(userId);
    }

    @GetMapping("/company/{companyId}")
    public List<Job> getCompanyJobs(@PathVariable Long companyId) {
        // Isso depende de você ter o método findByCompanyIdOrderByCreatedAtDesc no JobRepository
        // Que já havíamos criado em passos anteriores!
        return jobService.getJobsByCompany(companyId);
    }


}