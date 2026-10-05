package com.jobportal.controller;

import com.jobportal.entity.Job;
import com.jobportal.entity.Role;
import com.jobportal.entity.User;
import com.jobportal.service.ApplicationService;
import com.jobportal.service.JobService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/jobs")
public class JobController {

    private final JobService jobService;
    private final ApplicationService applicationService;

    @Autowired
    public JobController(JobService jobService, ApplicationService applicationService) {
        this.jobService = jobService;
        this.applicationService = applicationService;
    }

    @GetMapping
    public String listJobs(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "experience", required = false) String experience,
            @RequestParam(value = "jobType", required = false) String jobType,
            Model model) {

        List<Job> jobs;
        boolean isFiltered = (keyword != null && !keyword.isBlank()) ||
                             (location != null && !location.isBlank()) ||
                             (experience != null && !experience.isBlank() && !experience.equalsIgnoreCase("ALL")) ||
                             (jobType != null && !jobType.isBlank() && !jobType.equalsIgnoreCase("ALL"));

        if (isFiltered) {
            jobs = jobService.searchJobs(keyword, location, experience, jobType);
        } else {
            jobs = jobService.getAllOpenJobs();
        }

        model.addAttribute("jobs", jobs);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("location", location != null ? location : "");
        model.addAttribute("experience", experience != null ? experience : "ALL");
        model.addAttribute("jobType", jobType != null ? jobType : "ALL");
        model.addAttribute("totalResults", jobs.size());
        model.addAttribute("isFiltered", isFiltered);

        return "jobs/list";
    }

    @GetMapping("/{id}")
    public String viewJobDetails(@PathVariable("id") Long id, Model model, HttpSession session) {
        Optional<Job> jobOpt = jobService.getJobById(id);
        if (jobOpt.isEmpty()) {
            return "error/404";
        }

        Job job = jobOpt.get();
        model.addAttribute("job", job);

        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;
        boolean hasApplied = false;
        boolean isOwner = false;

        if (currentUser != null) {
            if (currentUser.getRole() == Role.JOB_SEEKER) {
                hasApplied = applicationService.hasApplied(job.getId(), currentUser);
            }
            if (currentUser.getRole() == Role.RECRUITER && job.getRecruiter().getId().equals(currentUser.getId())) {
                isOwner = true;
            }
        }

        model.addAttribute("hasApplied", hasApplied);
        model.addAttribute("isOwner", isOwner);

        return "jobs/details";
    }
}
