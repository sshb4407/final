package com.yk.Motivation.domain.lesson.entity;

import com.yk.Motivation.base.jpa.baseEntity.BaseEntity;
import com.yk.Motivation.domain.lecture.entity.Lecture;
import com.yk.Motivation.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Fetch;

import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Setter
@Getter
@AllArgsConstructor(access = PROTECTED)
@NoArgsConstructor(access = PROTECTED)
@SuperBuilder
@ToString(callSuper = true)
@Table(
        name = "lesson_playback_time",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"member_id", "lesson_id"}
        )
)
public class LessonPlaybackTime extends BaseEntity {

    @ManyToOne
    private Lesson lesson;

    @ManyToOne(fetch = LAZY)
    private Member member;

    @ManyToOne
    private Lecture lecture;

    private int playbackTime;
}
