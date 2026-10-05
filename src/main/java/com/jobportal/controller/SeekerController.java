package com.jobportal.controller;

import com.jobportal.dto.SeekerProfileDto;
import com.jobportal.entity.Application;
import com.jobportal.entity.Job;
import com.jobportal.entity.User;
import com.jobportal.service.ApplicationService;
import com.jobportal.service.JobService;
import com.jobportal.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/seeker")
public class SeekerController {

    private final UserService userService;
    private final JobService jobService;
    private final ApplicationService applicationService;

    @Autowired
    public SeekerController(UserService userService, JobService jobService, ApplicationService applicationService) {
        this.userService = userService;
        this.jobService = jobService;
        this.applicationService = applicationService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        // Reload fresh user from DB
        User user = userService.findById(currentUser.getId()).orElse(currentUser);
        session.setAttribute("currentUser", user);

        List<Application> applications = applicationService.getApplicationsBySeeker(user);
        long shortlistedCount = applications.stream()
                .filter(a -> "SHORTLISTED".equalsIgnoreCase(a.getStatus()))
                .count();

        List<Job> recommendedJobs = jobService.getAllOpenJobs();
        if (recommendedJobs.size() > 4) {
            recommendedJobs = recommendedJobs.subList(0, 4);
        }

        model.addAttribute("user", user);
        model.addAttribute("applications", applications);
        model.addAttribute("totalApplications", applications.size());
        model.addAttribute("shortlistedCount", shortlistedCount);
        model.addAttribute("recommendedJobs", recommendedJobs);

        return "seeker/dashboard";
    }

    @GetMapping("/profile")
    public String viewProfile(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        User user = userService.findById(currentUser.getId()).orElse(currentUser);

        SeekerProfileDto profileDto = new SeekerProfileDto();
        profileDto.setFullName(user.getFullName());
        profileDto.setEmail(user.getEmail());
        profileDto.setPhone(user.getPhone());
        profileDto.setSkills(user.getSkills());
        profileDto.setExperience(user.getExperience());
        profileDto.setEducation(user.getEducation());

        model.addAttribute("profileDto", profileDto);
        model.addAttribute("user", user);

        return "seeker/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @Valid @ModelAttribute("profileDto") SeekerProfileDto profileDto,
            BindingResult bindingResult,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("currentUser");

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", currentUser);
            return "seeker/profile";
        }

        try {
            User updatedUser = userService.updateSeekerProfile(currentUser.getId(), profileDto);
            session.setAttribute("currentUser", updatedUser);
            session.setAttribute("userName", updatedUser.getFullName());
            redirectAttributes.addFlashAttribute("successMessage", "Your profile has been successfully updated!");
            return "redirect:/seeker/profile";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Error updating profile: " + ex.getMessage());
            model.addAttribute("user", currentUser);
            return "seeker/profile";
        }
    }

    @GetMapping("/applications")
    public String viewApplications(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        List<Application> applications = applicationService.getApplicationsBySeeker(currentUser);

        model.addAttribute("applications", applications);
        model.addAttribute("totalApplications", applications.size());

        return "seeker/applications";
    }

    @PostMapping("/apply/{jobId}")
    public String applyForJob(
            @PathVariable("jobId") Long jobId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("currentUser");

        try {
            applicationService.applyForJob(jobId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Congratulations! Your job application was submitted successfully.");
        } catch (IllegalStateException | IllegalArgumentException | SecurityException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to submit application at this time: " + ex.getMessage());
        }

        return "redirect:/jobs/" + jobId;
    }
}
