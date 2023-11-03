package com.example.demo.domain.lecture.repository;

import com.example.demo.domain.lecture.repository.LectureRepositoryCustom;
import com.example.demo.domain.article.entity.Article;
import com.example.demo.domain.lecture.entity.Lecture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface LectureRepository extends JpaRepository<Lecture, Long>, LectureRepositoryCustom {

    Page<Lecture> findByLectureTags_contentAndIsPublicTrue(String tagContent, Pageable pageable);
}
