package com.yk.Motivation.domain.series.repository;

import com.yk.Motivation.domain.member.entity.Member;
import com.yk.Motivation.domain.post.entity.Post;
import com.yk.Motivation.domain.series.entity.Series;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeriesRepository extends JpaRepository<Series, Long>, SeriesRepositoryCustom {
    Page<Series> findBySeriesTags_contentAndIsPublic(String tagContent, Pageable pageable, boolean isPublic);

    Page<Series> findByAuthorAndSeriesTags_content(Member author, String tagContent, Pageable pageable);
}