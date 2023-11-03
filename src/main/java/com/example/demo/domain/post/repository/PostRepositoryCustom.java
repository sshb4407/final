package com.example.demo.domain.post.repository;

import com.example.demo.domain.member.entity.Member;
import com.example.demo.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {
    Page<Post> findByKw(String kwType, String kw, boolean isPublic, Pageable pageable);
    Page<Post> findByKw(Member author, String kwType, String kw, Pageable pageable);

}