package com.yk.Motivation.domain.cash.repository;

import com.yk.Motivation.domain.cash.entity.CashLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashLogRepository extends JpaRepository<CashLog, Long> {
}
