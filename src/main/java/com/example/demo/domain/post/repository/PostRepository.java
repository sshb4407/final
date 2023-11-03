package com.example.demo.domain.post.repository;

import com.example.demo.domain.member.entity.Member;
import com.example.demo.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    Page<Post> findByAuthorAndPostTags_content(Member author, String tagContent, Pageable pageable);

    Page<Post> findByPostTags_contentAndIsPublic(String tagContent, Boolean isPublic, Pageable pageable);

    Page<Post> findByAuthorAndPostTags_contentAndIsPublic(Member author, String tagContent, boolean isPublic, Pageable pageable);

}