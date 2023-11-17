package com.yk.Motivation.domain.order.entity;

import com.yk.Motivation.base.jpa.baseEntity.BaseEntity;
import com.yk.Motivation.domain.member.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Setter
@Getter
@AllArgsConstructor(access = PROTECTED)
@NoArgsConstructor(access = PROTECTED)
@SuperBuilder
@ToString(callSuper = true)
@Table(name = "product_order")
public class Order extends BaseEntity {

    @ManyToOne(fetch = LAZY)
    private Member buyer;

    private String name;

    private boolean isPaid; // 결제여부
    private boolean isCanceled; // 취소여부
    private boolean isRefunded; // 환불여부
    private String paymentKey;
    private String refundReason;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    public void addOrderItem(OrderItem orderItem) {
        orderItem.setOrder(this);

        orderItems.add(orderItem);
    }

    public int calculatePayPrice() {
        int payPrice = 0;

        for (OrderItem orderItem : orderItems) {
            payPrice += orderItem.getSalePrice();
        }

        return payPrice;
    }

    public void setPaymentDone() {
        for (OrderItem orderItem : orderItems) {
            orderItem.setPaymentDone();
        }

        isPaid = true;
    }

    public void setRefundDone() {
        for (OrderItem orderItem : orderItems) {
            orderItem.setRefundDone();
        }

        isRefunded = true;
    }

    public int getPayPrice() {
        int payPrice = 0;
        for (OrderItem orderItem : orderItems) {
            payPrice += orderItem.getPayPrice();
        }

        return payPrice;
    }

    public void makeName() {
        String name = orderItems.get(0).getProduct().getLecture().getSubject();

        if (orderItems.size() > 1) {
            name += " 외 %d강".formatted(orderItems.size() - 1);
        }

        this.name = name;
    }

    public boolean isPayable() {
        if ( isPaid ) return false;
        if ( isCanceled ) return false;

        return true;
    }
}
