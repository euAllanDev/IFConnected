package com.ifconnected.service;

import com.ifconnected.model.DTO.DashboardDTO;
import com.ifconnected.repository.jdbc.CampusRepository;
import com.ifconnected.repository.jdbc.UserRepository;
import com.ifconnected.repository.jpa.JobRepository;
import com.ifconnected.repository.mongo.PostRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final UserService userService;
    private final JobService jobService;
    private final CampusService campusService;


    private final com.ifconnected.repository.mongo.PostRepository postRepository;

    public AdminService(UserService userService, JobService jobService,
                        CampusService campusService,
                        com.ifconnected.repository.mongo.PostRepository postRepository) {
        this.userService = userService;
        this.jobService = jobService;
        this.campusService = campusService;
        this.postRepository = postRepository;
    }

    public DashboardDTO getStats() {
        long usersCount = userService.getAllUsers().size();
        long jobsCount = jobService.countTotalJobs();
        long campusCount = campusService.getAll().size();

        long postsCount = postRepository.count();

        return new DashboardDTO(usersCount, postsCount, jobsCount, campusCount);
    }
}