package com.yk.Motivation.domain.post.repository;

import com.yk.Motivation.domain.member.entity.Member;
import com.yk.Motivation.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {
    Page<Post> findByKw(String kwType, String kw, boolean isPublic, Pageable pageable);
    Page<Post> findByKw(Member author, String kwType, String kw, Pageable pageable);

}