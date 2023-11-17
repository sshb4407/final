package com.yk.Motivation.domain.member.repository;

import com.yk.Motivation.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberRepositoryCustom {
    Page<Member> findByKw(String kwType, String kw, Pageable pageable);
}