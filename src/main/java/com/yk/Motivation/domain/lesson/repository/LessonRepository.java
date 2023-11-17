package com.yk.Motivation.domain.lesson.repository;

import com.yk.Motivation.domain.lesson.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByLectureId_Id(Long id);
}
