package com.jobportal.service;

import com.jobportal.dto.SeekerProfileDto;
import com.jobportal.dto.UserRegistrationDto;
import com.jobportal.entity.Role;
import com.jobportal.entity.User;
import com.jobportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(UserRegistrationDto dto) {
        if (userRepository.existsByEmail(dto.getEmail().trim().toLowerCase())) {
            throw new IllegalArgumentException("An account with this email address already exists.");
        }

        User user = new User();
        user.setFullName(dto.getFullName().trim());
        user.setEmail(dto.getEmail().trim().toLowerCase());
        user.setPhone(dto.getPhone().trim());
        user.setPassword(dto.getPassword());
        user.setRole(dto.getRole());
        user.setActive(true);

        return userRepository.save(user);
    }

    public Optional<User> authenticate(String email, String password) {
        if (email == null || password == null) {
            return Optional.empty();
        }

        Optional<User> userOpt = userRepository.findByEmail(email.trim().toLowerCase());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(password) && user.isActive()) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return userRepository.findByEmail(email.trim().toLowerCase());
    }

    public User updateSeekerProfile(Long userId, SeekerProfileDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        user.setFullName(dto.getFullName().trim());
        user.setPhone(dto.getPhone().trim());
        user.setSkills(dto.getSkills() != null ? dto.getSkills().trim() : null);
        user.setExperience(dto.getExperience() != null ? dto.getExperience().trim() : null);
        user.setEducation(dto.getEducation() != null ? dto.getEducation().trim() : null);

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    @Transactional(readOnly = true)
    public long countByRole(Role role) {
        return userRepository.countByRole(role);
    }

    @Transactional(readOnly = true)
    public long countTotalUsers() {
        return userRepository.count();
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        user.setActive(!user.isActive());
        return userRepository.save(user);
    }

    public void initAdminAndSampleData() {
        String adminEmail = "admin@jobportal.com";
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = new User("System Administrator", adminEmail, "9876543210", "admin123", Role.ADMIN);
            userRepository.save(admin);
        }

        // Demo Recruiter if not present
        String recruiterEmail = "recruiter@techcorp.com";
        User demoRecruiter;
        if (!userRepository.existsByEmail(recruiterEmail)) {
            demoRecruiter = new User("Sarah Jenkins", recruiterEmail, "9123456780", "recruiter123", Role.RECRUITER);
            userRepository.save(demoRecruiter);
        } else {
            demoRecruiter = userRepository.findByEmail(recruiterEmail).get();
        }

        // Demo Job Seeker if not present
        String seekerEmail = "alex@example.com";
        if (!userRepository.existsByEmail(seekerEmail)) {
            User demoSeeker = new User("Alex Rivera", seekerEmail, "9812345678", "seeker123", Role.JOB_SEEKER);
            demoSeeker.setSkills("Java, Spring Boot, MySQL, REST APIs, HTML5, CSS3, JavaScript");
            demoSeeker.setExperience("2 Years as Junior Backend Developer");
            demoSeeker.setEducation("B.Tech in Computer Science, 2024");
            userRepository.save(demoSeeker);
        }
    }
}
