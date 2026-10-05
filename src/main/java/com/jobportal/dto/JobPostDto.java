package com.jobportal.dto;

import jakarta.validation.constraints.NotBlank;

public class JobPostDto {

    private Long id;

    @NotBlank(message = "Job Title is required")
    private String title;

    @NotBlank(message = "Company Name is required")
    private String companyName;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Experience is required")
    private String experience;

    @NotBlank(message = "Job Type is required")
    private String jobType;

    @NotBlank(message = "Salary is required")
    private String salary;

    @NotBlank(message = "Required Skills are required")
    private String skills;

    @NotBlank(message = "Job Description is required")
    private String description;

    private String status = "OPEN";

    public JobPostDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
