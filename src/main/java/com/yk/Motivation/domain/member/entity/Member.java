package com.yk.Motivation.domain.member.entity;

import com.yk.Motivation.base.jpa.baseEntity.BaseEntity;
import com.yk.Motivation.domain.lecture.entity.Lecture;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Setter
@Getter
@AllArgsConstructor(access = PROTECTED)
@NoArgsConstructor(access = PROTECTED)
@SuperBuilder
@ToString(callSuper = true)
public class Member extends BaseEntity {
    @Column(unique = true)
    private String username;
    private String password;
    @Column(unique = true)
    private String nickname;
    @Column(unique = true)
    private String producerName;
    private String email;
    private long restCash;

    @ManyToMany(fetch = LAZY)
    @JoinTable(
            name = "member_lecture",
            joinColumns = @JoinColumn(name = "member_id"),
            inverseJoinColumns = @JoinColumn(name = "lecture_id")
    )
    @Builder.Default
    private List<Lecture> lectures = new ArrayList<>();

    public boolean isAdmin() {
        return "admin".equals(username);
    }

    public boolean isProducer() {
        return producerName != null;
    }

    public List<? extends GrantedAuthority> getGrantedAuthorities() {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        // 모든 멤버는 member 권한을 가진다.
        grantedAuthorities.add(new SimpleGrantedAuthority("member"));

        // username이 admin인 회원은 추가로 admin 권한도 가진다.
        if (isAdmin()) grantedAuthorities.add(new SimpleGrantedAuthority("admin"));
        if (isProducer()) grantedAuthorities.add(new SimpleGrantedAuthority("producer"));

        return grantedAuthorities;
    }

    public boolean isSocialMember() {
        return username.startsWith("KAKAO_");
    }

    public boolean isModifyAvailable() {
        return !isSocialMember();
    }

    public String getEmailForPrint() {
        if (isSocialMember()) return "-";
        return email;
    }
}