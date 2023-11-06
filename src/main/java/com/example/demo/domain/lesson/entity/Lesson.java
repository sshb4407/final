package com.example.demo.domain.lesson.entity;


import com.example.demo.base.jpa.BaseEntity;
import com.example.demo.domain.genFile.entity.GenFile;
import com.example.demo.domain.lecture.entity.Lecture;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.SuperBuilder;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Setter
@Getter
@AllArgsConstructor(access = PROTECTED)
@NoArgsConstructor(access = PROTECTED)
@SuperBuilder
@ToString(callSuper = true)
public class Lesson extends BaseEntity {

    @ManyToOne
    private Lecture lecture;

    private String subject;

    private int sortNo;

    private int lessonLength;

    private boolean isLessonReady;

    public String getLessonLengthForPrint() {
        return lessonLength/60 + " : " + lessonLength%60;
    }
}
