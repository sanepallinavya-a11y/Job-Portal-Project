package com.jobportal.repository;

import com.jobportal.entity.Job;
import com.jobportal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByRecruiterOrderByPostedDateDesc(User recruiter);

    List<Job> findByStatusOrderByPostedDateDesc(String status);

    List<Job> findAllByOrderByPostedDateDesc();

    long countByStatus(String status);

    long countByRecruiter(User recruiter);

    @Query("SELECT j FROM Job j WHERE " +
           "(:status IS NULL OR j.status = :status) AND " +
           "(:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(j.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(j.skills) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:experience IS NULL OR j.experience = :experience) AND " +
           "(:jobType IS NULL OR j.jobType = :jobType) " +
           "ORDER BY j.postedDate DESC")
    List<Job> searchJobs(
            @Param("status") String status,
            @Param("keyword") String keyword,
            @Param("location") String location,
            @Param("experience") String experience,
            @Param("jobType") String jobType
    );
}
