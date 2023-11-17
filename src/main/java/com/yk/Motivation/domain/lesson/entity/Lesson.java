package com.yk.Motivation.domain.lesson.entity;

import com.yk.Motivation.base.jpa.baseEntity.BaseEntity;
import com.yk.Motivation.domain.genFile.entity.GenFile;
import com.yk.Motivation.domain.lecture.entity.Lecture;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;

import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Setter
@Getter
@AllArgsConstructor(access = PROTECTED)
@NoArgsConstructor(access = PROTECTED)
@SuperBuilder
@ToString(callSuper = true)
public class Lesson extends BaseEntity {

    @ManyToOne(fetch = LAZY)
    private Lecture lecture;

    private String subject;

    private int sortNo;

    private int lessonLength;

    private boolean isLessonReady;

    public String getLessonLengthForPrint() {
        return lessonLength/60 + " : " + lessonLength%60;
    }
}
