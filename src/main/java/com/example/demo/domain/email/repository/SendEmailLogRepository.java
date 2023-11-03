package com.example.demo.domain.email.repository;

import com.example.demo.domain.email.entity.SendEmailLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SendEmailLogRepository extends JpaRepository<SendEmailLog, Long> {
}