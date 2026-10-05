package com.jobportal.service;

import com.jobportal.dto.JobPostDto;
import com.jobportal.entity.Job;
import com.jobportal.entity.Role;
import com.jobportal.entity.User;
import com.jobportal.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class JobService {

    private final JobRepository jobRepository;

    @Autowired
    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public Job createJob(JobPostDto dto, User recruiter) {
        Job job = new Job();
        job.setTitle(dto.getTitle().trim());
        job.setCompanyName(dto.getCompanyName().trim());
        job.setLocation(dto.getLocation().trim());
        job.setExperience(dto.getExperience().trim());
        job.setJobType(dto.getJobType().trim());
        job.setSalary(dto.getSalary().trim());
        job.setSkills(dto.getSkills().trim());
        job.setDescription(dto.getDescription().trim());
        job.setStatus(dto.getStatus() != null && !dto.getStatus().isBlank() ? dto.getStatus() : "OPEN");
        job.setPostedDate(LocalDateTime.now());
        job.setRecruiter(recruiter);

        return jobRepository.save(job);
    }

    public Job updateJob(Long jobId, JobPostDto dto, User user) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with id: " + jobId));

        // Check ownership or admin
        if (!job.getRecruiter().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new SecurityException("Unauthorized: You can only edit your own jobs.");
        }

        job.setTitle(dto.getTitle().trim());
        job.setCompanyName(dto.getCompanyName().trim());
        job.setLocation(dto.getLocation().trim());
        job.setExperience(dto.getExperience().trim());
        job.setJobType(dto.getJobType().trim());
        job.setSalary(dto.getSalary().trim());
        job.setSkills(dto.getSkills().trim());
        job.setDescription(dto.getDescription().trim());
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            job.setStatus(dto.getStatus());
        }

        return jobRepository.save(job);
    }

    public void deleteJob(Long jobId, User user) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with id: " + jobId));

        if (!job.getRecruiter().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new SecurityException("Unauthorized: You can only delete your own jobs.");
        }

        jobRepository.delete(job);
    }

    public Job toggleJobStatus(Long jobId, User user) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with id: " + jobId));

        if (!job.getRecruiter().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new SecurityException("Unauthorized: You can only modify your own jobs.");
        }

        if ("OPEN".equalsIgnoreCase(job.getStatus())) {
            job.setStatus("CLOSED");
        } else {
            job.setStatus("OPEN");
        }

        return jobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public Optional<Job> getJobById(Long id) {
        return jobRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Job> getAllJobs() {
        return jobRepository.findAllByOrderByPostedDateDesc();
    }

    @Transactional(readOnly = true)
    public List<Job> getAllOpenJobs() {
        return jobRepository.findByStatusOrderByPostedDateDesc("OPEN");
    }

    @Transactional(readOnly = true)
    public List<Job> getJobsByRecruiter(User recruiter) {
        return jobRepository.findByRecruiterOrderByPostedDateDesc(recruiter);
    }

    @Transactional(readOnly = true)
    public List<Job> searchJobs(String keyword, String location, String experience, String jobType) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        String loc = (location != null && !location.isBlank()) ? location.trim() : null;
        String exp = (experience != null && !experience.isBlank() && !experience.equalsIgnoreCase("ALL")) ? experience.trim() : null;
        String type = (jobType != null && !jobType.isBlank() && !jobType.equalsIgnoreCase("ALL")) ? jobType.trim() : null;

        return jobRepository.searchJobs("OPEN", kw, loc, exp, type);
    }

    @Transactional(readOnly = true)
    public long countTotalJobs() {
        return jobRepository.count();
    }

    @Transactional(readOnly = true)
    public long countOpenJobs() {
        return jobRepository.countByStatus("OPEN");
    }

    @Transactional(readOnly = true)
    public long countJobsByRecruiter(User recruiter) {
        return jobRepository.countByRecruiter(recruiter);
    }

    public void initSampleJobs(User recruiter) {
        if (jobRepository.count() == 0 && recruiter != null) {
            Job j1 = new Job(
                    "Senior Java Backend Engineer",
                    "TechCorp Solutions",
                    "Bengaluru, India (Hybrid)",
                    "3-5 Years",
                    "Full-time",
                    "₹18,00,000 - ₹24,00,000 / yr",
                    "We are seeking an experienced Java Developer to architect and develop resilient microservices. You will work with Spring Boot, Hibernate, Kafka, and cloud platforms to deliver scalable distributed systems.",
                    "Java 17, Spring Boot, Microservices, Hibernate, PostgreSQL, Docker, REST APIs",
                    recruiter
            );

            Job j2 = new Job(
                    "Full Stack Web Developer",
                    "Innovatech Labs",
                    "Remote",
                    "1-3 Years",
                    "Full-time",
                    "₹10,00,000 - ₹14,00,000 / yr",
                    "Join our fast-paced product engineering team! Build responsive web applications using Spring MVC, Thymeleaf/React, and relational databases. Collaborative mindset and clean coding practices required.",
                    "Java, Spring MVC, HTML5/CSS3, JavaScript, Bootstrap, Git",
                    recruiter
            );

            Job j3 = new Job(
                    "Cloud DevOps & Platform Engineer",
                    "CloudMatrix Systems",
                    "Hyderabad, India",
                    "3-5 Years",
                    "Full-time",
                    "₹16,00,000 - ₹22,00,000 / yr",
                    "Looking for a passionate DevOps specialist to build CI/CD pipelines, manage Kubernetes clusters, automate infrastructure, and optimize application reliability.",
                    "AWS, Docker, Kubernetes, Jenkins, Terraform, Linux, CI/CD",
                    recruiter
            );

            Job j4 = new Job(
                    "Junior Software Developer (Trainee)",
                    "Apex Global Infotech",
                    "Pune, India",
                    "0-1 Years",
                    "Full-time",
                    "₹5,00,000 - ₹7,50,000 / yr",
                    "Great opportunity for fresh graduates and entry-level coders! Gain hands-on experience in Java enterprise development, REST APIs, and database fundamentals with our mentor program.",
                    "Core Java, SQL, OOP, HTML/CSS, Problem Solving",
                    recruiter
            );

            Job j5 = new Job(
                    "UI/UX Frontend Specialist",
                    "DesignCraft Digital",
                    "Remote",
                    "1-3 Years",
                    "Contract",
                    "₹8,00,000 - ₹12,00,000 / yr",
                    "Design intuitive, responsive user experiences for our enterprise SaaS suites. Must have an eye for aesthetic typography, smooth animations, and accessibility.",
                    "Figma, HTML5, CSS3, JavaScript, Bootstrap, Responsive Design",
                    recruiter
            );

            jobRepository.saveAll(List.of(j1, j2, j3, j4, j5));
        }
    }
}
