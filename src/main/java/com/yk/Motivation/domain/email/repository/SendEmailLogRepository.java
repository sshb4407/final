package com.yk.Motivation.domain.email.repository;

import com.yk.Motivation.domain.email.entity.SendEmailLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SendEmailLogRepository extends JpaRepository<SendEmailLog, Long> {
}