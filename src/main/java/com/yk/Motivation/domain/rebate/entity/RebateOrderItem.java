package com.yk.Motivation.domain.rebate.entity;

import com.yk.Motivation.base.jpa.baseEntity.BaseEntity;
import com.yk.Motivation.domain.cash.entity.CashLog;
import com.yk.Motivation.domain.member.entity.Member;
import com.yk.Motivation.domain.order.entity.Order;
import com.yk.Motivation.domain.order.entity.OrderItem;
import com.yk.Motivation.domain.product.entity.Product;
import jakarta.persistence.*;
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
public class RebateOrderItem extends BaseEntity {

    @OneToOne(fetch = LAZY)
    @ToString.Exclude
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))

    private OrderItem orderItem;

    @ManyToOne(fetch = LAZY)
    @ToString.Exclude
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Order order;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Product product;

    // 가격
    private int price; // 권장판매가
    private int salePrice; // 실제판매가
    private int pgFee; // 결제대행사 수수료
    private int payPrice; // 결제금액
    private int refundPrice; // 환불금액
    private boolean isPaid; // 결제여부
    private LocalDateTime payDate; // 결제날짜

    @ManyToOne(fetch = LAZY)
    @ToString.Exclude
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private CashLog rebateCashLog; // 정산에 관련된 환급지급내역
    private LocalDateTime rebateDate;

    // 상품
    private String productSubject;

    // 주문품목
    private LocalDateTime orderItemCreateDate;

    // 구매자 회원
    @ManyToOne(fetch = LAZY)
    @ToString.Exclude
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member buyer;
    private String buyerName;

    // 판매자 회원
    @ManyToOne(fetch = LAZY)
    @ToString.Exclude
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member seller;
    private String sellerName;

    public RebateOrderItem(OrderItem orderItem) {
        this.orderItem = orderItem;
        order = orderItem.getOrder();
        product = orderItem.getProduct();
        price = orderItem.getPrice();
        salePrice = orderItem.getSalePrice();
        pgFee = orderItem.getPgFee();
        payPrice = orderItem.getPayPrice();
        refundPrice = orderItem.getRefundPrice();
        isPaid = orderItem.isPaid();
        payDate = orderItem.getPayDate();

        // 상품 추가데이터
        productSubject = orderItem.getProduct().getLecture().getSubject();

        // 주문품목 추가데이터
        orderItemCreateDate = orderItem.getCreateDate();

        // 구매자 추가데이터
        buyer = orderItem.getOrder().getBuyer();
        buyerName = orderItem.getOrder().getBuyer().getNickname();

        // 판매자 추가데이터
        seller = orderItem.getProduct().getProducer();
        sellerName = orderItem.getProduct().getProducer().getProducerName();
    }

    public int calculateRebatePrice() {
        if (refundPrice > 0) {
            return 0;
        }

        return salePrice - pgFee;
    }

    public boolean isRebateAvailable() {
        if (refundPrice > 0 || rebateDate != null) {
            return false;
        }

        return true;
    }

    public void setRebateDone(CashLog cashLog) {
        rebateDate = LocalDateTime.now();
        this.rebateCashLog = cashLog;
    }

}
