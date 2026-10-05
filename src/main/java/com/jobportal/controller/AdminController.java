package com.jobportal.controller;

import com.jobportal.entity.Application;
import com.jobportal.entity.Job;
import com.jobportal.entity.Role;
import com.jobportal.entity.User;
import com.jobportal.service.ApplicationService;
import com.jobportal.service.JobService;
import com.jobportal.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final JobService jobService;
    private final ApplicationService applicationService;

    @Autowired
    public AdminController(UserService userService, JobService jobService, ApplicationService applicationService) {
        this.userService = userService;
        this.jobService = jobService;
        this.applicationService = applicationService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long totalUsers = userService.countTotalUsers();
        long totalJobSeekers = userService.countByRole(Role.JOB_SEEKER);
        long totalRecruiters = userService.countByRole(Role.RECRUITER);
        long totalJobs = jobService.countTotalJobs();
        long totalApplications = applicationService.countTotalApplications();

        List<User> recentUsers = userService.getAllUsers();
        if (recentUsers.size() > 5) {
            recentUsers = recentUsers.subList(0, 5);
        }

        List<Job> recentJobs = jobService.getAllJobs();
        if (recentJobs.size() > 5) {
            recentJobs = recentJobs.subList(0, 5);
        }

        List<Application> recentApplications = applicationService.getAllApplications();
        if (recentApplications.size() > 5) {
            recentApplications = recentApplications.subList(0, 5);
        }

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalJobSeekers", totalJobSeekers);
        model.addAttribute("totalRecruiters", totalRecruiters);
        model.addAttribute("totalJobs", totalJobs);
        model.addAttribute("totalApplications", totalApplications);

        model.addAttribute("recentUsers", recentUsers);
        model.addAttribute("recentJobs", recentJobs);
        model.addAttribute("recentApplications", recentApplications);

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("totalUsers", users.size());
        return "admin/users";
    }

    @PostMapping("/users/toggle-status/{id}")
    public String toggleUserStatus(
            @PathVariable("id") Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser != null && currentUser.getId().equals(id)) {
            redirectAttributes.addFlashAttribute("errorMessage", "You cannot deactivate your own admin account.");
            return "redirect:/admin/users";
        }

        try {
            User updated = userService.toggleUserStatus(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "User account for " + updated.getEmail() + " is now " + (updated.isActive() ? "Active" : "Inactive"));
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error modifying user: " + ex.getMessage());
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(
            @PathVariable("id") Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser != null && currentUser.getId().equals(id)) {
            redirectAttributes.addFlashAttribute("errorMessage", "You cannot delete your own admin account.");
            return "redirect:/admin/users";
        }

        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User account deleted successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + ex.getMessage());
        }

        return "redirect:/admin/users";
    }

    @GetMapping("/jobs")
    public String listJobs(Model model) {
        List<Job> jobs = jobService.getAllJobs();
        model.addAttribute("jobs", jobs);
        model.addAttribute("totalJobs", jobs.size());
        return "admin/jobs";
    }

    @PostMapping("/jobs/toggle-status/{id}")
    public String toggleJobStatus(
            @PathVariable("id") Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("currentUser");
        try {
            Job updated = jobService.toggleJobStatus(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Job '" + updated.getTitle() + "' status changed to " + updated.getStatus());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error toggling job status: " + ex.getMessage());
        }

        return "redirect:/admin/jobs";
    }

    @PostMapping("/jobs/delete/{id}")
    public String deleteJob(
            @PathVariable("id") Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("currentUser");
        try {
            jobService.deleteJob(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Job posting deleted successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting job: " + ex.getMessage());
        }

        return "redirect:/admin/jobs";
    }

    @GetMapping("/applications")
    public String listApplications(Model model) {
        List<Application> applications = applicationService.getAllApplications();
        model.addAttribute("applications", applications);
        model.addAttribute("totalApplications", applications.size());
        return "admin/applications";
    }
}
