package com.jobportal.config;

import com.jobportal.entity.Role;
import com.jobportal.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        HttpSession session = request.getSession(false);

        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (uri.startsWith("/seeker") || uri.startsWith("/apply")) {
            if (currentUser == null) {
                response.sendRedirect("/login?error=Please login to access your Job Seeker portal.");
                return false;
            }
            if (currentUser.getRole() != Role.JOB_SEEKER) {
                response.sendRedirect("/access-denied");
                return false;
            }
        }

        if (uri.startsWith("/recruiter")) {
            if (currentUser == null) {
                response.sendRedirect("/login?error=Please login to access the Recruiter portal.");
                return false;
            }
            if (currentUser.getRole() != Role.RECRUITER) {
                response.sendRedirect("/access-denied");
                return false;
            }
        }

        if (uri.startsWith("/admin")) {
            if (currentUser == null) {
                response.sendRedirect("/login?error=Please login to access the Admin panel.");
                return false;
            }
            if (currentUser.getRole() != Role.ADMIN) {
                response.sendRedirect("/access-denied");
                return false;
            }
        }

        return true;
    }
}
