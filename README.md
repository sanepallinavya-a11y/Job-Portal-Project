# JobPortal - Enterprise Job Portal Web Application

A full-stack, enterprise-grade Job Portal Web Application built from scratch using **Java 17**, **Spring Boot 3**, **Spring MVC**, **Thymeleaf**, **Spring Data JPA**, and a persistent file-based **H2 Database**.

---

## 🌟 Overview & Key Features

JobPortal provides a modern, responsive platform connecting ambitious candidates with forward-thinking employers and recruiters. The platform is structured with role-based access control, session-based authentication, and clear workflows for Job Seekers, Recruiters, and Administrators.

### 1. Public & Home Page
- **Modern Responsive UI**: Clean navigation bar, Hero banner with dynamic call-to-actions, live metric counters, and feature highlights.
- **Recent Openings Showcase**: Highlights the newest open job positions with immediate click-through to details.
- **Quick Demo Credentials**: Login helper shortcuts for rapid demo and testing.

### 2. Candidate / Job Seeker (`JOB_SEEKER`)
- **User Registration & Validation**: Form validation with mandatory fields, valid email verification, exact 10-digit mobile number requirement, and email uniqueness checks.
- **Candidate Dashboard**: Overview of submitted applications, shortlisted status counter, and personalized job recommendations.
- **Profile Management**: Maintain professional qualifications, contact info, key technical skills, years of experience, and educational background.
- **Job Search & Multi-criteria Filtering**: Search positions by title/keyword, location, experience level (0-1 yrs, 1-3 yrs, 3-5 yrs, 5+ yrs), and job type (Full-time, Part-time, Remote, Contract, Internship).
- **One-Click Application Pipeline**:
  - Direct application submission.
  - Strict duplicate application prevention for the same candidate and job.
  - Closed job protection (candidates cannot apply to closed or archived positions).
- **"My Applications" Tracking**: Real-time status tracking (`APPLIED`, `SHORTLISTED`, `REJECTED`) with applied timestamps and direct links to job postings.

### 3. Employer / Recruiter (`RECRUITER`)
- **Recruiter Dashboard**: Total jobs posted, active vacancies count, total candidate resumes received, and shortlisted metrics.
- **Job Lifecycle Management**: Post new vacancies, edit existing listings, toggle job status between `OPEN` and `CLOSED`, or delete listings.
- **Ownership Security**: Recruiters can **only** manage and view applicants for positions created by their own account.
- **Candidate Applicant Review**:
  - Filter applicants by specific job and application status.
  - View applicant contact details, education, work experience, and tagged technical skills.
  - Instantly update candidate recruitment status: `APPLIED`, `SHORTLISTED`, or `REJECTED`.

### 4. Administrator Control Center (`ADMIN`)
- **System Telemetry & Statistics**:
  - Total Registered Users
  - Total Job Seekers
  - Total Recruiters
  - Total Jobs
  - Total Applications
- **User Account Management**: View all platform accounts, toggle account status (`Active` / `Inactive`), or delete accounts (with self-deletion protection).
- **Platform Job Moderation**: View all jobs across employers, toggle open/close status, or delete spam/inappropriate listings.
- **Global Applications Monitor**: Centralized view of all candidate applications across all companies and vacancies.
- **Direct Database Console**: Instant access to the embedded H2 Web Console.

---

## 🛠️ Technology Stack

| Component | Technology |
| :--- | :--- |
| **Language** | Java 17 (OpenJDK 17) |
| **Framework** | Spring Boot 3.2.5 |
| **Architecture** | Layered Spring MVC (Controller, Service, Repository, Entity, DTO) |
| **Template Engine** | Thymeleaf 3 (with layout fragments) |
| **Persistence / ORM** | Spring Data JPA / Hibernate 6 |
| **Database** | Persistent file-based H2 Database (`./data/jobportaldb`) |
| **Validation** | Jakarta Bean Validation (`spring-boot-starter-validation`) |
| **Styling & Icons** | Bootstrap 5.3.3, Bootstrap Icons 1.11.3, Custom Modern CSS |
| **Build Tool** | Apache Maven 3.9+ |

---

## 🗄️ Database Details

- **Database URL**: `jdbc:h2:file:./data/jobportaldb`
- **Username**: `sa`
- **Password**: *(empty)*
- **DDL Mode**: `spring.jpa.hibernate.ddl-auto=update`
- **Console URL**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- **Persistence**: Data is saved to the local file `./data/jobportaldb.mv.db` across application restarts.

---

## 👤 User Roles & Default Accounts

The application automatically seeds a default administrator account and sample demonstration accounts on startup if the database is fresh:

| Role | Email | Password | Purpose |
| :--- | :--- | :--- | :--- |
| **ADMIN** | `admin@jobportal.com` | `admin123` | Platform oversight, user moderation, telemetry |
| **RECRUITER** | `recruiter@techcorp.com` | `recruiter123` | Posting jobs, managing applicants |
| **JOB_SEEKER** | `alex@example.com` | `seeker123` | Applying to jobs, tracking application status |

*(You can also register any new Job Seeker or Recruiter account directly from the `/register` page).*

---

## 🚀 How to Run the Application

### Prerequisites
- **Java 17** installed (`java -version`)
- **Maven** (either on your system PATH or via the provided launcher)

### Option 1: Using the provided Windows Batch script (Recommended)
Double-click `run.bat` or run:
```cmd
run.bat
```

### Option 2: Using Maven directly
```bash
mvn spring-boot:run
```
Or if using your local Maven path:
```bash
"C:\Users\S.Hema\.maven\maven-3.9.15\bin\mvn.cmd" spring-boot:run
```

The application will start on: **[http://localhost:8080](http://localhost:8080)**

---

## 🧭 Key Navigation URLs

- **Homepage**: [http://localhost:8080/](http://localhost:8080/)
- **Find Jobs**: [http://localhost:8080/jobs](http://localhost:8080/jobs)
- **Sign In**: [http://localhost:8080/login](http://localhost:8080/login)
- **Register**: [http://localhost:8080/register](http://localhost:8080/register)
- **Job Seeker Dashboard**: [http://localhost:8080/seeker/dashboard](http://localhost:8080/seeker/dashboard)
- **Candidate Profile**: [http://localhost:8080/seeker/profile](http://localhost:8080/seeker/profile)
- **My Applications**: [http://localhost:8080/seeker/applications](http://localhost:8080/seeker/applications)
- **Recruiter Dashboard**: [http://localhost:8080/recruiter/dashboard](http://localhost:8080/recruiter/dashboard)
- **Post a Job**: [http://localhost:8080/recruiter/jobs/new](http://localhost:8080/recruiter/jobs/new)
- **Recruiter Jobs**: [http://localhost:8080/recruiter/jobs](http://localhost:8080/recruiter/jobs)
- **Review Applicants**: [http://localhost:8080/recruiter/applicants](http://localhost:8080/recruiter/applicants)
- **Admin Dashboard**: [http://localhost:8080/admin/dashboard](http://localhost:8080/admin/dashboard)
- **Admin User Management**: [http://localhost:8080/admin/users](http://localhost:8080/admin/users)
- **Admin Job Moderation**: [http://localhost:8080/admin/jobs](http://localhost:8080/admin/jobs)
- **H2 Database Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

---

## 🧪 Testing & Verification

A comprehensive automated test suite (`JobPortalWorkflowTests.java`) covers all critical paths:
1. Default Admin auto-creation verification.
2. User registration with valid inputs & 10-digit phone formatting.
3. Duplicate email registration rejection.
4. Authentication with valid and invalid credentials.
5. Candidate profile modification and skill persistence.
6. Job posting, multi-criteria keyword and category search.
7. Job application submission.
8. Duplicate application prevention.
9. Prohibiting applications to closed jobs.
10. Recruiter applicant status lifecycle (`APPLIED` ➔ `SHORTLISTED` ➔ `REJECTED`).
11. Recruiter ownership authorization checks.
12. Admin metric calculation and account moderation.
13. Session-based URL access interception and redirects.
14. Role-based unauthorized access rejection (HTTP 403 Access Denied).

To execute the test suite:
```bash
mvn test
```

---

## 🔮 Future Enhancements
- Resume PDF upload and download support.
- Email notifications on status changes (Shortlisted/Rejected) via JavaMailSender.
- Password hashing (e.g. BCrypt) and JWT integration for mobile clients.
- Advanced salary analytics and interview scheduling.
