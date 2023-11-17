package com.yk.Motivation.domain.series.repository;

import com.yk.Motivation.domain.series.entity.Series;
import com.yk.Motivation.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SeriesRepositoryCustom {
    Page<Series> findByKw(String kwType, String kw, boolean isPublic, Pageable pageable);

    Page<Series> findByKw(Member author, String kwType, String kw, Pageable pageable);
}