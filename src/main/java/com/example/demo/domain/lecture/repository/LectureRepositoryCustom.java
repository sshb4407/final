package com.example.demo.domain.lecture.repository;

import com.example.demo.domain.article.entity.Article;
import com.example.demo.domain.board.entity.Board;
import com.example.demo.domain.lecture.entity.Lecture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LectureRepositoryCustom {

    Page<Lecture> findByKw(String kwType, String kw, Pageable pageable);

}
