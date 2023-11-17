package com.yk.Motivation.domain.attr.entity;

import com.yk.Motivation.base.jpa.baseEntity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(
        // 변수명이 같은 데이터 생성되는것을 막는 역할
        // 특정 변수명으로 검색했을 때 초고속 검색이 되도록
        uniqueConstraints = @UniqueConstraint(
                columnNames = { // 아래 4가지 컬럼 조합의 결과가 유니크해야함.
                        "relId", "relTypeCode", "typeCode", "type2Code"
                }
        ),
        indexes = { // 인덱스 지정
                @Index(name = "idx1", columnList = "relTypeCode, typeCode, type2Code")
        }
)
@Setter
@Getter
@AllArgsConstructor(access = PROTECTED)
@NoArgsConstructor(access = PROTECTED)
@SuperBuilder
@ToString(callSuper = true)
public class Attr extends BaseEntity {
    private String relTypeCode;
    private long relId;
    private String typeCode;
    private String type2Code;
    private String val; // value
    private LocalDateTime expireDate;
}