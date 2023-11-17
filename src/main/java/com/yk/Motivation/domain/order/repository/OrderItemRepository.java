package com.yk.Motivation.domain.order.repository;

import com.yk.Motivation.domain.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findAllByPayDateBetween(LocalDateTime fromDate, LocalDateTime toDate);

    List<OrderItem> findAllByOrderId(long id);

    List<OrderItem> findAllByProductId(long id);
}