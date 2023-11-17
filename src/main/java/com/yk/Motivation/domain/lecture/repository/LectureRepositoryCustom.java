package com.yk.Motivation.domain.lecture.repository;

import com.yk.Motivation.domain.article.entity.Article;
import com.yk.Motivation.domain.board.entity.Board;
import com.yk.Motivation.domain.lecture.entity.Lecture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LectureRepositoryCustom {

    Page<Lecture> findByKw(String kwType, String kw, Pageable pageable);

}
