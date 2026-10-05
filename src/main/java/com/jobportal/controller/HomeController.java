package com.jobportal.controller;

import com.jobportal.entity.Job;
import com.jobportal.service.ApplicationService;
import com.jobportal.service.JobService;
import com.jobportal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final JobService jobService;
    private final UserService userService;
    private final ApplicationService applicationService;

    @Autowired
    public HomeController(JobService jobService, UserService userService, ApplicationService applicationService) {
        this.jobService = jobService;
        this.userService = userService;
        this.applicationService = applicationService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Job> recentJobs = jobService.getAllOpenJobs();
        if (recentJobs.size() > 6) {
            recentJobs = recentJobs.subList(0, 6);
        }
        model.addAttribute("recentJobs", recentJobs);
        model.addAttribute("totalJobs", jobService.countTotalJobs());
        model.addAttribute("totalUsers", userService.countTotalUsers());
        model.addAttribute("totalApplications", applicationService.countTotalApplications());
        return "index";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/403";
    }
}
