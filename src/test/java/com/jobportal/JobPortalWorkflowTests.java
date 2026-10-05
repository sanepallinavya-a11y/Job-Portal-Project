package com.jobportal;

import com.jobportal.dto.JobPostDto;
import com.jobportal.dto.SeekerProfileDto;
import com.jobportal.dto.UserRegistrationDto;
import com.jobportal.entity.Application;
import com.jobportal.entity.Job;
import com.jobportal.entity.Role;
import com.jobportal.entity.User;
import com.jobportal.service.ApplicationService;
import com.jobportal.service.JobService;
import com.jobportal.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class JobPortalWorkflowTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private JobService jobService;

    @Autowired
    private ApplicationService applicationService;

    @Test
    @DisplayName("Verify default Admin user is automatically created upon startup")
    void testDefaultAdminAutoCreated() {
        Optional<User> adminOpt = userService.findByEmail("admin@jobportal.com");
        assertTrue(adminOpt.isPresent(), "Default admin user must exist in the database");
        User admin = adminOpt.get();
        assertEquals("admin123", admin.getPassword());
        assertEquals(Role.ADMIN, admin.getRole());
        assertTrue(admin.isActive());
    }

    @Test
    @DisplayName("Verify user registration workflow for Job Seeker")
    void testJobSeekerRegistration() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setFullName("David Miller");
        dto.setEmail("david.m@testportal.com");
        dto.setPhone("9876543210");
        dto.setPassword("pass123");
        dto.setRole(Role.JOB_SEEKER);

        User registered = userService.registerUser(dto);
        assertNotNull(registered.getId());
        assertEquals("David Miller", registered.getFullName());
        assertEquals("david.m@testportal.com", registered.getEmail());
        assertEquals(Role.JOB_SEEKER, registered.getRole());
        assertTrue(registered.isActive());
    }

    @Test
    @DisplayName("Verify duplicate email registration is rejected")
    void testDuplicateEmailRejection() {
        UserRegistrationDto dto1 = new UserRegistrationDto();
        dto1.setFullName("User One");
        dto1.setEmail("duplicate@testportal.com");
        dto1.setPhone("9876543210");
        dto1.setPassword("pass123");
        dto1.setRole(Role.JOB_SEEKER);
        userService.registerUser(dto1);

        UserRegistrationDto dto2 = new UserRegistrationDto();
        dto2.setFullName("User Two");
        dto2.setEmail("duplicate@testportal.com");
        dto2.setPhone("9876543211");
        dto2.setPassword("pass456");
        dto2.setRole(Role.RECRUITER);

        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(dto2),
                "Registering with duplicate email must throw IllegalArgumentException");
    }

    @Test
    @DisplayName("Verify authentication for valid and invalid credentials")
    void testAuthentication() {
        // Valid
        Optional<User> authAdmin = userService.authenticate("admin@jobportal.com", "admin123");
        assertTrue(authAdmin.isPresent());
        assertEquals(Role.ADMIN, authAdmin.get().getRole());

        // Invalid password
        Optional<User> invalidPass = userService.authenticate("admin@jobportal.com", "wrongpass");
        assertFalse(invalidPass.isPresent());

        // Non-existent email
        Optional<User> unknownUser = userService.authenticate("nonexistent@domain.com", "anypass");
        assertFalse(unknownUser.isPresent());
    }

    @Test
    @DisplayName("Verify Job Seeker profile update workflow")
    void testSeekerProfileUpdate() {
        User seeker = userService.findByEmail("alex@example.com").orElseThrow();

        SeekerProfileDto profileDto = new SeekerProfileDto();
        profileDto.setFullName("Alex Rivera Updated");
        profileDto.setEmail(seeker.getEmail());
        profileDto.setPhone("9112233445");
        profileDto.setSkills("Java 17, Spring Boot, Microservices, Angular, Docker");
        profileDto.setExperience("3 Years Senior Software Engineer");
        profileDto.setEducation("Master of Science in Computer Science");

        User updated = userService.updateSeekerProfile(seeker.getId(), profileDto);
        assertEquals("Alex Rivera Updated", updated.getFullName());
        assertEquals("9112233445", updated.getPhone());
        assertEquals("Java 17, Spring Boot, Microservices, Angular, Docker", updated.getSkills());
        assertEquals("3 Years Senior Software Engineer", updated.getExperience());
    }

    @Test
    @DisplayName("Verify job posting, updating, and search filters")
    void testJobPostingAndSearch() {
        User recruiter = userService.findByEmail("recruiter@techcorp.com").orElseThrow();

        JobPostDto postDto = new JobPostDto();
        postDto.setTitle("Lead Cloud Architect");
        postDto.setCompanyName("TechCorp Solutions");
        postDto.setLocation("Mumbai, India");
        postDto.setExperience("5+ Years");
        postDto.setJobType("Full-time");
        postDto.setSalary("₹30,00,000 - ₹40,00,000 / yr");
        postDto.setSkills("AWS, GCP, Terraform, Kubernetes, Java");
        postDto.setDescription("Design enterprise cloud infrastructures with high availability and fault tolerance.");
        postDto.setStatus("OPEN");

        Job createdJob = jobService.createJob(postDto, recruiter);
        assertNotNull(createdJob.getId());
        assertEquals("OPEN", createdJob.getStatus());

        // Test Search by title keyword
        List<Job> titleResults = jobService.searchJobs("Architect", null, null, null);
        assertTrue(titleResults.stream().anyMatch(j -> j.getId().equals(createdJob.getId())));

        // Test Search by location
        List<Job> locResults = jobService.searchJobs(null, "Mumbai", null, null);
        assertTrue(locResults.stream().anyMatch(j -> j.getId().equals(createdJob.getId())));

        // Test Search by experience
        List<Job> expResults = jobService.searchJobs(null, null, "5+ Years", null);
        assertTrue(expResults.stream().anyMatch(j -> j.getId().equals(createdJob.getId())));

        // Test Search by job type
        List<Job> typeResults = jobService.searchJobs(null, null, null, "Full-time");
        assertTrue(typeResults.stream().anyMatch(j -> j.getId().equals(createdJob.getId())));
    }

    @Test
    @DisplayName("Verify job application workflow and duplicate application prevention")
    void testJobApplicationWorkflow() {
        User seeker = userService.findByEmail("alex@example.com").orElseThrow();
        List<Job> openJobs = jobService.getAllOpenJobs();
        assertFalse(openJobs.isEmpty(), "Sample jobs must be available");
        Job targetJob = openJobs.get(0);

        // 1. Submit application
        Application application = applicationService.applyForJob(targetJob.getId(), seeker);
        assertNotNull(application.getId());
        assertEquals("APPLIED", application.getStatus());
        assertEquals(targetJob.getId(), application.getJob().getId());
        assertEquals(seeker.getId(), application.getJobSeeker().getId());

        // 2. Duplicate application prevention
        assertThrows(IllegalStateException.class, () -> applicationService.applyForJob(targetJob.getId(), seeker),
                "Submitting duplicate application for the same job must throw IllegalStateException");

        // 3. Verify hasApplied
        assertTrue(applicationService.hasApplied(targetJob.getId(), seeker));
    }

    @Test
    @DisplayName("Verify applying to closed job is prohibited")
    void testApplyToClosedJobProhibited() {
        User recruiter = userService.findByEmail("recruiter@techcorp.com").orElseThrow();
        User seeker = userService.findByEmail("alex@example.com").orElseThrow();

        JobPostDto postDto = new JobPostDto();
        postDto.setTitle("Archived QA Position");
        postDto.setCompanyName("TechCorp");
        postDto.setLocation("Remote");
        postDto.setExperience("1-3 Years");
        postDto.setJobType("Full-time");
        postDto.setSalary("₹6,00,000 / yr");
        postDto.setSkills("Selenium, JUnit");
        postDto.setDescription("Testing position");
        postDto.setStatus("CLOSED");

        Job closedJob = jobService.createJob(postDto, recruiter);

        assertThrows(IllegalStateException.class, () -> applicationService.applyForJob(closedJob.getId(), seeker),
                "Applying to a CLOSED job must be prevented");
    }

    @Test
    @DisplayName("Verify Recruiter applicant management and status changes")
    void testRecruiterApplicantStatusManagement() {
        User recruiter = userService.findByEmail("recruiter@techcorp.com").orElseThrow();
        User seeker = userService.findByEmail("alex@example.com").orElseThrow();
        List<Job> myJobs = jobService.getJobsByRecruiter(recruiter);
        assertFalse(myJobs.isEmpty());
        Job job = myJobs.get(0);

        Application application = applicationService.applyForJob(job.getId(), seeker);

        // Recruiter changes status to SHORTLISTED
        Application updated = applicationService.updateApplicationStatus(application.getId(), "SHORTLISTED", recruiter);
        assertEquals("SHORTLISTED", updated.getStatus());

        // Recruiter changes status to REJECTED
        Application rejected = applicationService.updateApplicationStatus(application.getId(), "REJECTED", recruiter);
        assertEquals("REJECTED", rejected.getStatus());
    }

    @Test
    @DisplayName("Verify Recruiter cannot manage applicants or jobs belonging to other recruiters")
    void testRecruiterOwnershipAccessControl() {
        User recruiter1 = userService.findByEmail("recruiter@techcorp.com").orElseThrow();

        // Create second recruiter
        UserRegistrationDto regDto = new UserRegistrationDto();
        regDto.setFullName("Competitor Recruiter");
        regDto.setEmail("recruiter2@othercorp.com");
        regDto.setPhone("9988776655");
        regDto.setPassword("pass123");
        regDto.setRole(Role.RECRUITER);
        User recruiter2 = userService.registerUser(regDto);

        // Job owned by recruiter1
        Job job1 = jobService.getJobsByRecruiter(recruiter1).get(0);

        // Recruiter2 tries to delete or edit recruiter1's job
        assertThrows(SecurityException.class, () -> jobService.deleteJob(job1.getId(), recruiter2));
        assertThrows(SecurityException.class, () -> jobService.toggleJobStatus(job1.getId(), recruiter2));
    }

    @Test
    @DisplayName("Verify Admin dashboard metrics and user status moderation")
    void testAdminManagementAndMetrics() {
        long totalUsers = userService.countTotalUsers();
        assertTrue(totalUsers >= 3, "Total users should include admin, sample recruiter, sample seeker");

        long totalJobs = jobService.countTotalJobs();
        assertTrue(totalJobs >= 5, "Sample jobs should be loaded");

        User seeker = userService.findByEmail("alex@example.com").orElseThrow();
        assertTrue(seeker.isActive());

        // Admin toggles user status (deactivate)
        User toggled = userService.toggleUserStatus(seeker.getId());
        assertFalse(toggled.isActive());

        // Deactivated user cannot authenticate
        Optional<User> authAttempt = userService.authenticate(seeker.getEmail(), "seeker123");
        assertFalse(authAttempt.isPresent(), "Deactivated user must not authenticate");

        // Reactivate
        User reactivated = userService.toggleUserStatus(seeker.getId());
        assertTrue(reactivated.isActive());
    }

    @Test
    @DisplayName("Verify Web MVC endpoints: Home, Jobs, Job Details")
    void testPublicWebEndpoints() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("recentJobs", "totalJobs", "totalUsers"));

        mockMvc.perform(get("/jobs"))
                .andExpect(status().isOk())
                .andExpect(view().name("jobs/list"))
                .andExpect(model().attributeExists("jobs", "totalResults"));

        List<Job> jobs = jobService.getAllOpenJobs();
        if (!jobs.isEmpty()) {
            mockMvc.perform(get("/jobs/" + jobs.get(0).getId()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("jobs/details"))
                    .andExpect(model().attributeExists("job"));
        }
    }

    @Test
    @DisplayName("Verify session-based access control redirects unauthenticated users")
    void testSessionAccessControlRedirects() throws Exception {
        // Without session, seeker dashboard redirects to login
        mockMvc.perform(get("/seeker/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=Please login to access your Job Seeker portal."));

        // Without session, recruiter dashboard redirects to login
        mockMvc.perform(get("/recruiter/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=Please login to access the Recruiter portal."));

        // Without session, admin dashboard redirects to login
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=Please login to access the Admin panel."));
    }

    @Test
    @DisplayName("Verify role-based access denial: Job Seeker cannot access Recruiter or Admin dashboard")
    void testRoleBasedAccessDenial() throws Exception {
        User seeker = userService.findByEmail("alex@example.com").orElseThrow();
        MockHttpSession seekerSession = new MockHttpSession();
        seekerSession.setAttribute("currentUser", seeker);
        seekerSession.setAttribute("userRole", seeker.getRole().name());

        // Seeker accessing recruiter dashboard -> redirected to /access-denied
        mockMvc.perform(get("/recruiter/dashboard").session(seekerSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/access-denied"));

        // Seeker accessing admin dashboard -> redirected to /access-denied
        mockMvc.perform(get("/admin/dashboard").session(seekerSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/access-denied"));

        // Seeker accessing seeker dashboard -> 200 OK
        mockMvc.perform(get("/seeker/dashboard").session(seekerSession))
                .andExpect(status().isOk())
                .andExpect(view().name("seeker/dashboard"));
    }
}
