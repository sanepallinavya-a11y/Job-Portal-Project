package com.jobportal.repository;

import com.jobportal.entity.Application;
import com.jobportal.entity.Job;
import com.jobportal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByJobSeekerOrderByAppliedDateDesc(User jobSeeker);

    List<Application> findByJobOrderByAppliedDateDesc(Job job);

    @Query("SELECT a FROM Application a WHERE a.job.recruiter = :recruiter ORDER BY a.appliedDate DESC")
    List<Application> findByRecruiterOrderByAppliedDateDesc(@Param("recruiter") User recruiter);

    @Query("SELECT a FROM Application a WHERE a.job.recruiter = :recruiter AND (:jobId IS NULL OR a.job.id = :jobId) AND (:status IS NULL OR a.status = :status) ORDER BY a.appliedDate DESC")
    List<Application> filterRecruiterApplications(
            @Param("recruiter") User recruiter,
            @Param("jobId") Long jobId,
            @Param("status") String status
    );

    boolean existsByJobAndJobSeeker(Job job, User jobSeeker);

    Optional<Application> findByJobAndJobSeeker(Job job, User jobSeeker);

    long countByJobSeeker(User jobSeeker);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.recruiter = :recruiter")
    long countByRecruiter(@Param("recruiter") User recruiter);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.recruiter = :recruiter AND a.status = :status")
    long countByRecruiterAndStatus(@Param("recruiter") User recruiter, @Param("status") String status);

    long countByStatus(String status);

    List<Application> findAllByOrderByAppliedDateDesc();
}
