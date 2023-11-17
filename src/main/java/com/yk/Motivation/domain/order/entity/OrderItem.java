package com.yk.Motivation.domain.order.entity;

import com.yk.Motivation.base.jpa.baseEntity.BaseEntity;
import com.yk.Motivation.domain.product.entity.Product;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Setter
@Getter
@AllArgsConstructor(access = PROTECTED)
@NoArgsConstructor(access = PROTECTED)
@SuperBuilder
@ToString(callSuper = true)
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = LAZY)
    @ToString.Exclude
    private Order order;

    private LocalDateTime payDate;

    @ManyToOne(fetch = LAZY)
    private Product product;

    // 가격
    private int price; // 권장판매가
    private int salePrice; // 실제판매가
    private int pgFee; // 결제대행사 수수료
    private int payPrice; // 결제금액
    private int refundPrice; // 환불금액
    private boolean isPaid; // 결제여부

    public OrderItem(Product product) {
        this.product = product;
        this.price = product.getPrice();
        this.salePrice = product.getSalePrice();
    }

    public void setPaymentDone() {
        this.pgFee = 0;
        this.payPrice = getSalePrice();
        this.isPaid = true;
        this.payDate = LocalDateTime.now();
    }

    public void setRefundDone() {
        this.refundPrice = payPrice;
    }
}
