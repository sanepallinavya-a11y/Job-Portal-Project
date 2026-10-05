package com.jobportal.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "applications", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"job_id", "job_seeker_id"})
})
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "job_seeker_id", nullable = false)
    private User jobSeeker;

    private LocalDateTime appliedDate = LocalDateTime.now();

    @Column(nullable = false)
    private String status = "APPLIED"; // APPLIED, SHORTLISTED, REJECTED

    public Application() {
    }

    public Application(Job job, User jobSeeker) {
        this.job = job;
        this.jobSeeker = jobSeeker;
        this.appliedDate = LocalDateTime.now();
        this.status = "APPLIED";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public User getJobSeeker() {
        return jobSeeker;
    }

    public void setJobSeeker(User jobSeeker) {
        this.jobSeeker = jobSeeker;
    }

    public LocalDateTime getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(LocalDateTime appliedDate) {
        this.appliedDate = appliedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
