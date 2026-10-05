package com.jobportal.controller;

import com.jobportal.dto.JobPostDto;
import com.jobportal.entity.Application;
import com.jobportal.entity.Job;
import com.jobportal.entity.User;
import com.jobportal.service.ApplicationService;
import com.jobportal.service.JobService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/recruiter")
public class RecruiterController {

    private final JobService jobService;
    private final ApplicationService applicationService;

    @Autowired
    public RecruiterController(JobService jobService, ApplicationService applicationService) {
        this.jobService = jobService;
        this.applicationService = applicationService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");

        List<Job> myJobs = jobService.getJobsByRecruiter(currentUser);
        List<Application> applicants = applicationService.getApplicationsByRecruiter(currentUser);

        long activeJobsCount = myJobs.stream().filter(Job::isOpen).count();
        long shortlistedCount = applicants.stream().filter(a -> "SHORTLISTED".equalsIgnoreCase(a.getStatus())).count();

        model.addAttribute("myJobs", myJobs);
        model.addAttribute("recentApplicants", applicants.size() > 5 ? applicants.subList(0, 5) : applicants);
        model.addAttribute("totalJobs", myJobs.size());
        model.addAttribute("activeJobs", activeJobsCount);
        model.addAttribute("totalApplicants", applicants.size());
        model.addAttribute("shortlistedCount", shortlistedCount);

        return "recruiter/dashboard";
    }

    @GetMapping("/jobs")
    public String myJobs(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        List<Job> myJobs = jobService.getJobsByRecruiter(currentUser);
        model.addAttribute("jobs", myJobs);
        return "recruiter/jobs";
    }

    @GetMapping("/jobs/new")
    public String showJobForm(Model model) {
        if (!model.containsAttribute("jobDto")) {
            JobPostDto dto = new JobPostDto();
            dto.setStatus("OPEN");
            model.addAttribute("jobDto", dto);
        }
        model.addAttribute("isEdit", false);
        return "recruiter/job-form";
    }

    @PostMapping("/jobs/new")
    public String createJob(
            @Valid @ModelAttribute("jobDto") JobPostDto jobDto,
            BindingResult bindingResult,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "recruiter/job-form";
        }

        User currentUser = (User) session.getAttribute("currentUser");
        jobService.createJob(jobDto, currentUser);

        redirectAttributes.addFlashAttribute("successMessage", "New job posting has been successfully created!");
        return "redirect:/recruiter/jobs";
    }

    @GetMapping("/jobs/edit/{id}")
    public String showEditJobForm(@PathVariable("id") Long id, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        Optional<Job> jobOpt = jobService.getJobById(id);

        if (jobOpt.isEmpty()) {
            return "error/404";
        }

        Job job = jobOpt.get();
        if (!job.getRecruiter().getId().equals(currentUser.getId())) {
            return "redirect:/access-denied";
        }

        JobPostDto dto = new JobPostDto();
        dto.setId(job.getId());
        dto.setTitle(job.getTitle());
        dto.setCompanyName(job.getCompanyName());
        dto.setLocation(job.getLocation());
        dto.setExperience(job.getExperience());
        dto.setJobType(job.getJobType());
        dto.setSalary(job.getSalary());
        dto.setSkills(job.getSkills());
        dto.setDescription(job.getDescription());
        dto.setStatus(job.getStatus());

        model.addAttribute("jobDto", dto);
        model.addAttribute("isEdit", true);
        model.addAttribute("jobId", job.getId());

        return "recruiter/job-form";
    }

    @PostMapping("/jobs/edit/{id}")
    public String updateJob(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("jobDto") JobPostDto jobDto,
            BindingResult bindingResult,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("jobId", id);
            return "recruiter/job-form";
        }

        User currentUser = (User) session.getAttribute("currentUser");

        try {
            jobService.updateJob(id, jobDto, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Job posting updated successfully!");
            return "redirect:/recruiter/jobs";
        } catch (SecurityException ex) {
            return "redirect:/access-denied";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Error updating job: " + ex.getMessage());
            model.addAttribute("isEdit", true);
            model.addAttribute("jobId", id);
            return "recruiter/job-form";
        }
    }

    @PostMapping("/jobs/toggle-status/{id}")
    public String toggleJobStatus(@PathVariable("id") Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        try {
            Job updatedJob = jobService.toggleJobStatus(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Job status toggled to " + updatedJob.getStatus() + " successfully.");
        } catch (SecurityException ex) {
            return "redirect:/access-denied";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not change status: " + ex.getMessage());
        }
        return "redirect:/recruiter/jobs";
    }

    @PostMapping("/jobs/delete/{id}")
    public String deleteJob(@PathVariable("id") Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        try {
            jobService.deleteJob(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Job posting deleted successfully.");
        } catch (SecurityException ex) {
            return "redirect:/access-denied";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not delete job: " + ex.getMessage());
        }
        return "redirect:/recruiter/jobs";
    }

    @GetMapping("/applicants")
    public String viewApplicants(
            @RequestParam(value = "jobId", required = false) Long jobId,
            @RequestParam(value = "status", required = false) String status,
            Model model,
            HttpSession session) {

        User currentUser = (User) session.getAttribute("currentUser");
        List<Job> myJobs = jobService.getJobsByRecruiter(currentUser);
        List<Application> applicants = applicationService.filterRecruiterApplications(currentUser, jobId, status);

        model.addAttribute("applicants", applicants);
        model.addAttribute("myJobs", myJobs);
        model.addAttribute("selectedJobId", jobId);
        model.addAttribute("selectedStatus", status != null ? status : "ALL");
        model.addAttribute("totalApplicants", applicants.size());

        return "recruiter/applicants";
    }

    @PostMapping("/applicants/status/{id}")
    public String updateApplicantStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") String status,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("currentUser");
        try {
            applicationService.updateApplicationStatus(id, status, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Applicant status successfully updated to " + status);
        } catch (SecurityException ex) {
            return "redirect:/access-denied";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update status: " + ex.getMessage());
        }
        return "redirect:/recruiter/applicants";
    }
}
