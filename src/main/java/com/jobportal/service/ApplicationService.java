package com.jobportal.service;

import com.jobportal.entity.Application;
import com.jobportal.entity.Job;
import com.jobportal.entity.Role;
import com.jobportal.entity.User;
import com.jobportal.repository.ApplicationRepository;
import com.jobportal.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;

    @Autowired
    public ApplicationService(ApplicationRepository applicationRepository, JobRepository jobRepository) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
    }

    public Application applyForJob(Long jobId, User jobSeeker) {
        if (jobSeeker == null || jobSeeker.getRole() != Role.JOB_SEEKER) {
            throw new SecurityException("Only registered Job Seekers can apply for jobs.");
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job position not found with id: " + jobId));

        if (!job.isOpen()) {
            throw new IllegalStateException("This job position is currently closed and no longer accepting applications.");
        }

        if (applicationRepository.existsByJobAndJobSeeker(job, jobSeeker)) {
            throw new IllegalStateException("You have already submitted an application for this position.");
        }

        Application application = new Application();
        application.setJob(job);
        application.setJobSeeker(jobSeeker);
        application.setStatus("APPLIED");
        application.setAppliedDate(LocalDateTime.now());

        return applicationRepository.save(application);
    }

    @Transactional(readOnly = true)
    public boolean hasApplied(Long jobId, User jobSeeker) {
        if (jobSeeker == null || jobId == null) return false;
        Optional<Job> jobOpt = jobRepository.findById(jobId);
        return jobOpt.filter(job -> applicationRepository.existsByJobAndJobSeeker(job, jobSeeker)).isPresent();
    }

    @Transactional(readOnly = true)
    public List<Application> getApplicationsBySeeker(User jobSeeker) {
        return applicationRepository.findByJobSeekerOrderByAppliedDateDesc(jobSeeker);
    }

    @Transactional(readOnly = true)
    public List<Application> getApplicationsByRecruiter(User recruiter) {
        return applicationRepository.findByRecruiterOrderByAppliedDateDesc(recruiter);
    }

    @Transactional(readOnly = true)
    public List<Application> filterRecruiterApplications(User recruiter, Long jobId, String status) {
        String cleanStatus = (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) ? status.trim() : null;
        return applicationRepository.filterRecruiterApplications(recruiter, jobId, cleanStatus);
    }

    @Transactional(readOnly = true)
    public List<Application> getApplicationsByJob(Job job) {
        return applicationRepository.findByJobOrderByAppliedDateDesc(job);
    }

    public Application updateApplicationStatus(Long applicationId, String status, User user) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found with id: " + applicationId));

        // Check ownership or admin
        boolean isOwnerRecruiter = application.getJob().getRecruiter().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isOwnerRecruiter && !isAdmin) {
            throw new SecurityException("Unauthorized: You can only manage applications for your own job postings.");
        }

        String normalizedStatus = status.trim().toUpperCase();
        if (!List.of("APPLIED", "SHORTLISTED", "REJECTED").contains(normalizedStatus)) {
            throw new IllegalArgumentException("Invalid status. Allowed values: APPLIED, SHORTLISTED, REJECTED");
        }

        application.setStatus(normalizedStatus);
        return applicationRepository.save(application);
    }

    @Transactional(readOnly = true)
    public Optional<Application> getApplicationById(Long id) {
        return applicationRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Application> getAllApplications() {
        return applicationRepository.findAllByOrderByAppliedDateDesc();
    }

    @Transactional(readOnly = true)
    public long countTotalApplications() {
        return applicationRepository.count();
    }

    @Transactional(readOnly = true)
    public long countApplicationsBySeeker(User jobSeeker) {
        return applicationRepository.countByJobSeeker(jobSeeker);
    }

    @Transactional(readOnly = true)
    public long countApplicationsByRecruiter(User recruiter) {
        return applicationRepository.countByRecruiter(recruiter);
    }

    @Transactional(readOnly = true)
    public long countShortlistedByRecruiter(User recruiter) {
        return applicationRepository.countByRecruiterAndStatus(recruiter, "SHORTLISTED");
    }
}
