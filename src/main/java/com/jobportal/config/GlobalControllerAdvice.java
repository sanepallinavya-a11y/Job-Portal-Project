package com.jobportal.config;

import com.jobportal.entity.Role;
import com.jobportal.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute("currentUser")
    public User getCurrentUser(HttpSession session) {
        if (session != null) {
            return (User) session.getAttribute("currentUser");
        }
        return null;
    }

    @ModelAttribute("isLoggedIn")
    public boolean isLoggedIn(HttpSession session) {
        return session != null && session.getAttribute("currentUser") != null;
    }

    @ModelAttribute("isSeeker")
    public boolean isSeeker(HttpSession session) {
        if (session != null && session.getAttribute("currentUser") != null) {
            User user = (User) session.getAttribute("currentUser");
            return user.getRole() == Role.JOB_SEEKER;
        }
        return false;
    }

    @ModelAttribute("isRecruiter")
    public boolean isRecruiter(HttpSession session) {
        if (session != null && session.getAttribute("currentUser") != null) {
            User user = (User) session.getAttribute("currentUser");
            return user.getRole() == Role.RECRUITER;
        }
        return false;
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin(HttpSession session) {
        if (session != null && session.getAttribute("currentUser") != null) {
            User user = (User) session.getAttribute("currentUser");
            return user.getRole() == Role.ADMIN;
        }
        return false;
    }

    @ModelAttribute("currentUri")
    public String getCurrentUri(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
