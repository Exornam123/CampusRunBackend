package com.example.campusrunbackend.repository;

import com.example.campusrunbackend.model.Report;
import com.example.campusrunbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByReporter(User reporter);
    List<Report> findByReported(User reported);
    List<Report> findByResolved(boolean resolved);
}
