package com.jobportal.controller;

import com.jobportal.dto.UserLoginDto;
import com.jobportal.dto.UserRegistrationDto;
import com.jobportal.entity.Role;
import com.jobportal.entity.User;
import com.jobportal.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model, HttpSession session) {
        if (session != null && session.getAttribute("currentUser") != null) {
            return redirectToDashboard((User) session.getAttribute("currentUser"));
        }
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new UserRegistrationDto());
        }
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(
            @Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            userService.registerUser(registrationDto);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please sign in with your credentials.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "register";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "An unexpected error occurred during registration. Please try again.");
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "registered", required = false) String registered,
            @RequestParam(value = "logout", required = false) String logout,
            Model model,
            HttpSession session) {

        if (session != null && session.getAttribute("currentUser") != null) {
            return redirectToDashboard((User) session.getAttribute("currentUser"));
        }

        if (!model.containsAttribute("loginDto")) {
            model.addAttribute("loginDto", new UserLoginDto());
        }

        if (error != null) {
            model.addAttribute("errorMessage", error);
        }
        if (registered != null) {
            model.addAttribute("successMessage", registered);
        }
        if (logout != null) {
            model.addAttribute("infoMessage", logout);
        }

        return "login";
    }

    @PostMapping("/login")
    public String processLogin(
            @Valid @ModelAttribute("loginDto") UserLoginDto loginDto,
            BindingResult bindingResult,
            HttpServletRequest request,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "login";
        }

        Optional<User> authenticatedUser = userService.authenticate(loginDto.getEmail(), loginDto.getPassword());

        if (authenticatedUser.isEmpty()) {
            model.addAttribute("errorMessage", "Invalid email or password. Please verify your credentials.");
            return "login";
        }

        User user = authenticatedUser.get();
        if (!user.isActive()) {
            model.addAttribute("errorMessage", "Your account has been deactivated. Please contact support.");
            return "login";
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("currentUser", user);
        session.setAttribute("userRole", user.getRole().name());
        session.setAttribute("userName", user.getFullName());

        return redirectToDashboard(user);
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        redirectAttributes.addFlashAttribute("infoMessage", "You have been successfully logged out.");
        return "redirect:/login";
    }

    private String redirectToDashboard(User user) {
        if (user.getRole() == Role.ADMIN) {
            return "redirect:/admin/dashboard";
        } else if (user.getRole() == Role.RECRUITER) {
            return "redirect:/recruiter/dashboard";
        } else {
            return "redirect:/seeker/dashboard";
        }
    }
}
