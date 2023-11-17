package com.yk.Motivation.domain.product.entity;

import com.yk.Motivation.base.jpa.baseEntity.BaseEntity;
import com.yk.Motivation.domain.lecture.entity.Lecture;
import com.yk.Motivation.domain.member.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.*;
import lombok.experimental.SuperBuilder;

import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Setter
@Getter
@AllArgsConstructor(access = PROTECTED)
@NoArgsConstructor(access = PROTECTED)
@SuperBuilder
@ToString(callSuper = true)
public class Product extends BaseEntity {

    @ManyToOne(fetch = LAZY)
    private Member producer;

    @OneToOne(fetch = LAZY)
    private Lecture lecture;

    private int price;

    private boolean isFree;

    public int getSalePrice() {
        return getPrice();
    }

    public boolean isOrderable() {
        return true;
    }
}