package com.jobportal.config;

import com.jobportal.entity.User;
import com.jobportal.service.JobService;
import com.jobportal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final JobService jobService;

    @Autowired
    public DataInitializer(UserService userService, JobService jobService) {
        this.userService = userService;
        this.jobService = jobService;
    }

    @Override
    public void run(String... args) {
        userService.initAdminAndSampleData();

        Optional<User> recruiterOpt = userService.findByEmail("recruiter@techcorp.com");
        recruiterOpt.ifPresent(jobService::initSampleJobs);
    }
}
