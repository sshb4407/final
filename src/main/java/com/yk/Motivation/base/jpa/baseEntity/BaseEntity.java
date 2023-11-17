package com.yk.Motivation.base.jpa.baseEntity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@SuperBuilder
@MappedSuperclass // BaseEntiy 가 db 에 테이블로 생성 안되게 해줌
@NoArgsConstructor(access = PROTECTED)
@EntityListeners(AuditingEntityListener.class) // @CreateDate , LastModifiedDate 사용가능하게
@ToString
@EqualsAndHashCode
public class BaseEntity { // 모든 Entity 가 상속 받게 될 BaseEntity

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @CreatedDate
    private LocalDateTime createDate;

    @LastModifiedDate
    private LocalDateTime modifyDate;

    @Transient // 아래 필드가 DB 필드가 되는 것을 막는다.
    @Builder.Default
    private Map<String, Object> extra = new LinkedHashMap<>();

    public String getModelName() { // Entity class 이름 첫자 소문자로 return
        String simpleName = this.getClass().getSimpleName();
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }

}