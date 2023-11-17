package com.yk.Motivation.domain.order.repository;

import com.yk.Motivation.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByBuyerIdAndIsPaidTrueOrderByIdDesc(Long buyerId);
}